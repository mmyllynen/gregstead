package com.lowdragmc.lowdraglib.client.model;

import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.core.Direction;
import java.util.*;

/** Native terrain layers are carried by each quad's material, including unculled geometry. */
public final class NativeQuadLayers {
    private NativeQuadLayers() {}
    public static Set<ChunkSectionLayer> layers(BlockStateModelPart part) {
        EnumSet<ChunkSectionLayer> layers = EnumSet.noneOf(ChunkSectionLayer.class);
        add(layers, part.getQuads(null));
        for (Direction side : Direction.values()) add(layers, part.getQuads(side));
        return Collections.unmodifiableSet(layers);
    }
    public static Set<ChunkSectionLayer> layers(List<BlockStateModelPart> parts) {
        EnumSet<ChunkSectionLayer> layers = EnumSet.noneOf(ChunkSectionLayer.class);
        for (var part : parts) layers.addAll(layers(part));
        return Collections.unmodifiableSet(layers);
    }
    public static void add(Set<ChunkSectionLayer> output, List<BakedQuad> quads) {
        for (var quad : quads) output.add(quad.materialInfo().layer());
    }
    public static List<BakedQuad> select(List<BakedQuad> quads, ChunkSectionLayer layer) {
        return layer == null ? List.copyOf(quads) : quads.stream().filter(quad -> quad.materialInfo().layer() == layer).toList();
    }
}
