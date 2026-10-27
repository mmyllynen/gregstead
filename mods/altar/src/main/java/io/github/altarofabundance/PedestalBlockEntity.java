package io.github.altarofabundance;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;

public final class PedestalBlockEntity extends BlockEntity {
    public static final int RADIUS = 3;
    public static final int RITUAL_TICKS = 60;
    private ItemStack item = ItemStack.EMPTY;
    // Progress and participants deliberately are not persisted: unloading restarts the ritual.
    private int progress;
    private BlockPos activeMaterial;
    private BlockPos activeOutput;
    private ItemStack activeItem = ItemStack.EMPTY;
    private ItemStack activeOffering = ItemStack.EMPTY;

    public PedestalBlockEntity(BlockPos pos, BlockState state) {
        super(AltarOfAbundance.PEDESTAL.get(), pos, state);
    }

    public PedestalBlock.Role role() { return ((PedestalBlock) getBlockState().getBlock()).role(); }
    public ItemStack displayedItem() { return item.copy(); }
    public int progress() { return progress; }

    public static boolean validMaterial(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() instanceof BlockItem
            && stack.getMaxStackSize() == 64 && stack.is(AltarOfAbundance.MATERIALS);
    }

    public String insert(ItemStack held, boolean creative) {
        if (level == null || level.isClientSide()) return "invalid";
        if (role() == PedestalBlock.Role.OUTPUT) return "output";
        if (!item.isEmpty()) return "occupied";
        if (role() == PedestalBlock.Role.MATERIAL ? !validMaterial(held) : !held.is(AltarOfAbundance.OFFERINGS)) return "invalid";
        item = held.copyWithCount(1);
        if (!creative) held.shrink(1);
        resetNearbyRituals();
        sync();
        return "inserted";
    }

    public ItemStack take() {
        if (level == null || level.isClientSide()) return ItemStack.EMPTY;
        ItemStack taken = item;
        item = ItemStack.EMPTY;
        resetNearbyRituals();
        sync();
        return taken;
    }

    private void sync() {
        setChanged();
        if (level != null) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
    }

    private void resetNearbyRituals() {
        reset();
        if (level instanceof ServerLevel server) {
            for (PedestalBlockEntity nearby : nearby(server, worldPosition, PedestalBlock.Role.OFFERING)) nearby.reset();
        }
    }

    private void reset() {
        progress = 0;
        activeMaterial = null;
        activeOutput = null;
        activeItem = ItemStack.EMPTY;
        activeOffering = ItemStack.EMPTY;
    }

