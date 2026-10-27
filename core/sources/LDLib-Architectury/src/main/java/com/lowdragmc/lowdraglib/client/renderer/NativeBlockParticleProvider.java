package com.lowdragmc.lowdraglib.client.renderer;

import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/** Optional native particle material supplied by a custom block renderer. */
public interface NativeBlockParticleProvider {
    @Nullable Material.Baked nativeParticleMaterial(BlockState state, BlockAndTintGetter level, BlockPos pos);
}
