package com.gregtechceu.gtceu.common.data;

import net.minecraft.core.Holder;
import net.minecraft.world.level.levelgen.densityfunction.DensityFunction;
import net.minecraft.world.level.levelgen.densityfunction.DensityFunctions;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

/** Native registry holder functions retaining the original ore scales and threshold. */
public final class NativeOreVeinFunctions {
    private NativeOreVeinFunctions() {}
    public static DensityFunction toggle(Holder<NormalNoise> noise) {
        // The original GT generator sampled the marker at points outside NoiseChunk.
        // Native interpolated() would add actual grid interpolation to those samples.
        return DensityFunctions.noise(noise, 1.5f, 1.5f);
    }
    public static DensityFunction ridged(Holder<NormalNoise> first, Holder<NormalNoise> second) {
        return DensityFunctions.add(DensityFunctions.constant(-0.08f), DensityFunctions.max(
                DensityFunctions.noise(first, 4.0f, 4.0f).abs(),
                DensityFunctions.noise(second, 4.0f, 4.0f).abs()));
    }
}
