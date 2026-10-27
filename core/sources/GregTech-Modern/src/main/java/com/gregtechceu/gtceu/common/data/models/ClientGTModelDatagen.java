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

public class ClientGTModelDatagen {

    public static final Identifier BLANK_TEXTURE = GTCEu.id("block/void");

    public static final String ACTIVE_SUFFIX = "_active";

    // region BLOCK MODELS

    public static NonNullBiConsumer<DataGenContext<Block, ? extends Block>, GTBlockstateProvider> createModelBlockState(Identifier modelLocation) {
        return (ctx, prov) -> {
            prov.create(ctx.getEntry(), modelLocation);
        };
    }

    public static void createCrossBlockState(DataGenContext<Block, ? extends Block> ctx,
                                             RegistrateBlockModelGenerator prov) {
        emitSimple(ctx, prov, new NativeInlineModel().parent(Identifier.withDefaultNamespace("block/cross"))
                .texture("cross", ctx.getId().withPrefix("block/")));
    }

    public static void cellModel(DataGenContext<Item, ? extends Item> ctx, RegistrateItemModelGenerator provider) {
        com.gregtechceu.gtceu.data.model.NativeSpecialItemDatagen.cellModel(ctx, provider);
    }
    public static <T extends Item> NonNullBiConsumer<DataGenContext<Item, T>, RegistrateItemModelGenerator> overrideModel(Identifier predicate, int modelNumber) {
        return com.gregtechceu.gtceu.data.model.NativeSpecialItemDatagen.overrideModel(predicate, modelNumber);
    }
    public static void createTextureModel(DataGenContext<Item, ? extends Item> ctx, RegistrateItemModelGenerator prov, Identifier texture) {
        prov.generateFlatItem(ctx.getEntry(), new net.minecraft.client.resources.model.sprite.Material(texture));
    }
    public static void rubberTreeSaplingModel(DataGenContext<Item, BlockItem> context, RegistrateItemModelGenerator provider) {
        provider.generateFlatItem(context.getEntry(), new net.minecraft.client.resources.model.sprite.Material(context.getId().withPrefix("block/")));
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
        return (ctx, prov) -> emitSimple(ctx, prov, NativeActiveModels.sided(texture.withSuffix("/side"), texture.withSuffix("/bottom"), texture.withSuffix("/top")));
    }
    public static NonNullBiConsumer<DataGenContext<Block, ? extends Block>, GTBlockstateProvider> cubeAllModel(Identifier texture) {
        return cubeAllModel(texture,false);
    }
    public static NonNullBiConsumer<DataGenContext<Block, ? extends Block>, GTBlockstateProvider> cubeAllModel(Identifier texture,boolean forceTranslucent) {
        return (ctx, prov) -> emitSimple(ctx, prov, NativeActiveModels.cube(texture,forceTranslucent));
    }
    public static NonNullBiConsumer<DataGenContext<Block, Block>, RegistrateBlockModelGenerator> createMachineCasingModel(String tierName) {
        return (ctx, prov) -> emitSimple(ctx, prov, new NativeInlineModel().parent(GTCEu.id("block/cube/tinted/bottom_top"))
                .texture("bottom", GTCEu.id("block/casings/voltage/" + tierName + "/bottom"))
                .texture("top", GTCEu.id("block/casings/voltage/" + tierName + "/top"))
                .texture("side", GTCEu.id("block/casings/voltage/" + tierName + "/side")));
    }
    public static NonNullBiConsumer<DataGenContext<Block, Block>, RegistrateBlockModelGenerator> createHermeticCasingModel(String tierName) {
        return (ctx, prov) -> emitSimple(ctx, prov, new NativeInlineModel().parent(GTCEu.id("block/hermetic_casing"))
                .texture("bot_bottom", GTCEu.id("block/casings/voltage/" + tierName + "/bottom"))
                .texture("bot_top", GTCEu.id("block/casings/voltage/" + tierName + "/top"))
                .texture("bot_side", GTCEu.id("block/casings/voltage/" + tierName + "/side")));
    }
    public static NonNullBiConsumer<DataGenContext<Block, Block>, RegistrateBlockModelGenerator> createSteamCasingModel(String material) {
        return createSidedCasingModel(GTCEu.id("block/casings/steam/" + material));
    }
    public static NonNullBiConsumer<DataGenContext<Block, CoilBlock>, RegistrateBlockModelGenerator> createCoilModel(ICoilType type) {
        return (ctx, prov) -> emitActive(ctx, prov, NativeActiveModels.cube(type.getTexture()), NativeActiveModels.glowing(type.getTexture()));
    }
    public static NonNullBiConsumer<DataGenContext<Block, BatteryBlock>, RegistrateBlockModelGenerator> createBatteryBlockModel(IBatteryData data) {
        return (ctx, prov) -> emitSimple(ctx, prov, NativeActiveModels.sided(
                GTCEu.id("block/casings/battery/" + data.getBatteryName() + "/side"),
                GTCEu.id("block/casings/battery/" + data.getBatteryName() + "/top"),
                GTCEu.id("block/casings/battery/" + data.getBatteryName() + "/top")));
    }
    public static NonNullBiConsumer<DataGenContext<Block, FusionCasingBlock>, RegistrateBlockModelGenerator> createFusionCasingModel(IFusionCasingType type) {
        return (ctx, prov) -> emitActive(ctx, prov, NativeActiveModels.cube(type.getTexture()), NativeActiveModels.glowing(type.getTexture()));
    }
    public static NonNullBiConsumer<DataGenContext<Block, Block>, RegistrateBlockModelGenerator> createCleanroomFilterModel(IFilterType type) {
        return (ctx, prov) -> emitSimple(ctx, prov, NativeActiveModels.cube(GTCEu.id("block/casings/cleanroom/" + type.getSerializedName())));
    }
    public static NonNullBiConsumer<DataGenContext<Block, ActiveBlock>, RegistrateBlockModelGenerator> createActiveModel(Identifier model) {
        return (ctx, prov) -> prov.blockStateOutput.accept(NativeActiveModels.states(ctx.getEntry(), model, model.withSuffix("_active")));
    }
    public static NonNullBiConsumer<DataGenContext<Block, ActiveBlock>, RegistrateBlockModelGenerator> createFireboxModel(BoilerFireboxType type) {
        return (ctx, prov) -> emitActive(ctx, prov, NativeActiveModels.sided(type.side(), type.bottom(), type.top()),
                new NativeInlineModel().parent(GTCEu.id("block/fire_box_active"))
                        .texture("side", type.side()).texture("bottom", type.bottom()).texture("top", type.top()));
    }
    private static void emitSimple(DataGenContext<Block, ? extends Block> ctx, RegistrateBlockModelGenerator provider, NativeInlineModel model) {
        provider.create(ctx.getEntry(), model.emit(ctx.getId().withPrefix("block/"), provider.modelOutput));
    }
    private static void emitActive(DataGenContext<Block, ? extends Block> ctx, RegistrateBlockModelGenerator provider, NativeInlineModel inactive, NativeInlineModel active) {
        Identifier id = ctx.getId().withPrefix("block/");
        provider.blockStateOutput.accept(NativeActiveModels.states(ctx.getEntry(), inactive.emit(id, provider.modelOutput),
                active.emit(id.withSuffix("_active"), provider.modelOutput)));
    }

