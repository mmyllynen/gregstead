package com.lowdragmc.lowdraglib.client.renderer;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndLightGetter;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.util.LightCoordsUtil;

/** Original renderer light policy, available without a client-only world view. */
public interface NativeBlockLightProvider {
    default int getLightMap(BlockAndLightGetter world, BlockState state, BlockPos pos) {
        if (state.emissiveRendering()) return LightCoordsUtil.FULL_BRIGHT;
        int sky = world.getBrightness(LightLayer.SKY, pos);
        int block = Math.max(world.getBrightness(LightLayer.BLOCK, pos), state.getLightEmission(world, pos));
        return LightCoordsUtil.pack(block, sky);
    }
}
