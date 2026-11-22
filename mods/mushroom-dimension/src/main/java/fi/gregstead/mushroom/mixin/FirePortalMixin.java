package fi.gregstead.mushroom.mixin;

import fi.gregstead.mushroom.MushroomPortalBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BaseFireBlock.class)
abstract class FirePortalMixin {
    @Inject(method = "onPlace", at = @At("HEAD"), cancellable = true)
    private void mushroom$ignite(BlockState state, Level level, BlockPos pos, BlockState oldState,
                                 boolean movedByPiston, CallbackInfo ci) {
        if (!oldState.is(state.getBlock()) && MushroomPortalBlock.ignite(level, pos)) ci.cancel();
    }

    @Inject(method = "canBePlacedAt", at = @At("RETURN"), cancellable = true)
    private static void mushroom$allowFire(Level level, BlockPos pos, Direction direction, CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValue() && level.getBlockState(pos).isAir() && MushroomPortalBlock.hasUnlitFrame(level, pos)) {
            cir.setReturnValue(true);
        }
    }
}
