package com.lowdragmc.lowdraglib.client.renderer;

import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.util.TriState;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.model.data.ModelData;
import java.util.List;
import java.util.Set;
import java.util.EnumSet;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import com.lowdragmc.lowdraglib.client.model.NativeQuadLayers;

/** Quad/material operations shared by the renderer and native block model snapshot. */
public interface NativeBlockRenderer extends NativeBlockEntitySource {
    List<BakedQuad> renderModel(BlockAndTintGetter level, BlockPos pos, BlockState state, Direction side,
                               RandomSource random, ModelData data, ChunkSectionLayer renderType);
    default List<BakedQuad> renderModelLayer(BlockAndTintGetter level, BlockPos pos, BlockState state, Direction side,
            RandomSource random, ModelData data, ChunkSectionLayer layer) {
        return NativeQuadLayers.select(renderModel(level,pos,state,side,random,data,null), layer);
    }
    default Set<ChunkSectionLayer> getRenderTypes(BlockAndTintGetter level, BlockPos pos, BlockState state,
            RandomSource random, ModelData data) {
        long seed = random.nextLong();
        Set<ChunkSectionLayer> layers = EnumSet.noneOf(ChunkSectionLayer.class);
        NativeQuadLayers.add(layers, renderModel(level,pos,state,null,RandomSource.create(seed),data,null));
        for (Direction side : Direction.values()) {
            NativeQuadLayers.add(layers, renderModel(level,pos,state,side,RandomSource.create(seed),data,null));
        }
        return java.util.Collections.unmodifiableSet(layers);
    }
    TextureAtlasSprite getParticleTexture(BlockAndTintGetter level, BlockPos pos, ModelData data);
    TriState useAO(BlockState state, ModelData data, RenderType renderType);
    boolean reBakeCustomQuads();
    float reBakeCustomQuadsOffset();
    default boolean useBlockLight(net.minecraft.world.item.ItemStack stack) { return false; }
}
