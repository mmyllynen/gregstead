package com.lowdragmc.lowdraglib.core.mixins;
import com.lowdragmc.lowdraglib.client.renderer.ATESRRendererProvider;
import com.llamalad7.mixinextras.injector.wrapoperation.*;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
@Mixin(net.minecraft.client.multiplayer.ClientLevel.class)
public abstract class NativeBerGlobalLevelMixin {
    @WrapOperation(method="onBlockEntityAdded", at=@At(value="INVOKE",target="Lnet/minecraft/client/renderer/blockentity/BlockEntityRenderer;shouldRenderOffScreen()Z"), require=1)
    private boolean ldlib$global(BlockEntityRenderer<?,?> renderer,Operation<Boolean> original,@Local(argsOnly=true) BlockEntity entity) {
        return renderer instanceof ATESRRendererProvider<?> provider ? ((ATESRRendererProvider<BlockEntity>)provider).isGlobalRenderer(entity) : original.call(renderer);
    }
}
