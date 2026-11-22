package fi.gregstead.mushroom;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;

import org.jspecify.annotations.Nullable;

final class PortalSite {
    private static final int RADIUS = 16;

    static @Nullable PortalFrame create(ServerLevel level, BlockPos origin, Direction.Axis axis) {
        PortalFrame best = null;
        double distance = Double.POSITIVE_INFINITY;
        for (BlockPos column : BlockPos.spiralAround(origin, RADIUS, Direction.EAST, Direction.SOUTH)) {
            if (!level.getWorldBorder().isWithinBounds(column)) continue;
            level.getChunk(column);
            int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING, column.getX(), column.getZ()) + 1;
            var candidate = new PortalFrame(column.atY(y), axis, 2, 3);
            double difference = candidate.bottom().distSqr(origin);
            if (difference < distance && canBuild(level, candidate, false)) {
                distance = difference;
                best = candidate;
            }
            if (distance == 0.0) break;
        }
        if (best != null) {
            build(level, best, false);
            return best;
        }

        for (BlockPos column : BlockPos.spiralAround(origin, RADIUS, Direction.EAST, Direction.SOUTH)) {
            if (!level.getWorldBorder().isWithinBounds(column)) continue;
            level.getChunk(column);
            int surface = level.getHeight(Heightmap.Types.MOTION_BLOCKING, column.getX(), column.getZ());
            int y = Math.max(70, surface + 3);
            var candidate = new PortalFrame(column.atY(y), axis, 2, 3);
            if (canBuild(level, candidate, true)) {
                build(level, candidate, true);
                return candidate;
            }
        }
        return null;
    }

    private static boolean canBuild(ServerLevel level, PortalFrame frame, boolean platform) {
        Direction perpendicular = frame.right().getClockWise();
        for (int x = -1; x <= 2; x++) {
            for (int depth = -1; depth <= 1; depth++) {
                BlockPos foot = frame.at(x, -1).relative(perpendicular, depth);
                if (!allowed(level, foot)) return false;
                if (platform) {
                    if (!level.getBlockState(foot).isAir()) return false;
                } else if (!level.getBlockState(foot.below()).isFaceSturdy(level, foot.below(), Direction.UP)) {
                    return false;
                }
                for (int y = 0; y <= 4; y++) {
                    BlockPos pos = foot.above(y);
                    if (!allowed(level, pos) || !level.getBlockState(pos).isAir()) return false;
                }
            }
        }
        return true;
    }

    private static boolean allowed(ServerLevel level, BlockPos pos) {
        return level.isInWorldBounds(pos) && level.getWorldBorder().isWithinBounds(pos)
                && PortalClaims.allowsAutomaticPlacement(level, pos);
    }

    private static void build(ServerLevel level, PortalFrame frame, boolean platform) {
        for (int x = -1; x <= 2; x++) {
            for (int y = -1; y <= 3; y++) {
                if (x == -1 || x == 2 || y == -1 || y == 3) {
                    level.setBlockAndUpdate(frame.at(x, y), (x == 0 && y == -1 ? Blocks.CRYING_OBSIDIAN : Blocks.OBSIDIAN)
                            .defaultBlockState());
                }
            }
        }
        if (platform) {
            Direction perpendicular = frame.right().getClockWise();
            for (int x = -1; x <= 2; x++) {
                level.setBlockAndUpdate(frame.at(x, -1).relative(perpendicular), Blocks.OBSIDIAN.defaultBlockState());
                level.setBlockAndUpdate(frame.at(x, -1).relative(perpendicular.getOpposite()), Blocks.OBSIDIAN.defaultBlockState());
            }
        }
    }
}
