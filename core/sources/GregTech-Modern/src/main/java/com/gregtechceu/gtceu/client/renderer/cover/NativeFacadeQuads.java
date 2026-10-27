package com.gregtechceu.gtceu.client.renderer.cover;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.*;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.core.*;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import java.util.*;
/** Native facade child-part queries preserve contextual model data and render layers on each quad. */
public final class NativeFacadeQuads {
    private NativeFacadeQuads() {}
    public static List<BakedQuad> collect(BlockStateModel model,BlockAndTintGetter level,BlockPos pos,BlockState state,
                                          Direction side,RandomSource random) {
        var parts=new ArrayList<BlockStateModelPart>();model.collectParts(level,pos,state,random,parts);
        var quads=new ArrayList<BakedQuad>();for(var part:parts)quads.addAll(part.getQuads(side));return quads;
    }
}
