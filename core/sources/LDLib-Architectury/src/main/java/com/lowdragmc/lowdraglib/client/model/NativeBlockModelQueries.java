package com.lowdragmc.lowdraglib.client.model;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.core.*;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.neoforge.model.data.ModelData;
import java.util.*;

/** Select native parts once while preserving explicitly supplied legacy model data. */
public final class NativeBlockModelQueries {
    private NativeBlockModelQueries() {}
    public static net.minecraft.util.TriState ambientOcclusion(BlockState state) {
        var parts = parts(state, null, null, ModelData.EMPTY, RandomSource.create(0));
        if (parts.isEmpty()) return net.minecraft.util.TriState.DEFAULT;
        if (parts.stream().allMatch(part -> part.ambientOcclusion() == net.minecraft.util.TriState.FALSE)) return net.minecraft.util.TriState.FALSE;
        if (parts.stream().allMatch(part -> part.ambientOcclusion() == net.minecraft.util.TriState.TRUE)) return net.minecraft.util.TriState.TRUE;
        return net.minecraft.util.TriState.DEFAULT;
    }
    public static List<BlockStateModelPart> parts(BlockState state, BlockAndTintGetter level, BlockPos pos,
            ModelData data, RandomSource random) {
        if (state.getRenderShape() == RenderShape.INVISIBLE) return List.of();
        var model = Minecraft.getInstance().getModelManager().getBlockStateModelSet().get(state);
        var parts = new ArrayList<BlockStateModelPart>();
        if (level == null || pos == null) model.collectParts(random, parts);
        else model.collectParts(new DataView(level, pos.immutable(), Objects.requireNonNull(data)), pos, state, random, parts);
        return List.copyOf(parts);
    }
    public static List<BakedQuad> quads(BlockState state, BlockAndTintGetter level, BlockPos pos, ModelData data,
            Direction side, RandomSource random, ChunkSectionLayer layer) {
        var result = new ArrayList<BakedQuad>();
        for (var part : parts(state,level,pos,data,random)) result.addAll(part.getQuads(side));
        return NativeQuadLayers.select(result, layer);
    }
    private record DataView(BlockAndTintGetter parent, BlockPos pos, ModelData data) implements BlockAndTintGetter {
        @Override public ModelData getModelData(BlockPos at) { return at.equals(pos) ? data : parent.getModelData(at); }
        public CardinalLighting cardinalLighting() { return parent.cardinalLighting(); }
        public int getBlockTint(BlockPos at, ColorResolver color) { return parent.getBlockTint(at,color); }
        public LevelLightEngine getLightEngine() { return parent.getLightEngine(); }
        public BlockEntity getBlockEntity(BlockPos at) { return parent.getBlockEntity(at); }
        public BlockState getBlockState(BlockPos at) { return parent.getBlockState(at); }
        public FluidState getFluidState(BlockPos at) { return parent.getFluidState(at); }
        public int getHeight() { return parent.getHeight(); }
        public int getMinY() { return parent.getMinY(); }
        public int getBrightness(LightLayer layer, BlockPos at) { return parent.getBrightness(layer,at); }
        public int getRawBrightness(BlockPos at, int amount) { return parent.getRawBrightness(at,amount); }
        public boolean canSeeSky(BlockPos at) { return parent.canSeeSky(at); }
    }
}
