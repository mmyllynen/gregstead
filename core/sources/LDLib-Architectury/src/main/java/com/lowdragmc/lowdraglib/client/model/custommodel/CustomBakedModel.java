package com.lowdragmc.lowdraglib.client.model.custommodel;

import com.lowdragmc.lowdraglib.client.bakedpipeline.Quad;
import com.lowdragmc.lowdraglib.client.bakedpipeline.Submap;
import com.lowdragmc.lowdraglib.client.model.ModelFactory;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.util.TriState;
import net.minecraft.world.level.block.state.BlockState;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

/** Native CTM/emissive wrapper with immutable quad snapshots per selected child part. */
public class CustomBakedModel<T extends BlockStateModel> implements BlockStateModel {
    private final T originalModel;
    public CustomBakedModel(T parent) { this.originalModel = Objects.requireNonNull(parent); }

    public T child() { return originalModel; }

    @Override
    public void collectParts(RandomSource random, List<BlockStateModelPart> output) {
        // Original context-free behavior returns the parent unchanged.
        originalModel.collectParts(random, output);
    }

    @Override
    public void collectParts(BlockAndTintGetter level, BlockPos pos, BlockState state,
                             RandomSource random, List<BlockStateModelPart> output) {
        var selected = new ArrayList<BlockStateModelPart>();
        originalModel.collectParts(level, pos, state, random, selected);
        var connections = new EnumMap<Direction, Connections>(Direction.class);
        for (Direction side : Direction.values()) connections.put(side, Connections.checkConnections(level, pos, state, side));
        var unculled = Connections.checkConnections(level, pos, state, null);
        for (BlockStateModelPart part : selected) {
            output.add(new Part(part, connections, unculled));
        }
    }

    @Override public Material.Baked particleMaterial() { return originalModel.particleMaterial(); }
    @Override public Material.Baked particleMaterial(BlockAndTintGetter level, BlockPos pos, BlockState state) {
        return originalModel.particleMaterial(level, pos, state);
    }
    // Connection sprites may be animated/translucent; conservatively retain both capabilities.
    @Override public int materialFlags() { return originalModel.materialFlags() | BakedQuad.FLAG_ANIMATED | BakedQuad.FLAG_TRANSLUCENT; }
    @Override public int materialFlags(BlockAndTintGetter level, BlockPos pos, BlockState state) {
        return originalModel.materialFlags(level, pos, state) | BakedQuad.FLAG_ANIMATED | BakedQuad.FLAG_TRANSLUCENT;
    }

    private static final class Part implements BlockStateModelPart {
        private final BlockStateModelPart original;
        private final Map<Direction, List<BakedQuad>> quads = new EnumMap<>(Direction.class);
        private final List<BakedQuad> unculled;
        private final int flags;
        Part(BlockStateModelPart original, Map<Direction, Connections> connections, Connections noSide) {
            this.original = original;
            int flags = original.materialFlags();
            unculled = List.copyOf(buildCustomQuads(noSide, original.getQuads(null), 0));
            for (BakedQuad quad : unculled) flags |= quad.materialInfo().flags();
            for (Direction side : Direction.values()) {
                var transformed = List.copyOf(buildCustomQuads(connections.get(side), original.getQuads(side), 0));
                quads.put(side, transformed);
                for (BakedQuad quad : transformed) flags |= quad.materialInfo().flags();
            }
            this.flags = flags;
        }
        public List<BakedQuad> getQuads(@Nullable Direction side) { return side == null ? unculled : quads.get(side); }
        public boolean useAmbientOcclusion() { return original.useAmbientOcclusion(); }
        public TriState ambientOcclusion() { return original.ambientOcclusion(); }
        public Material.Baked particleMaterial() { return original.particleMaterial(); }
        public int materialFlags() { return flags; }
    }

    public static List<BakedQuad> reBakeCustomQuads(List<BakedQuad> quads, BlockAndTintGetter level, BlockPos pos, @Nonnull BlockState state, @Nullable Direction side, float offset) {
        return buildCustomQuads(Connections.checkConnections(level, pos, state, side), quads, offset);
    }

    public static List<BakedQuad> buildCustomQuads(Connections connections, List<BakedQuad> base, float offset) {
        List<BakedQuad> result = new LinkedList<>();
        for (BakedQuad bakedQuad : base) {
            var section = LDLMetadataSection.getMetadata(bakedQuad.materialInfo().sprite());
            TextureAtlasSprite connection = section.connection == null ? null : ModelFactory.getBlockSprite(section.connection);
            if (connection == null) {
                result.add(makeQuad(bakedQuad, section, offset).rebake());
                continue;
            }

            Quad quad = makeQuad(bakedQuad, section, offset).derotate();
            Quad[] quads = quad.subdivide(4);

            int[] ctm = connections.getSubmapIndices();

            for (int j = 0; j < quads.length; j++) {
                Quad q = quads[j];
                if (q != null) {
                    int ctmid = q.getUvs().normalize().getQuadrant();
                    quads[j] = q.grow().transformUVs(ctm[ctmid] > 15 ? bakedQuad.materialInfo().sprite() : connection, Submap.uvs[ctm[ctmid]]);
                }
            }
            result.addAll(Arrays.stream(quads).filter(Objects::nonNull).map(Quad::rebake).toList());
        }
        return result;
    }

    protected static Quad makeQuad(BakedQuad bq, LDLMetadataSection section, float offset) {
        Quad q = Quad.from(bq, offset);
        if (section.emissive) {
            q = q.setLight(15, 15);
        }
        return q;
    }
}
