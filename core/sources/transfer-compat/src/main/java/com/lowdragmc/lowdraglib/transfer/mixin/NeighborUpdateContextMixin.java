package com.lowdragmc.lowdraglib.transfer.mixin;

import com.lowdragmc.lowdraglib.transfer.world.NeighborChangeContext;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.redstone.NeighborUpdater;
import net.minecraft.world.level.redstone.Orientation;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(NeighborUpdater.class)
public interface NeighborUpdateContextMixin {
    @WrapMethod(method = "executeUpdate")
    private static void gtport$scopedUpdate(Level level, BlockState state, BlockPos pos, Block block,
                                          Orientation orientation, boolean moved, Operation<Void> original) {
        NeighborChangeContext.runUpdate(level, pos,
                () -> original.call(level, state, pos, block, orientation, moved));
    }
}
