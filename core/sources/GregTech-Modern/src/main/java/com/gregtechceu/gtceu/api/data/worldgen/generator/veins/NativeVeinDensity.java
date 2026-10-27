package com.gregtechceu.gtceu.api.data.worldgen.generator.veins;

import net.minecraft.core.BlockPos;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.densityfunction.DensityVolume;
import net.minecraft.world.level.levelgen.densityfunction.SamplerContext;

/** Native point sampling retains the real generation-region blender and its alpha/offset samplers. */
public final class NativeVeinDensity {
    private NativeVeinDensity() {}
    public static SamplerContext context(Blender blender,BlockPos min,BlockPos max) {
        var fields=ContextMap.builder().set(Blender.CONTEXT_KEY,blender);
        if(!blender.isEmpty()) {
            var volume=new DensityVolume(max.getX()-min.getX()+1,max.getY()-min.getY()+1,max.getZ()-min.getZ()+1,
                    min.getX(),min.getY(),min.getZ());
            var blended=blender.blendOffsetAndFactor(volume);
            fields.set(Blender.ALPHA_KEY,blended.createAlphaSampler()).set(Blender.OFFSET_KEY,blended.createOffsetSampler());
        }
        return SamplerContext.builder().setUserFields(fields.build()).build();
    }
}
