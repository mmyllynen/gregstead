package fi.gregstead.mushroom;

import fi.gregstead.mushroom.mixin.PortalShapeAccess;

import net.minecraft.util.BlockUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.portal.PortalShape;

import org.jspecify.annotations.Nullable;

record PortalFrame(BlockPos bottom, Direction.Axis axis, int width, int height) {
    Direction right() {
        return Direction.get(Direction.AxisDirection.POSITIVE, axis);
    }

    BlockPos at(int x, int y) {
        return bottom.relative(right(), x).above(y);
    }

    PortalShape vanillaShape(BlockGetter level, boolean lit) {
        return PortalShape.findAnyShape(new ShapeView(level, axis, lit), bottom, axis);
    }

    BlockUtil.FoundRectangle rectangle() {
        return new BlockUtil.FoundRectangle(bottom, width, height);
    }

    static @Nullable PortalFrame find(BlockGetter level, BlockPos pos, Direction.Axis axis, boolean lit) {
        PortalShape shape = PortalShape.findAnyShape(new ShapeView(level, axis, lit), pos, axis);
        if (!shape.isValid()) return null;

        PortalShapeAccess access = (PortalShapeAccess) shape;
        int width = access.mushroom$width();
        int height = access.mushroom$height();
        Direction scan = access.mushroom$right();
        BlockPos bottom = access.mushroom$bottomLeft();
        if (scan.getAxisDirection() == Direction.AxisDirection.NEGATIVE) bottom = bottom.relative(scan, width - 1);
        PortalFrame frame = new PortalFrame(bottom, axis, width, height);
        int crying = 0;
        for (int x = -1; x <= width; x++) {
            for (int y = -1; y <= height; y++) {
                boolean side = x == -1 || x == width;
                boolean cap = y == -1 || y == height;
                if (side && cap) continue;
                BlockState state = level.getBlockState(frame.at(x, y));
                if (side || cap) {
                    if (state.is(Blocks.CRYING_OBSIDIAN)) crying++;
                    else if (!state.is(Blocks.OBSIDIAN)) return null;
                } else if (lit ? !state.is(MushroomDimension.PORTAL.get()) || state.getValue(MushroomPortalBlock.AXIS) != axis
                        : !state.isAir() && !state.is(BlockTags.FIRE)) {
                    return null;
                }
            }
        }
        return crying == 1 ? frame : null;
    }

    private record ShapeView(BlockGetter level, Direction.Axis axis, boolean lit) implements BlockGetter {
        @Override
        public BlockState getBlockState(BlockPos pos) {
            BlockState state = level.getBlockState(pos);
            if (state.is(Blocks.CRYING_OBSIDIAN)) return Blocks.OBSIDIAN.defaultBlockState();
            if (lit && state.is(MushroomDimension.PORTAL.get()) && state.getValue(MushroomPortalBlock.AXIS) == axis) {
                return Blocks.NETHER_PORTAL.defaultBlockState();
            }
            return state;
        }

        @Override public FluidState getFluidState(BlockPos pos) { return level.getFluidState(pos); }
        @Override public @Nullable BlockEntity getBlockEntity(BlockPos pos) { return level.getBlockEntity(pos); }
        @Override public int getHeight() { return level.getHeight(); }
        @Override public int getMinY() { return level.getMinY(); }
    }
}
