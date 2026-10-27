package com.lowdragmc.lowdraglib.core.mixins;

import com.lowdragmc.lowdraglib.client.renderer.NativeBlockParticleProvider;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.BlockStateModelSet;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockStateModelSet.class)
public abstract class BlockModelShaperMixin {
    @Inject(method = "getParticleMaterial(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/client/renderer/block/BlockAndTintGetter;Lnet/minecraft/core/BlockPos;)Lnet/minecraft/client/resources/model/sprite/Material$Baked;",
            at = @At("HEAD"), cancellable = true, require = 1)
    private void ldlib$particleMaterial(BlockState state, BlockAndTintGetter level, BlockPos pos,
                                       CallbackInfoReturnable<Material.Baked> cir) {
        if (state.getBlock() instanceof NativeBlockParticleProvider provider) {
            Material.Baked material = provider.nativeParticleMaterial(state, level, pos);
            if (material != null) cir.setReturnValue(material);
        }
    }
}
