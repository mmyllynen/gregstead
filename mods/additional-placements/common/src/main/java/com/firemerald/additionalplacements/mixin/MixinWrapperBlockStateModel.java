package com.firemerald.additionalplacements.mixin;

import com.firemerald.additionalplacements.client.models.WrapperBlockStateModelExtension;
import org.spongepowered.asm.mixin.Pseudo;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Pseudo
@Mixin(targets = "net.fabricmc.fabric.api.client.model.loading.v1.wrapper.WrapperBlockStateModel", remap = false)
public abstract class MixinWrapperBlockStateModel implements WrapperBlockStateModelExtension {
    @Shadow
    protected BlockStateModel wrapped;

    @Override
    public BlockStateModel additionalplacements$wrapped() {
        return wrapped;
    }
}
