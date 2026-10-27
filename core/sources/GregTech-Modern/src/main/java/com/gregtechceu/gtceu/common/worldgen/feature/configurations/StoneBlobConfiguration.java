package com.gregtechceu.gtceu.common.worldgen.feature.configurations;

import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.levelgen.feature.BlockReplacement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record StoneBlobConfiguration(BlockReplacement state, IntProvider size) {

    public static final Codec<StoneBlobConfiguration> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                    BlockReplacement.CODEC.fieldOf("state").forGetter(StoneBlobConfiguration::state),
                    net.minecraft.util.valueproviders.IntProviders.codec(1, 64).fieldOf("size").forGetter(StoneBlobConfiguration::size))
                    .apply(instance, StoneBlobConfiguration::new));
}
