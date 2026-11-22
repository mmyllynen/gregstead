package fi.gregstead.mushroom;

import fi.gregstead.mushroom.mixin.NetherPortalAccess;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiRecord;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.NetherPortalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.portal.TeleportTransition;

import org.jspecify.annotations.Nullable;

import java.util.Comparator;
import java.util.Objects;
import java.util.Optional;

import net.neoforged.neoforge.event.EventHooks;

public final class MushroomPortalBlock extends NetherPortalBlock {
    private static final int SEARCH_RADIUS = 16;
    private static final DustParticleOptions TEAL = new DustParticleOptions(0x29e6c5, 0.75F);

    MushroomPortalBlock(Properties properties) {
        super(properties);
    }

    public static boolean hasUnlitFrame(Level level, BlockPos pos) {
        return unlitFrame(level, pos) != null;
    }

    public static boolean ignite(Level level, BlockPos pos) {
        if (!(level instanceof ServerLevel server)) return false;
        PortalFrame frame = unlitFrame(level, pos);
        if (frame == null) return false;
        if (EventHooks.onTrySpawnPortal(level, pos, Optional.of(frame.vanillaShape(level, false))).isEmpty()) return false;
        fill(server, frame);
        return true;
    }

    private static @Nullable PortalFrame unlitFrame(Level level, BlockPos pos) {
        if (!MushroomDimension.allowsPortal(level)) return null;
        PortalFrame frame = PortalFrame.find(level, pos, Direction.Axis.X, false);
        return frame != null ? frame : PortalFrame.find(level, pos, Direction.Axis.Z, false);
    }

    private static void fill(ServerLevel level, PortalFrame frame) {
        BlockState state = MushroomDimension.PORTAL.get().defaultBlockState().setValue(AXIS, frame.axis());
        for (int x = 0; x < frame.width(); x++) {
            for (int y = 0; y < frame.height(); y++) {
                level.setBlock(frame.at(x, y), state, Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
            }
        }
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos,
                                     Direction direction, BlockPos neighbor, BlockState neighborState, RandomSource random) {
        Direction.Axis axis = state.getValue(AXIS);
        if ((!direction.getAxis().isHorizontal() || direction.getAxis() == axis)
                && (!neighborState.is(this) || neighborState.getValue(AXIS) != axis)
                && PortalFrame.find(level, pos, axis, true) == null) {
            return Blocks.AIR.defaultBlockState();
        }
        return state;
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        // This portal has no Nether mob spawning.
    }

    @Override
    public @Nullable TeleportTransition getPortalDestination(ServerLevel current, Entity entity, BlockPos entry) {
        if (!MushroomDimension.allowsPortal(current)) return null;
        ServerLevel destination = current.getServer().getLevel(
                current.dimension() == Level.OVERWORLD ? MushroomDimension.VANILLA : Level.OVERWORLD);
        PortalFrame original = litFrame(current, entry);
        if (destination == null || original == null) return noDestination(entity);

        PortalLinks links = PortalLinks.get(current);
        BlockPos stored = links.target(current.dimension(), original.bottom());
        if (stored != null) {
            PortalFrame frame = litFrame(destination, stored);
            if (frame != null && frame.bottom().equals(stored)) {
                TeleportTransition transition = safeTransition(destination, entity, entry, frame);
                if (transition != null) return transition;
                return noDestination(entity);
            }
            if (!entity.isSpectator()) links.unlink(current.dimension(), original.bottom());
        }

        BlockPos origin = destination.getWorldBorder().clampToBounds(entity.getX(), entity.getY(), entity.getZ());
        TeleportTransition transition = findExisting(destination, entity, entry, origin, original, links, current);
        if (transition != null) return transition;
        if (entity.isSpectator()) return null;

        PortalFrame created = PortalSite.create(destination, origin, original.axis());
        if (created == null) return noDestination(entity);
        fill(destination, created);
        transition = safeTransition(destination, entity, entry, created);
        if (transition == null) return noDestination(entity);
        links.link(current.dimension(), original.bottom(), created.bottom());
        return transition;
    }

    private static @Nullable TeleportTransition findExisting(ServerLevel destination, Entity entity, BlockPos entry,
                                                               BlockPos origin, PortalFrame original, PortalLinks links,
                                                               ServerLevel current) {
        PoiManager pois = destination.getPoiManager();
        pois.ensureLoadedAndValid(destination, origin, SEARCH_RADIUS);
        var frames = pois.getInSquare(type -> type.is(MushroomDimension.PORTAL_POI), origin, SEARCH_RADIUS,
                        PoiManager.Occupancy.ANY)
                .map(PoiRecord::getPos)
                .map(pos -> litFrame(destination, pos))
                .filter(Objects::nonNull)
                .distinct()
                .filter(frame -> links.target(destination.dimension(), frame.bottom()) == null)
                .sorted(Comparator.comparingDouble(frame -> frame.bottom().distSqr(origin)))
                .toList();
        for (PortalFrame frame : frames) {
            TeleportTransition transition = safeTransition(destination, entity, entry, frame);
            if (transition == null) continue;
            if (!entity.isSpectator()) links.link(current.dimension(), original.bottom(), frame.bottom());
            return transition;
        }
        return null;
    }

    private static @Nullable PortalFrame litFrame(ServerLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return state.is(MushroomDimension.PORTAL.get()) ? PortalFrame.find(level, pos, state.getValue(AXIS), true) : null;
    }

    private static @Nullable TeleportTransition safeTransition(ServerLevel level, Entity entity, BlockPos entry, PortalFrame frame) {
        var transition = NetherPortalAccess.mushroom$transition(entity, entry, frame.rectangle(), level,
                TeleportTransition.PLAY_PORTAL_SOUND.then(TeleportTransition.PLACE_PORTAL_TICKET));
        var landing = entity.getDimensions(entity.getPose()).makeBoundingBox(transition.position());
        if (!level.getWorldBorder().isWithinBounds(landing) || !level.noCollision(entity, landing)
                || level.containsAnyLiquid(landing)) return null;
        return transition;
    }

    private static @Nullable TeleportTransition noDestination(Entity entity) {
        if (entity instanceof ServerPlayer player) {
            player.sendSystemMessage(Component.translatable("message.gregstead_mushroom.no_destination"), true);
        }
        return null;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(100) == 0) {
            level.playLocalSound(pos, SoundEvents.PORTAL_AMBIENT, SoundSource.BLOCKS, 0.5F,
                    random.nextFloat() * 0.4F + 0.8F, false);
        }
        var axis = state.getValue(AXIS);
        for (int i = 0; i < 4; i++) {
            double side = random.nextBoolean() ? 0.77 : 0.23;
            double along = random.nextDouble();
            level.addParticle(TEAL, pos.getX() + (axis == Direction.Axis.X ? along : side),
                    pos.getY() + random.nextDouble(), pos.getZ() + (axis == Direction.Axis.Z ? along : side),
                    0.0, 0.0, 0.0);
        }
    }
}
