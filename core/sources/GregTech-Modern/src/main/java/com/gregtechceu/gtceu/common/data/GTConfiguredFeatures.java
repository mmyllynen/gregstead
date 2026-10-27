package com.gregtechceu.gtceu.common.data;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.common.worldgen.feature.configurations.FluidSproutConfiguration;
import com.gregtechceu.gtceu.common.worldgen.feature.configurations.StoneBlobConfiguration;

import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.feature.TreeFeature;
import com.gregtechceu.gtceu.common.worldgen.feature.StoneBlobFeature;
import com.gregtechceu.gtceu.common.worldgen.feature.FluidSproutFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.BlockReplacement;
import net.minecraft.world.level.levelgen.feature.featuresize.TwoLayersFeatureSize;
import net.minecraft.world.level.levelgen.feature.foliageplacers.MegaJungleFoliagePlacer;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.trunkplacers.ForkingTrunkPlacer;
import net.minecraft.world.level.levelgen.structure.templatesystem.TagMatchTest;

public class GTConfiguredFeatures {

    public static final ResourceKey<Feature> RUBBER_TREE = ResourceKey.create(
            Registries.FEATURE, GTCEu.id("rubber_tree"));
    public static final ResourceKey<Feature> RED_GRANITE_BLOB = ResourceKey
            .create(Registries.FEATURE, GTCEu.id("red_granite_blob"));
    public static final ResourceKey<Feature> MARBLE_BLOB = ResourceKey
            .create(Registries.FEATURE, GTCEu.id("marble_blob"));
    public static final ResourceKey<Feature> RAW_OIL_SPROUT = ResourceKey
            .create(Registries.FEATURE, GTCEu.id("raw_oil_sprout"));

    public static void bootstrap(BootstrapContext<Feature> ctx) {
        ctx.register(RUBBER_TREE, new TreeFeature(
                BlockStateProvider.holderOf(
                        GTBlocks.RUBBER_LOG.get().changeNatural(GTBlocks.RUBBER_LOG.getDefaultState(), true)),
                new ForkingTrunkPlacer(5, 1, 3),
                BlockStateProvider.holderOf(GTBlocks.RUBBER_LEAVES.get()),
                new MegaJungleFoliagePlacer(ConstantInt.of(1), UniformInt.of(0, 1), 1),
                java.util.Optional.empty(), new TwoLayersFeatureSize(1, 0, 2), java.util.List.of(), true,
                BlockStateProvider.holderOf(Blocks.DIRT)));

        ctx.register(RED_GRANITE_BLOB, new StoneBlobFeature(
                new StoneBlobConfiguration(BlockReplacement.replace(new TagMatchTest(BlockTags.STONE_ORE_REPLACEABLES),
                        GTBlocks.RED_GRANITE.getDefaultState()), UniformInt.of(20, 30))));
        ctx.register(MARBLE_BLOB, new StoneBlobFeature(
                new StoneBlobConfiguration(BlockReplacement.replace(new TagMatchTest(BlockTags.STONE_ORE_REPLACEABLES),
                        GTBlocks.MARBLE.getDefaultState()), UniformInt.of(20, 30))));
        ctx.register(RAW_OIL_SPROUT, new FluidSproutFeature(
                new FluidSproutConfiguration(GTMaterials.RawOil.getFluid(), UniformInt.of(9, 13), UniformInt.of(6, 9),
                        0.4f)));

    }
}
