package com.lowdragmc.lowdraglib.client.model.forge;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.lowdragmc.lowdraglib.client.model.ModelFactory;
import com.lowdragmc.lowdraglib.client.model.custommodel.CustomBakedModel;
import com.lowdragmc.lowdraglib.client.renderer.NativeBlockModelProvider;
import com.lowdragmc.lowdraglib.client.renderer.NativeBlockRenderer;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.*;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.util.TriState;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.*;
import net.neoforged.neoforge.model.data.ModelData;
import java.util.*;

/** Native renderer model marker and context-owned geometry snapshots. */
public class LDLRendererModel extends AbstractUnbakedModel {
    public LDLRendererModel(StandardModelParameters parameters) { super(parameters); }

    public static final class RendererBakedModel implements BlockStateModel {
        private static Material.Baked missing() {
            return new Material.Baked(ModelFactory.getBlockSprite(MissingTextureAtlasSprite.getLocation()), false);
        }
        private static NativeBlockRenderer renderer(BlockState state) {
            return state.getBlock() instanceof NativeBlockModelProvider provider ? provider.getNativeBlockRenderer(state) : null;
        }
        @Override public void collectParts(RandomSource random, List<BlockStateModelPart> output) {}
        @Override public void collectParts(BlockAndTintGetter level, BlockPos pos, BlockState state,
                                           RandomSource random, List<BlockStateModelPart> output) {
            var renderer = renderer(state);
            if (renderer != null) output.add(new Part(renderer, level, pos, state, level.getModelData(pos), random.nextLong()));
        }
        @Override public Material.Baked particleMaterial() { return missing(); }
        @Override public Material.Baked particleMaterial(BlockAndTintGetter level, BlockPos pos, BlockState state) {
            var renderer = renderer(state);
            return renderer == null ? missing() : new Material.Baked(renderer.getParticleTexture(level, pos, level.getModelData(pos)), false);
        }
        @Override public int materialFlags() { return BakedQuad.FLAG_ANIMATED | BakedQuad.FLAG_TRANSLUCENT; }
    }

    private static final class Part implements BlockStateModelPart {
        private final Map<Direction, List<BakedQuad>> sides = new EnumMap<>(Direction.class);
        private final List<BakedQuad> unculled;
        private final Material.Baked particle;
        private final TriState ao;
        private final int flags;
        Part(NativeBlockRenderer renderer, BlockAndTintGetter level, BlockPos pos, BlockState state, ModelData data, long seed) {
            particle = new Material.Baked(renderer.getParticleTexture(level, pos, data), false);
            ao = renderer.useAO(state, data, null);
            unculled = bake(renderer, level, pos, state, null, data, seed);
            int flags = 0;
            for (BakedQuad quad : unculled) flags |= quad.materialInfo().flags();
            for (Direction side : Direction.values()) {
                var quads = bake(renderer, level, pos, state, side, data, seed);
                sides.put(side, quads);
                for (BakedQuad quad : quads) flags |= quad.materialInfo().flags();
            }
            this.flags = flags;
        }
        private static List<BakedQuad> bake(NativeBlockRenderer renderer, BlockAndTintGetter level, BlockPos pos,
                BlockState state, Direction side, ModelData data, long seed) {
            var quads = renderer.renderModel(level, pos, state, side, RandomSource.create(seed), data, null);
            if (renderer.reBakeCustomQuads()) quads = CustomBakedModel.reBakeCustomQuads(
                    quads, level, pos, state, side, renderer.reBakeCustomQuadsOffset());
            return List.copyOf(quads);
        }
        public List<BakedQuad> getQuads(Direction side) { return side == null ? unculled : sides.get(side); }
        public boolean useAmbientOcclusion() { return ao != TriState.FALSE; }
        public TriState ambientOcclusion() { return ao; }
        public Material.Baked particleMaterial() { return particle; }
        public int materialFlags() { return flags; }
    }

    public static final class Loader implements UnbakedModelLoader<LDLRendererModel> {
        public static final Loader INSTANCE = new Loader();
        private Loader() {}
        @Override public LDLRendererModel read(JsonObject json, JsonDeserializationContext context) {
            return new LDLRendererModel(StandardModelParameters.parse(json, context));
        }
    }
}
