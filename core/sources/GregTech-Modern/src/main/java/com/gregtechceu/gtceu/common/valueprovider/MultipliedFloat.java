package com.gregtechceu.gtceu.common.valueprovider;

import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.FloatProvider;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.jetbrains.annotations.NotNull;

public class MultipliedFloat implements FloatProvider {

    public static final MapCodec<MultipliedFloat> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            net.minecraft.util.valueproviders.FloatProviders.CODEC.fieldOf("source").forGetter(provider -> provider.source),
            net.minecraft.util.valueproviders.FloatProviders.CODEC.fieldOf("multiplier").forGetter(provider -> provider.multiplier))
            .apply(instance, MultipliedFloat::new));

    private final FloatProvider source;
    private final FloatProvider multiplier;

    public static MultipliedFloat of(FloatProvider source, FloatProvider multiplier) {
        return new MultipliedFloat(source, multiplier);
    }

    public MultipliedFloat(FloatProvider source, FloatProvider multiplier) {
        this.source = source;
        this.multiplier = multiplier;
    }

    @Override
    public float sample(@NotNull RandomSource random) {
        return this.source.sample(random) * this.multiplier.sample(random);
    }

    @Override
    public float min() {
        float a = source.min() * multiplier.min();
        float b = source.min() * multiplier.max();
        float c = source.max() * multiplier.min();
        float d = source.max() * multiplier.max();
        return Math.min(Math.min(a, b), Math.min(c, d));
    }

    @Override
    public float max() {
        float a = source.min() * multiplier.min();
        float b = source.min() * multiplier.max();
        float c = source.max() * multiplier.min();
        float d = source.max() * multiplier.max();
        return Math.max(Math.max(a, b), Math.max(c, d));
    }

    @Override
    public @NotNull MapCodec<MultipliedFloat> codec() {
        return CODEC;
    }
}
