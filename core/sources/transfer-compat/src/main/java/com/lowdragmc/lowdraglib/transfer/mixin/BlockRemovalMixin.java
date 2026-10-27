package com.lowdragmc.lowdraglib.transfer.mixin;

import com.lowdragmc.lowdraglib.transfer.world.LegacyBlockRemoval;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LevelChunk.class)
public abstract class BlockRemovalMixin {
    @Shadow @Final private Level level;

    @Inject(method = "setBlockState", at = @At(value = "INVOKE", target =
            "Lnet/minecraft/world/level/block/state/BlockState;is(Ljava/lang/Object;)Z", ordinal = 0), require = 1)
    private void gtport$beforeRemoval(BlockPos pos, BlockState replacement, int flags,
                                     CallbackInfoReturnable<BlockState> ci, @Local(ordinal = 1) BlockState previous) {
        if (!level.isClientSide() && previous.getBlock() instanceof LegacyBlockRemoval callback) {
            callback.beforeBlockReplacement(previous, level, pos, replacement, (flags & 64) != 0);
        }
    }
}
