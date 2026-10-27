package com.lowdragmc.lowdraglib.transfer.mixin;

import com.lowdragmc.lowdraglib.transfer.world.NeighborChangeContext;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.redstone.Orientation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.minecraft.world.level.redstone.CollectingNeighborUpdater$SimpleNeighborUpdate")
public abstract class SimpleNeighborSourceMixin {
    @Unique private NeighborChangeContext.Snapshot gtport$source;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void gtport$capture(BlockPos pos, Block block, Orientation orientation, CallbackInfo ci) {
        var pending = NeighborChangeContext.pending();
        if (pending != null && pending.target().equals(pos)) gtport$source = pending;
    }

    @WrapOperation(method = "runNext", at = @At(value = "INVOKE", target =
            "Lnet/minecraft/world/level/redstone/NeighborUpdater;executeUpdate(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/Block;Lnet/minecraft/world/level/redstone/Orientation;Z)V"))
    private void gtport$restoreSource(Level level, BlockState state, BlockPos pos, Block block,
                                     Orientation orientation, boolean moved, Operation<Void> original) {
        Runnable update = () -> original.call(level, state, pos, block, orientation, moved);
        if (gtport$source != null && gtport$source.level() == level && gtport$source.target().equals(pos)) {
            NeighborChangeContext.withSource(level, pos, gtport$source.source(), update);
        } else {
            update.run();
        }
    }
}
