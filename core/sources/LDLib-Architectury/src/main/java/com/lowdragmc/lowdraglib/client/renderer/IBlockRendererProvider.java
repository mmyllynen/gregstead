package com.lowdragmc.lowdraglib.client.renderer;

import net.minecraft.client.renderer.block.dispatch.BlockModelRotation;
import net.minecraft.client.renderer.block.dispatch.ModelState;
import net.minecraft.core.BlockPos;
import net.minecraft.client.resources.model.sprite.Material;
import net.neoforged.neoforge.model.data.ModelData;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import javax.annotation.Nullable;

/**
 * Author: KilaBash
 * Date: 2022/04/21
 * Description: 
 */
public interface IBlockRendererProvider extends NativeBlockLightProvider, NativeBlockModelProvider, NativeBlockParticleProvider, com.lowdragmc.lowdraglib.client.model.custommodel.ICTMPredicate.Provider {

    @Override
    @Nullable
    default NativeBlockRenderer getNativeBlockRenderer(BlockState state) { return getRenderer(state); }

    @Override
    @Nullable
    default com.lowdragmc.lowdraglib.client.model.custommodel.ICTMPredicate getCTMPredicate(BlockState state) {
        return getRenderer(state) instanceof com.lowdragmc.lowdraglib.client.model.custommodel.ICTMPredicate predicate ? predicate : null;
    }

    @Override
    @Nullable
    default Material.Baked nativeParticleMaterial(
            BlockState state, BlockAndTintGetter level, BlockPos pos) {
        IRenderer renderer = getRenderer(state);
        if (renderer == null) return null;
        var sprite = renderer.getParticleTexture(
                level == BlockAndTintGetter.EMPTY ? null : level,
                level == BlockAndTintGetter.EMPTY ? null : pos,
                ModelData.EMPTY);
        return sprite == null ? null : new Material.Baked(sprite, false);
    }


    /**
     * Get the renderer for the block state.
     * @return return null if the block state does not have a renderer.
     */
    @Nullable
    IRenderer getRenderer(BlockState state);

    /**
     * Provide a way to modify the model state based on the block in the world.
     * you can use this to rotate the model based on the block state.
     */
    @OnlyIn(Dist.CLIENT)
    default ModelState getModelState(BlockAndTintGetter world, BlockPos pos, BlockState state) {
        return ClientBlockRendererState.identity();
    }

}
