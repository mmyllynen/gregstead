package com.gregtechceu.gtceu.common.data;

import com.gregtechceu.gtceu.GTCEu;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.densityfunction.DensityFunction;
import net.minecraft.world.level.levelgen.densityfunction.DensityFunctions;
import net.minecraft.world.level.levelgen.Noises;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

public class GTWorldgen {

    public static final ResourceKey<DensityFunction> NEW_ORE_VEIN_TOGGLE = create("vein_toggle");
    public static final ResourceKey<DensityFunction> NEW_ORE_VEIN_RIDGED = create("vein_ridged");

    public static void bootstrap(BootstrapContext<DensityFunction> ctx) {
        HolderGetter<NormalNoise> noises = ctx.lookup(Registries.NOISE);

        Holder<NormalNoise> oreVeininess = noises.getOrThrow(Noises.ORE_VEININESS);
        ctx.register(NEW_ORE_VEIN_TOGGLE, NativeOreVeinFunctions.toggle(oreVeininess));

        Holder<NormalNoise> oreVeinA = noises.getOrThrow(Noises.ORE_VEIN_A);
        Holder<NormalNoise> oreVeinB = noises.getOrThrow(Noises.ORE_VEIN_B);
        ctx.register(NEW_ORE_VEIN_RIDGED, NativeOreVeinFunctions.ridged(oreVeinA, oreVeinB));
    }

    private static ResourceKey<DensityFunction> create(String path) {
        return ResourceKey.create(Registries.DENSITY_FUNCTION, GTCEu.id(path));
    }
}
