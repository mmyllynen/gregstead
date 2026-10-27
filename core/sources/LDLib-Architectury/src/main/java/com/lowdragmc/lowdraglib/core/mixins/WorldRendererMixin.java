package com.lowdragmc.lowdraglib.core.mixins;

import com.lowdragmc.lowdraglib.client.renderer.NativeBlockLightProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndLightGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.util.LightCoordsUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LightCoordsUtil.class)
public abstract class WorldRendererMixin {
    @Inject(method="getLightCoords(Lnet/minecraft/util/LightCoordsUtil$BrightnessGetter;Lnet/minecraft/world/level/BlockAndLightGetter;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;)I",
            at=@At("HEAD"), cancellable=true, require=1)
    private static void ldlib$light(LightCoordsUtil.BrightnessGetter brightness, BlockAndLightGetter level,
            BlockState state, BlockPos pos, CallbackInfoReturnable<Integer> callback) {
        if (state.getBlock() instanceof NativeBlockLightProvider provider) {
            callback.setReturnValue(provider.getLightMap(level, state, pos));
        }
    }
}
