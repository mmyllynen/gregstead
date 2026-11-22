package com.gregtechceu.gtceu.core.interop;

import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.feature.BlockReplacement;

import java.util.List;

public interface OreFeatureTargets {

    List<BlockReplacement> gtceu$targetsFor(WorldGenLevel level, List<BlockReplacement> vanilla);
}
