package com.gregtechceu.gtceu.common.worldgen.feature.configurations;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.material.Fluid;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record FluidSproutConfiguration(Fluid fluid, IntProvider size, IntProvider surfaceOffset, float sproutChance) {

    public static final Codec<FluidSproutConfiguration> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                    BuiltInRegistries.FLUID.byNameCodec().fieldOf("fluid").forGetter(FluidSproutConfiguration::fluid),
                    net.minecraft.util.valueproviders.IntProviders.codec(1, 64).fieldOf("size").forGetter(FluidSproutConfiguration::size),
                    net.minecraft.util.valueproviders.IntProviders.codec(0, 24).fieldOf("surface_offset")
                            .forGetter(FluidSproutConfiguration::surfaceOffset),
                    Codec.FLOAT.fieldOf("sprout_chance").forGetter(FluidSproutConfiguration::sproutChance))
                    .apply(instance, FluidSproutConfiguration::new));
}
