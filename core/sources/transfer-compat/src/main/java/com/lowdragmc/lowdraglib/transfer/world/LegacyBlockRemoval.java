package com.lowdragmc.lowdraglib.transfer.world;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/** Block-side removal/rotation effects that require the existing block entity. */
public interface LegacyBlockRemoval {
    void beforeBlockReplacement(BlockState previous, Level level, BlockPos pos, BlockState replacement, boolean moved);
}
