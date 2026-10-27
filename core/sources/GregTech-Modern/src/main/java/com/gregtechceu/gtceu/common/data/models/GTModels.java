package com.gregtechceu.gtceu.common.data.models;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.block.*;
import com.gregtechceu.gtceu.api.block.property.GTBlockStateProperties;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.fluids.GTFluid;
import com.gregtechceu.gtceu.api.fluids.store.FluidStorage;
import com.gregtechceu.gtceu.api.fluids.store.FluidStorageKey;
import com.gregtechceu.gtceu.api.machine.multiblock.IBatteryData;
import com.gregtechceu.gtceu.api.registry.registrate.provider.GTBlockstateProvider;
import com.gregtechceu.gtceu.common.block.*;
import com.gregtechceu.gtceu.core.MixinHelpers;
import com.gregtechceu.gtceu.data.pack.GTDynamicResourcePack;

import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;

import com.google.gson.JsonObject;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.generators.RegistrateBlockModelGenerator;
import com.gregtechceu.gtceu.data.model.NativeActiveModels;
import com.gregtechceu.gtceu.data.model.NativeInlineModel;
import com.gregtechceu.gtceu.data.model.NativeItemRanges;
import com.tterrag.registrate.providers.generators.RegistrateItemModelGenerator;
import com.tterrag.registrate.util.nullness.NonNullBiConsumer;

import java.io.BufferedReader;
import java.io.IOException;

public class GTModels {

    public static final Identifier BLANK_TEXTURE = GTCEu.id("block/void");

    public static final String ACTIVE_SUFFIX = "_active";

    // region BLOCK MODELS

    public static NonNullBiConsumer<DataGenContext<Block, ? extends Block>, GTBlockstateProvider> createModelBlockState(Identifier modelLocation) {
        return com.gregtechceu.gtceu.data.model.DeferredModelCallback.of((ctx, provider) -> ClientGTModelDatagen.execute(ClientGTModelDatagen.createModelBlockState(modelLocation), ctx, provider));
    }

    public static void createCrossBlockState(DataGenContext<Block, ? extends Block> ctx,
                                             RegistrateBlockModelGenerator prov) {
        ClientGTModelDatagen.createCrossBlockState(ctx, prov);
    }

    public static void cellModel(DataGenContext<Item, ? extends Item> ctx, RegistrateItemModelGenerator provider) {
        ClientGTModelDatagen.cellModel(ctx, provider);
    }
    public static <T extends Item> NonNullBiConsumer<DataGenContext<Item, T>, RegistrateItemModelGenerator> overrideModel(Identifier predicate, int modelNumber) {
        return com.gregtechceu.gtceu.data.model.DeferredModelCallback.of((ctx, provider) -> ClientGTModelDatagen.execute(ClientGTModelDatagen.<T>overrideModel(predicate, modelNumber), ctx, provider));
    }
    public static void createTextureModel(DataGenContext<Item, ? extends Item> ctx, RegistrateItemModelGenerator prov, Identifier texture) {
        ClientGTModelDatagen.createTextureModel(ctx, prov, texture);
    }
    public static void rubberTreeSaplingModel(DataGenContext<Item, BlockItem> context, RegistrateItemModelGenerator provider) {
        ClientGTModelDatagen.rubberTreeSaplingModel(context, provider);
    }

    public static NonNullBiConsumer<DataGenContext<Block, LampBlock>,
            com.tterrag.registrate.providers.generators.RegistrateBlockModelGenerator> lampModel(DyeColor color,
                                                                                                   boolean border) {
        return (ctx, prov) -> {
            Identifier off = com.gregtechceu.gtceu.data.model.NativeLampModels.model(color, border, false, false)
                    .emit(ctx.getId().withPrefix("block/").withSuffix("_off"), prov.modelOutput);
            Identifier on = com.gregtechceu.gtceu.data.model.NativeLampModels.model(color, border, true, false)
                    .emit(ctx.getId().withPrefix("block/"), prov.modelOutput);
            Identifier bloom = com.gregtechceu.gtceu.data.model.NativeLampModels.model(color, border, true, true)
                    .emit(ctx.getId().withPrefix("block/").withSuffix("_bloom"), prov.modelOutput);
            var dispatch = net.minecraft.client.data.models.blockstates.PropertyDispatch
                    .initial(LampBlock.LIGHT, GTBlockStateProperties.BLOOM)
                    .generate((lit, glowing) -> net.minecraft.client.data.models.BlockModelGenerators.variant(
                            new net.minecraft.client.renderer.block.dispatch.Variant(lit ? glowing ? bloom : on : off)));
            prov.blockStateOutput.accept(net.minecraft.client.data.models.blockstates.MultiVariantGenerator
                    .dispatch(ctx.getEntry()).with(dispatch));
        };
    }

    public static NonNullBiConsumer<DataGenContext<Block, Block>,
            com.tterrag.registrate.providers.generators.RegistrateBlockModelGenerator> randomRotatedModel(Identifier texturePath) {
        return (ctx, prov) -> {
            Identifier normal = new com.gregtechceu.gtceu.data.model.NativeInlineModel()
                    .parent(Identifier.withDefaultNamespace("block/cube_all")).texture("all", texturePath)
                    .emit(ctx.getId().withPrefix("block/"), prov.modelOutput);
            Identifier mirrored = new com.gregtechceu.gtceu.data.model.NativeInlineModel()
                    .parent(Identifier.withDefaultNamespace("block/cube_mirrored_all")).texture("all", texturePath)
                    .emit(ctx.getId().withPrefix("block/").withSuffix("_mirrored"), prov.modelOutput);
            var variants = com.gregtechceu.gtceu.data.model.NativeRandomRotations.variants(normal, mirrored);
            prov.blockStateOutput.accept(net.minecraft.client.data.models.blockstates.MultiVariantGenerator
                    .dispatch(ctx.getEntry(), variants));
        };
    }

