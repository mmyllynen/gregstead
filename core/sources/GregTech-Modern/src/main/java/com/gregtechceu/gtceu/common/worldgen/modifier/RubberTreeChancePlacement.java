package com.gregtechceu.gtceu.common.worldgen.modifier;

import com.gregtechceu.gtceu.config.ConfigHolder;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.placement.RepeatingPlacement;

import com.mojang.serialization.MapCodec;
import org.jetbrains.annotations.NotNull;

public class RubberTreeChancePlacement implements RepeatingPlacement {

    public static final RubberTreeChancePlacement INSTANCE = new RubberTreeChancePlacement();
    public static final MapCodec<RubberTreeChancePlacement> CODEC = MapCodec.unit(INSTANCE);

    @Override
    public int count(RandomSource random, @NotNull BlockPos pos) {
        return random.nextFloat() < ConfigHolder.INSTANCE.worldgen.rubberTreeSpawnChance ? 1 : 0;
    }

    @Override
    public MapCodec<RubberTreeChancePlacement> codec() {
        return CODEC;
    }
}