    // Never fetch block entities in unloaded chunks: proximity checks must not load the world.
    private static List<PedestalBlockEntity> nearby(ServerLevel level, BlockPos center, PedestalBlock.Role role) {
        List<PedestalBlockEntity> result = new ArrayList<>();
        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-RADIUS, -RADIUS, -RADIUS), center.offset(RADIUS, RADIUS, RADIUS))) {
            if (center.distSqr(pos) <= RADIUS * RADIUS && level.hasChunkAt(pos)
                    && level.getBlockEntity(pos) instanceof PedestalBlockEntity pedestal && !pedestal.isRemoved() && pedestal.role() == role) {
                result.add(pedestal);
            }
        }
        return result;
    }

    private record Structure(PedestalBlockEntity material, PedestalBlockEntity output, String error) {}

    private Structure structure(ServerLevel server) {
        List<PedestalBlockEntity> materials = nearby(server, worldPosition, PedestalBlock.Role.MATERIAL);
        List<PedestalBlockEntity> outputs = nearby(server, worldPosition, PedestalBlock.Role.OUTPUT);
        if (materials.isEmpty() || outputs.isEmpty()) return new Structure(null, null, "missing");
        if (materials.size() != 1 || outputs.size() != 1) return new Structure(null, null, "ambiguous");
        PedestalBlockEntity material = materials.getFirst();
        PedestalBlockEntity output = outputs.getFirst();
        if (nearby(server, material.worldPosition, PedestalBlock.Role.OFFERING).size() != 1
                || nearby(server, output.worldPosition, PedestalBlock.Role.OFFERING).size() != 1) {
            return new Structure(null, null, "ambiguous");
        }
        if (!server.hasChunkAt(output.worldPosition.above()) || !server.getBlockState(output.worldPosition.above()).isAir()) {
            return new Structure(null, null, "blocked");
        }
        return new Structure(material, output, "ready");
    }

    public String status() {
        if (!(level instanceof ServerLevel server)) return "missing";
        if (role() == PedestalBlock.Role.OUTPUT) return "output";
        if (role() == PedestalBlock.Role.MATERIAL) return "material";
        Structure structure = structure(server);
        if (!structure.error.equals("ready")) return structure.error;
        if (!validMaterial(structure.material.item)) return "material";
        if (item.isEmpty() || !item.is(AltarOfAbundance.OFFERINGS)) return "offering";
        return progress > 0 ? "running" : "ready";
    }

    public void tick(ServerLevel server) {
        if (role() != PedestalBlock.Role.OFFERING) return;
        // Idle checks are staggered; active rituals validate on every server tick.
        if (progress == 0 && (server.getGameTime() + worldPosition.asLong()) % 10 != 0) return;
        Structure structure = structure(server);
        if (!structure.error.equals("ready") || !validMaterial(structure.material.item)
                || item.isEmpty() || !item.is(AltarOfAbundance.OFFERINGS)) {
            reset();
            return;
        }
        PedestalBlockEntity material = structure.material;
        PedestalBlockEntity output = structure.output;
        if (progress > 0 && (!material.worldPosition.equals(activeMaterial) || !output.worldPosition.equals(activeOutput)
                || !ItemStack.isSameItemSameComponents(activeItem, material.item)
                || !ItemStack.isSameItemSameComponents(activeOffering, item))) reset();
        if (progress == 0) {
            activeMaterial = material.worldPosition;
            activeOutput = output.worldPosition;
            activeItem = material.item.copy();
            activeOffering = item.copy();
            server.playSound(null, worldPosition, SoundEvents.BEACON_ACTIVATE, SoundSource.BLOCKS, 0.5F, 1.4F);
        }
        progress++;
        if (progress % 5 == 0) {
            particles(server, material.worldPosition, 3);
            particles(server, worldPosition, 3);
            particles(server, output.worldPosition, 3);
        }
        if (progress < RITUAL_TICKS) return;

        ItemStack result = material.item.copyWithCount(64);
        Vec3 at = Vec3.atBottomCenterOf(output.worldPosition).add(0, 1.05, 0);
        ItemEntity drop = new ItemEntity(server, at.x, at.y, at.z, result);
        drop.setDeltaMovement(0, 0.08, 0);
        drop.setDefaultPickUpDelay();
        // A failed entity spawn leaves ingredients untouched and retries with a fresh ritual.
        if (!server.addFreshEntity(drop)) {
            reset();
            return;
        }
        material.item = ItemStack.EMPTY;
        item = ItemStack.EMPTY;
        material.sync();
        sync();
        reset();
        server.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, at.x, at.y, at.z, 30, 0.3, 0.4, 0.3, 0.05);
        server.playSound(null, output.worldPosition, SoundEvents.PLAYER_LEVELUP, SoundSource.BLOCKS, 0.6F, 1.5F);
    }

    private static void particles(ServerLevel server, BlockPos pos, int count) {
        server.sendParticles(new DustParticleOptions(0xFFD470, 1.0F), pos.getX() + 0.5, pos.getY() + 1.1, pos.getZ() + 0.5,
            count, 0.25, 0.25, 0.25, 0.1);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        item = input.read("Item", ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY);
        if (!item.isEmpty()) item = item.copyWithCount(1);
        reset();
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.store("Item", ItemStack.OPTIONAL_CODEC, item);
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Override
    public void onChunkUnloaded() {
        resetNearbyRituals();
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        if (level instanceof ServerLevel server) {
            ItemStack contents = take();
            if (!contents.isEmpty()) Block.popResource(server, pos, contents);
        }
        super.preRemoveSideEffects(pos, state);
    }
}
