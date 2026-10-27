package com.lowdragmc.lowdraglib.client.model;

import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.*;
import net.minecraft.client.renderer.state.level.BlockBreakingRenderState;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.util.TriState;
import com.lowdragmc.lowdraglib.transfer.model.QuadTextureKey;
import org.joml.Vector3f;
import java.util.*;

/** Freeze contextual breaking geometry before render submission loses world/model-data access. */
public final class NativeBreakingGeometry {
    private NativeBreakingGeometry() {}
    public static void capture(BlockBreakingRenderState state, BlockAndTintGetter level, BlockStateModel model) {
        var parts = new ArrayList<BlockStateModelPart>();
        model.collectParts(level, state.blockPos(), state.blockState(), RandomSource.create(state.blockState().getSeed(state.blockPos())), parts);
        var frozen = new ArrayList<BlockStateModelPart>();
        for (var part : parts) {
            var groups = new ArrayList<List<BakedQuad>>();
            for (Direction side : Direction.values()) groups.add(copy(part.getQuads(side)));
            groups.add(copy(part.getQuads(null)));
            frozen.add(new FrozenPart(List.copyOf(groups), part.particleMaterial(), part.ambientOcclusion(), part.materialFlags()));
        }
        ((BreakingModelSnapshot) (Object) state).ldlib$breakingParts(List.copyOf(frozen));
    }
    private static List<BakedQuad> copy(List<BakedQuad> source) {
        var result = new ArrayList<BakedQuad>();
        for (var quad : source) {
            var clone = new BakedQuad(new Vector3f(quad.position0()), new Vector3f(quad.position1()),
                    new Vector3f(quad.position2()), new Vector3f(quad.position3()), quad.packedUV0(), quad.packedUV1(),
                    quad.packedUV2(), quad.packedUV3(), quad.direction(), quad.materialInfo(), quad.bakedNormals(), quad.bakedColors());
            if ((Object) quad instanceof QuadTextureKey from && (Object) clone instanceof QuadTextureKey to) to.textureKey(from.textureKey());
            result.add(clone);
        }
        return List.copyOf(result);
    }
    private record FrozenPart(List<List<BakedQuad>> groups, Material.Baked particleMaterial, TriState ambientOcclusion,
                              int materialFlags) implements BlockStateModelPart {
        public List<BakedQuad> getQuads(Direction side) { return groups.get(side == null ? 6 : side.ordinal()); }
        public boolean useAmbientOcclusion() { return ambientOcclusion != TriState.FALSE; }
    }
}
