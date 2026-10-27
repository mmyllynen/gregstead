package com.gregtechceu.gtceu.api.data.worldgen.ores;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.AbstractOreFeature;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;
import java.util.function.Function;

/** Original vein target and air-exposure policy, with native position-aware rules. */
public final class OrePlacementRules {
    private OrePlacementRules() {}
    public static boolean canPlace(BlockState state, Function<BlockPos, BlockState> adjacent,
                                   RandomSource random, RuleTest target, BlockPos position, float discardChance) {
        if (!target.test(state, position, random)) return false;
        if (discardChance <= 0 || (!(discardChance >= 1) && random.nextFloat() >= discardChance)) return true;
        return !AbstractOreFeature.isAdjacentToAir(adjacent, position);
    }
}
