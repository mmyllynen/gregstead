package com.gregtechceu.gtceu.core.mixins;

import com.gregtechceu.gtceu.core.interop.OreFeatureTargets;

import com.gregtechceu.gtceu.api.data.worldgen.VanillaOreDimension;
import com.gregtechceu.gtceu.config.ConfigHolder;

import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.feature.BlockReplacement;
import net.minecraft.world.level.levelgen.feature.AbstractOreFeature;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(AbstractOreFeature.class)
public class OreConfigurationMixin implements OreFeatureTargets {

    @Unique
    private static final List<Block> EXCLUDED_BLOCKS = List.of(
            Blocks.COAL_ORE, Blocks.DEEPSLATE_COAL_ORE,
            Blocks.COPPER_ORE, Blocks.DEEPSLATE_COPPER_ORE,
            Blocks.IRON_ORE, Blocks.DEEPSLATE_IRON_ORE,
            Blocks.GOLD_ORE, Blocks.DEEPSLATE_GOLD_ORE, Blocks.NETHER_GOLD_ORE,
            Blocks.DIAMOND_ORE, Blocks.DEEPSLATE_DIAMOND_ORE,
            Blocks.EMERALD_ORE, Blocks.DEEPSLATE_EMERALD_ORE,
            Blocks.LAPIS_ORE, Blocks.DEEPSLATE_LAPIS_ORE,
            Blocks.REDSTONE_ORE, Blocks.DEEPSLATE_REDSTONE_ORE,
            Blocks.NETHER_QUARTZ_ORE);

    @Unique
    private List<BlockReplacement> gtceu$filteredTargets;

    @Inject(method = "<init>(Ljava/util/List;IF)V", at = @At("RETURN"))
    private void gtceu$cacheAllowedTargets(List<BlockReplacement> targets, int size, float chance, CallbackInfo ci) {
        gtceu$filteredTargets = targets.stream()
                .filter(target -> !EXCLUDED_BLOCKS.contains(target.state().getBlock()))
                .toList();
    }

    @Override
    public List<BlockReplacement> gtceu$targetsFor(WorldGenLevel level, List<BlockReplacement> vanilla) {
        if (ConfigHolder.INSTANCE == null || !ConfigHolder.INSTANCE.worldgen.oreVeins.removeVanillaOreGen
                || VanillaOreDimension.matches(level.getLevel().dimension())) return vanilla;
        return gtceu$filteredTargets;
    }
}
