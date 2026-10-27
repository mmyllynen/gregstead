package com.gregtechceu.gtceu.api.data.worldgen.modifier;

import net.minecraft.core.BlockPos;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;

import com.mojang.serialization.MapCodec;

import java.util.function.Consumer;

public class FrequencyModifier implements PlacementModifier {

    public static final MapCodec<FrequencyModifier> CODEC = ExtraCodecs.POSITIVE_FLOAT.fieldOf("chance")
            .xmap(FrequencyModifier::new, (modifier) -> modifier.frequency);

    private final float frequency;

    public FrequencyModifier(float frequency) {
        this.frequency = frequency;
    }

    @Override
    public void modify(PlacementContext context, RandomSource random, BlockPos pos, Consumer<BlockPos> output) {
        int count = getCount(frequency, random);
        if (count == 0) {
            return;
        }
        output.accept(pos);
    }

    public int getCount(float frequency, RandomSource random) {
        int floored = Mth.floor(frequency);
        return floored + (random.nextFloat() < (frequency - floored) ? 1 : 0);
    }

    @Override
    public MapCodec<FrequencyModifier> codec() {
        return CODEC;
    }
}