    public static void createPipeBlockModel(DataGenContext<Block, ? extends PipeBlock<?, ?, ?>> ctx,
                                            GTBlockstateProvider prov) {
        // the pipe model generator handles adding its models to the provider by itself
        ctx.getEntry().createPipeModel(prov).initModels();
    }

    // endregion

    // region RUNTIME GEN

    /**
     * register fluid models for materials
     */
    public static void registerMaterialFluidModels() {
        for (var material : GTCEuAPI.materialManager) {
            var fluidProperty = material.getProperty(PropertyKey.FLUID);
            if (fluidProperty == null) continue;

            for (FluidStorageKey key : FluidStorageKey.allKeys()) {
                FluidStorage storage = fluidProperty.getStorage();
                // fluid block models.
                FluidStorage.FluidEntry fluidEntry = storage.getEntry(key);
                if (fluidEntry != null && fluidEntry.getBuilder() != null) {
                    MixinHelpers.addFluidTexture(material, fluidEntry);
                }

                // bucket models.
                Fluid fluid = storage.get(key);
                if (fluid instanceof GTFluid gtFluid) {
                    // read the base bucket model JSON
                    JsonObject original;
                    try (BufferedReader reader = Minecraft.getInstance().getResourceManager()
                            .openAsReader(GTCEu.id("models/item/bucket/bucket.json"))) {
                        original = net.minecraft.util.LenientJsonParser.parse(reader).getAsJsonObject();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }

                    JsonObject newJson = original.deepCopy();
                    newJson.addProperty("fluid", BuiltInRegistries.FLUID.getKey(gtFluid).toString());
                    if (gtFluid.getFluidType().isLighterThanAir()) {
                        newJson.addProperty("flip_gas", true);
                    }
                    if (gtFluid.getFluidType().getLightLevel() > 0) {
                        newJson.addProperty("apply_fluid_luminosity", true);
                    }

                    JsonObject definition = new JsonObject();
                    definition.add("model", newJson);
                    GTDynamicResourcePack.addClientItem(BuiltInRegistries.ITEM.getKey(gtFluid.getBucket()),
                            net.minecraft.client.renderer.item.ClientItem.CODEC
                                    .parse(com.mojang.serialization.JsonOps.INSTANCE, definition).getOrThrow());
                }
            }
        }
    }

    // endregion
    @SuppressWarnings({"rawtypes", "unchecked"})
    public static void execute(NonNullBiConsumer callback, Object context, Object provider) {
        callback.accept(context, provider);
    }

}