    public static NonNullBiConsumer<DataGenContext<Block, Block>, RegistrateBlockModelGenerator> createSidedCasingModel(Identifier texture) {
        return com.gregtechceu.gtceu.data.model.DeferredModelCallback.of((ctx, provider) -> ClientGTModelDatagen.execute(ClientGTModelDatagen.createSidedCasingModel(texture), ctx, provider));
    }
    public static NonNullBiConsumer<DataGenContext<Block, ? extends Block>, GTBlockstateProvider> cubeAllModel(Identifier texture) {
        return com.gregtechceu.gtceu.data.model.DeferredModelCallback.of((ctx, provider) -> ClientGTModelDatagen.execute(ClientGTModelDatagen.cubeAllModel(texture), ctx, provider));
    }
    public static NonNullBiConsumer<DataGenContext<Block, ? extends Block>, GTBlockstateProvider> cubeAllModel(Identifier texture,boolean forceTranslucent) {
        return com.gregtechceu.gtceu.data.model.DeferredModelCallback.of((ctx, provider) -> ClientGTModelDatagen.execute(ClientGTModelDatagen.cubeAllModel(texture, forceTranslucent), ctx, provider));
    }
    public static NonNullBiConsumer<DataGenContext<Block, Block>, RegistrateBlockModelGenerator> createMachineCasingModel(String tierName) {
        return com.gregtechceu.gtceu.data.model.DeferredModelCallback.of((ctx, provider) -> ClientGTModelDatagen.execute(ClientGTModelDatagen.createMachineCasingModel(tierName), ctx, provider));
    }
    public static NonNullBiConsumer<DataGenContext<Block, Block>, RegistrateBlockModelGenerator> createHermeticCasingModel(String tierName) {
        return com.gregtechceu.gtceu.data.model.DeferredModelCallback.of((ctx, provider) -> ClientGTModelDatagen.execute(ClientGTModelDatagen.createHermeticCasingModel(tierName), ctx, provider));
    }
    public static NonNullBiConsumer<DataGenContext<Block, Block>, RegistrateBlockModelGenerator> createSteamCasingModel(String material) {
        return com.gregtechceu.gtceu.data.model.DeferredModelCallback.of((ctx, provider) -> ClientGTModelDatagen.execute(ClientGTModelDatagen.createSteamCasingModel(material), ctx, provider));
    }
    public static NonNullBiConsumer<DataGenContext<Block, CoilBlock>, RegistrateBlockModelGenerator> createCoilModel(ICoilType type) {
        return com.gregtechceu.gtceu.data.model.DeferredModelCallback.of((ctx, provider) -> ClientGTModelDatagen.execute(ClientGTModelDatagen.createCoilModel(type), ctx, provider));
    }
    public static NonNullBiConsumer<DataGenContext<Block, BatteryBlock>, RegistrateBlockModelGenerator> createBatteryBlockModel(IBatteryData data) {
        return com.gregtechceu.gtceu.data.model.DeferredModelCallback.of((ctx, provider) -> ClientGTModelDatagen.execute(ClientGTModelDatagen.createBatteryBlockModel(data), ctx, provider));
    }
    public static NonNullBiConsumer<DataGenContext<Block, FusionCasingBlock>, RegistrateBlockModelGenerator> createFusionCasingModel(IFusionCasingType type) {
        return com.gregtechceu.gtceu.data.model.DeferredModelCallback.of((ctx, provider) -> ClientGTModelDatagen.execute(ClientGTModelDatagen.createFusionCasingModel(type), ctx, provider));
    }
    public static NonNullBiConsumer<DataGenContext<Block, Block>, RegistrateBlockModelGenerator> createCleanroomFilterModel(IFilterType type) {
        return com.gregtechceu.gtceu.data.model.DeferredModelCallback.of((ctx, provider) -> ClientGTModelDatagen.execute(ClientGTModelDatagen.createCleanroomFilterModel(type), ctx, provider));
    }
    public static NonNullBiConsumer<DataGenContext<Block, ActiveBlock>, RegistrateBlockModelGenerator> createActiveModel(Identifier model) {
        return com.gregtechceu.gtceu.data.model.DeferredModelCallback.of((ctx, provider) -> ClientGTModelDatagen.execute(ClientGTModelDatagen.createActiveModel(model), ctx, provider));
    }
    public static NonNullBiConsumer<DataGenContext<Block, ActiveBlock>, RegistrateBlockModelGenerator> createFireboxModel(BoilerFireboxType type) {
        return com.gregtechceu.gtceu.data.model.DeferredModelCallback.of((ctx, provider) -> ClientGTModelDatagen.execute(ClientGTModelDatagen.createFireboxModel(type), ctx, provider));
    }



    public static void createPipeBlockModel(DataGenContext<Block, ? extends PipeBlock<?, ?, ?>> ctx,
                                            GTBlockstateProvider prov) {
        ClientGTModelDatagen.createPipeBlockModel(ctx, prov);
    }

    // endregion

    // region RUNTIME GEN

    /**
     * register fluid models for materials
     */
    public static void registerMaterialFluidModels() {
        ClientGTModelDatagen.registerMaterialFluidModels();
    }

    // endregion
}
