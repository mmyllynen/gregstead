package com.gregtechceu.gtceu.common.data.models;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.block.MetaMachineBlock;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.pattern.util.RelativeDirection;
import com.gregtechceu.gtceu.api.registry.registrate.MachineBuilder;
import com.gregtechceu.gtceu.api.registry.registrate.provider.GTBlockstateProvider;
import com.gregtechceu.gtceu.client.model.machine.MachineRenderState;
import com.gregtechceu.gtceu.client.model.machine.overlays.EnergyIOOverlay;
import com.gregtechceu.gtceu.client.model.machine.overlays.HPCAOverlay;
import com.gregtechceu.gtceu.client.model.machine.overlays.WorkableOverlays;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.machine.electric.ChargerMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.part.DiodePartMachine;
import com.gregtechceu.gtceu.data.model.builder.MachineModelBuilder;

import net.minecraft.util.Util;
import net.minecraft.core.Direction;
import net.minecraft.client.data.models.blockstates.*;
import net.minecraft.client.renderer.block.dispatch.Variant;
import net.minecraft.client.renderer.block.dispatch.VariantMutator;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import com.gregtechceu.gtceu.data.model.NativeInlineModel;
import com.gregtechceu.gtceu.data.model.builder.WeightedModelVariant;
import com.google.gson.JsonPrimitive;

import com.google.common.collect.ImmutableMap;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.util.nullness.NonNullBiConsumer;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

import static com.gregtechceu.gtceu.api.machine.property.GTMachineModelProperties.*;
import static com.gregtechceu.gtceu.client.model.machine.overlays.EnergyIOOverlay.*;
import static com.gregtechceu.gtceu.common.data.models.GTModels.*;

public class GTMachineModels {

    public static final String OVERLAY_PREFIX = "overlay_";
    public static final String EMISSIVE_SUFFIX = "_emissive";

    // spotless:off
    public static final Identifier SIDED_SIDED_OVERLAY_MODEL = GTCEu.id("block/machine/template/sided/sided");
    public static final Identifier SIDED_SINGLE_OVERLAY_MODEL = GTCEu.id("block/machine/template/sided/single");
    public static final Identifier CUBE_ALL_SIDED_OVERLAY_MODEL = GTCEu.id("block/machine/template/cube_all/sided");
    public static final Identifier CUBE_ALL_SINGLE_OVERLAY_MODEL = GTCEu.id("block/machine/template/cube_all/single");

    public static final Int2ObjectMap<Identifier> TIERED_HULL_MODELS = Util.make(new Int2ObjectOpenHashMap<>(), map -> {
        for (int tier : GTValues.ALL_TIERS) {
            String vn = GTValues.VN[tier].toLowerCase(Locale.ROOT);
            map.put(tier, GTCEu.id("block/casings/voltage/" + vn));
        }
        map.defaultReturnValue(GTCEu.id("block/casings/voltage/lv"));
    });
    public static final Identifier LP_STEAM_HULL_MODEL = GTCEu.id("block/casings/steam/bricked_bronze");
    public static final Identifier HP_STEAM_HULL_MODEL = GTCEu.id("block/casings/steam/bricked_steel");

    public static final Identifier HATCH_PART_MODEL = GTCEu.id("block/machine/template/part/hatch_machine");
    public static final Identifier HATCH_PART_COLOR_RING_MODEL = GTCEu.id("block/machine/template/part/hatch_machine_color_ring");
    public static final Identifier HATCH_PART_EMISSIVE_MODEL = GTCEu.id("block/machine/template/part/hatch_machine_emissive");
    public static final Identifier HATCH_PART_EMISSIVE_COLOR_RING_MODEL = GTCEu.id("block/machine/template/part/hatch_machine_emissive_color_ring");
    // spotless:on

    // region generic models

    public static MachineBuilder.ModelInitializer createBasicMachineModel(Identifier baseModel) {
        return new com.gregtechceu.gtceu.data.model.DeferredMachineModelInitializer((ctx, provider, builder) -> ClientGTMachineModelDatagen.execute(ClientGTMachineModelDatagen.createBasicMachineModel(baseModel), ctx, provider, builder));
    }

    public static MachineBuilder.ModelInitializer createBasicReplaceableTextureMachineModel(Identifier baseModel) {
        return new com.gregtechceu.gtceu.data.model.DeferredMachineModelInitializer((ctx, provider, builder) -> ClientGTMachineModelDatagen.execute(ClientGTMachineModelDatagen.createBasicReplaceableTextureMachineModel(baseModel), ctx, provider, builder));
    }

    public static MachineBuilder.ModelInitializer createTieredHullMachineModel(Identifier parentModel) {
        return new com.gregtechceu.gtceu.data.model.DeferredMachineModelInitializer((ctx, provider, builder) -> ClientGTMachineModelDatagen.execute(ClientGTMachineModelDatagen.createTieredHullMachineModel(parentModel), ctx, provider, builder));
    }

    public static MachineBuilder.ModelInitializer createOverlayTieredHullMachineModel(Identifier overlayModel) {
        return new com.gregtechceu.gtceu.data.model.DeferredMachineModelInitializer((ctx, provider, builder) -> ClientGTMachineModelDatagen.execute(ClientGTMachineModelDatagen.createOverlayTieredHullMachineModel(overlayModel), ctx, provider, builder));
    }

    public static MachineBuilder.ModelInitializer createOverlayCasingMachineModel(Identifier baseCasingTexture,
                                                                                  Identifier overlayModel) {
        return new com.gregtechceu.gtceu.data.model.DeferredMachineModelInitializer((ctx, provider, builder) -> ClientGTMachineModelDatagen.execute(ClientGTMachineModelDatagen.createOverlayCasingMachineModel(baseCasingTexture, overlayModel), ctx, provider, builder));
    }

    public static MachineBuilder.ModelInitializer createColorOverlayTieredHullMachineModel(Identifier overlay,
                                                                                           @Nullable Identifier pipeOverlay,
                                                                                           @Nullable Identifier emissiveOverlay) {
        return new com.gregtechceu.gtceu.data.model.DeferredMachineModelInitializer((ctx, provider, builder) -> ClientGTMachineModelDatagen.execute(ClientGTMachineModelDatagen.createColorOverlayTieredHullMachineModel(overlay, pipeOverlay, emissiveOverlay), ctx, provider, builder));
    }

    public static MachineBuilder.ModelInitializer createSingleOverlayTieredHullMachineModel(Identifier overlayTexture,
                                                                                            Identifier emissiveOverlayTexture) {
        return new com.gregtechceu.gtceu.data.model.DeferredMachineModelInitializer((ctx, provider, builder) -> ClientGTMachineModelDatagen.execute(ClientGTMachineModelDatagen.createSingleOverlayTieredHullMachineModel(overlayTexture, emissiveOverlayTexture), ctx, provider, builder));
    }

    public static MachineBuilder.ModelInitializer createWorkableTieredHullMachineModel(Identifier overlayDir) {
        return new com.gregtechceu.gtceu.data.model.DeferredMachineModelInitializer((ctx, provider, builder) -> ClientGTMachineModelDatagen.execute(ClientGTMachineModelDatagen.createWorkableTieredHullMachineModel(overlayDir), ctx, provider, builder));
    }

    public static MachineBuilder.ModelInitializer createOverlaySteamHullMachineModel(Identifier overlayModel) {
        return new com.gregtechceu.gtceu.data.model.DeferredMachineModelInitializer((ctx, provider, builder) -> ClientGTMachineModelDatagen.execute(ClientGTMachineModelDatagen.createOverlaySteamHullMachineModel(overlayModel), ctx, provider, builder));
    }

    public static MachineBuilder.ModelInitializer createColorOverlaySteamHullMachineModel(Identifier overlay,
                                                                                          @Nullable Identifier pipeOverlay,
                                                                                          @Nullable Identifier emissiveOverlay) {
        return new com.gregtechceu.gtceu.data.model.DeferredMachineModelInitializer((ctx, provider, builder) -> ClientGTMachineModelDatagen.execute(ClientGTMachineModelDatagen.createColorOverlaySteamHullMachineModel(overlay, pipeOverlay, emissiveOverlay), ctx, provider, builder));
    }

    public static final Identifier VENT_OVERLAY = GTCEu.id("block/overlay/machine/overlay_steam_vent");

    // spotless:off
    public static MachineBuilder.ModelInitializer createWorkableSteamHullMachineModel(boolean highPressure, Identifier overlayDir) {
        return new com.gregtechceu.gtceu.data.model.DeferredMachineModelInitializer((ctx, provider, builder) -> ClientGTMachineModelDatagen.execute(ClientGTMachineModelDatagen.createWorkableSteamHullMachineModel(highPressure, overlayDir), ctx, provider, builder));
    }
    // spotless:on



    public static MachineBuilder.ModelInitializer createWorkableCasingMachineModel(Identifier baseCasingTexture,
                                                                                   Identifier overlayDir) {
        return new com.gregtechceu.gtceu.data.model.DeferredMachineModelInitializer((ctx, provider, builder) -> ClientGTMachineModelDatagen.execute(ClientGTMachineModelDatagen.createWorkableCasingMachineModel(baseCasingTexture, overlayDir), ctx, provider, builder));
    }

    // spotless:off
    public static MachineBuilder.ModelInitializer createSidedOverlayCasingMachineModel(Identifier baseCasingTexture,
                                                                                       Identifier overlayModel) {
        return new com.gregtechceu.gtceu.data.model.DeferredMachineModelInitializer((ctx, provider, builder) -> ClientGTMachineModelDatagen.execute(ClientGTMachineModelDatagen.createSidedOverlayCasingMachineModel(baseCasingTexture, overlayModel), ctx, provider, builder));
    }

    public static MachineBuilder.ModelInitializer createSidedWorkableCasingMachineModel(Identifier baseCasingTexture,
                                                                                        Identifier overlayDir) {
        return new com.gregtechceu.gtceu.data.model.DeferredMachineModelInitializer((ctx, provider, builder) -> ClientGTMachineModelDatagen.execute(ClientGTMachineModelDatagen.createSidedWorkableCasingMachineModel(baseCasingTexture, overlayDir), ctx, provider, builder));
    }

    // endregion

    // region per-machine models

    public static final String OVERLAY_FLUID_HATCH_INPUT = "overlay_fluid_hatch_input";
    public static final String OVERLAY_FLUID_HATCH_OUTPUT = "overlay_fluid_hatch_output";
    public static final String OVERLAY_ITEM_HATCH_INPUT = "overlay_item_hatch_input";
    public static final String OVERLAY_ITEM_HATCH_OUTPUT = "overlay_item_hatch_output";

    // @deprecated use {@link com.gregtechceu.gtceu.common.data.models.GTMachineModels.OVERLAY_FLUID_HATCH_INPUT} or {@link com.gregtechceu.gtceu.common.data.models.GTMachineModels.OVERLAY_FLUID_HATCH_OUTPUT} instead.
    @Deprecated
    public static final String OVERLAY_FLUID_HATCH_TEX = "overlay_fluid_hatch";
    // @deprecated use {@link com.gregtechceu.gtceu.common.data.models.GTMachineModels.OVERLAY_FLUID_HATCH_INPUT} or {@link com.gregtechceu.gtceu.common.data.models.GTMachineModels.OVERLAY_FLUID_HATCH_OUTPUT} instead.
    @Deprecated
    public static final String OVERLAY_FLUID_HATCH_HALF_PX_TEX = "overlay_fluid_hatch_half_px_out";
    // @deprecated use {@link com.gregtechceu.gtceu.common.data.models.GTMachineModels.OVERLAY_ITEM_HATCH_INPUT} or {@link com.gregtechceu.gtceu.common.data.models.GTMachineModels.OVERLAY_ITEM_HATCH_OUTPUT} instead.
    @Deprecated
    public static final String OVERLAY_ITEM_HATCH = "overlay_item_hatch";

    public static final Identifier GENERATOR_MODEL = GTCEu.id("block/machine/template/generator_machine");

    public static MachineBuilder.ModelInitializer createSimpleGeneratorModel(Identifier overlayDir) {
        return new com.gregtechceu.gtceu.data.model.DeferredMachineModelInitializer((ctx, provider, builder) -> ClientGTMachineModelDatagen.execute(ClientGTMachineModelDatagen.createSimpleGeneratorModel(overlayDir), ctx, provider, builder));
    }

    public static MachineBuilder.ModelInitializer createBatteryBufferModel(int inventorySize) {
        return new com.gregtechceu.gtceu.data.model.DeferredMachineModelInitializer((ctx, provider, builder) -> ClientGTMachineModelDatagen.execute(ClientGTMachineModelDatagen.createBatteryBufferModel(inventorySize), ctx, provider, builder));
    }

    // spotless:off
    public static final Identifier CHARGER_IDLE = GTCEu.id("block/machines/charger/overlay_charger_idle");
    public static final Identifier CHARGER_RUNNING = GTCEu.id("block/machines/charger/overlay_charger_running");
    public static final Identifier CHARGER_RUNNING_EMISSIVE = GTCEu.id("block/machines/charger/overlay_charger_running_emissive");
    public static final Identifier CHARGER_FINISHED = GTCEu.id("block/machines/charger/overlay_charger_finished");
    public static final Identifier CHARGER_FINISHED_EMISSIVE = GTCEu.id("block/machines/charger/overlay_charger_finished_emissive");
    
    public static MachineBuilder.ModelInitializer createChargerModel() {
        return new com.gregtechceu.gtceu.data.model.DeferredMachineModelInitializer((ctx, provider, builder) -> ClientGTMachineModelDatagen.execute(ClientGTMachineModelDatagen.createChargerModel(), ctx, provider, builder));
    }
    // spotless:on

    public static final Identifier TRANSFORMER_LIKE = GTCEu.id("block/machine/template/transformer_like_machine");

    public static final Identifier CONVERTER_FE_IN = GTCEu.id("block/overlay/converter/converter_native_in");
    public static final Identifier CONVERTER_FE_OUT = GTCEu.id("block/overlay/converter/converter_native_out");

    public static final Identifier CONVERTER_FE_IN_EMISSIVE = GTCEu
            .id("block/overlay/converter/converter_native_in_emissive");
    public static final Identifier CONVERTER_FE_OUT_EMISSIVE = GTCEu
            .id("block/overlay/converter/converter_native_out_emissive");

    public static MachineBuilder.ModelInitializer createConverterModel(int amperage) {
        return new com.gregtechceu.gtceu.data.model.DeferredMachineModelInitializer((ctx, provider, builder) -> ClientGTMachineModelDatagen.execute(ClientGTMachineModelDatagen.createConverterModel(amperage), ctx, provider, builder));
    }

    public static MachineBuilder.ModelInitializer createCrateModel(boolean wooden) {
        return new com.gregtechceu.gtceu.data.model.DeferredMachineModelInitializer((ctx, provider, builder) -> ClientGTMachineModelDatagen.execute(ClientGTMachineModelDatagen.createCrateModel(wooden), ctx, provider, builder));
    }

    public static MachineBuilder.ModelInitializer createDiodeModel() {
        return new com.gregtechceu.gtceu.data.model.DeferredMachineModelInitializer((ctx, provider, builder) -> ClientGTMachineModelDatagen.execute(ClientGTMachineModelDatagen.createDiodeModel(), ctx, provider, builder));
    }

    public static MachineBuilder.ModelInitializer createTransformerModel(int baseAmp) {
        return new com.gregtechceu.gtceu.data.model.DeferredMachineModelInitializer((ctx, provider, builder) -> ClientGTMachineModelDatagen.execute(ClientGTMachineModelDatagen.createTransformerModel(baseAmp), ctx, provider, builder));
    }

    // spotless:off
    public static final Identifier ROTOR_HOLDER_BLOCK = GTCEu.id("block/machine/template/rotor_holder/block");
    public static final Identifier ROTOR_HOLDER_OVERLAY = GTCEu.id("block/machine/template/rotor_holder/overlay");
    public static final Identifier ROTOR_HOLDER_ROTOR_IDLE = GTCEu.id("block/machine/template/rotor_holder/rotor_idle");
    public static final Identifier ROTOR_HOLDER_ROTOR_SPINNING = GTCEu.id("block/machine/template/rotor_holder/rotor_spinning");
    // spotless:on

    public static MachineBuilder.ModelInitializer createRotorHolderModel() {
        return new com.gregtechceu.gtceu.data.model.DeferredMachineModelInitializer((ctx, provider, builder) -> ClientGTMachineModelDatagen.execute(ClientGTMachineModelDatagen.createRotorHolderModel(), ctx, provider, builder));
    }



    public static final ImmutableMap<Material, Identifier> MATERIALS_TO_CASING_TEXTURES = Util.make(() -> {
        ImmutableMap.Builder<Material, Identifier> builder = ImmutableMap.builder();
        builder.put(GTMaterials.Bronze, GTCEu.id("block/casings/solid/machine_casing_bronze_plated_bricks"));
        builder.put(GTMaterials.Invar, GTCEu.id("block/casings/solid/machine_casing_heatproof"));
        builder.put(GTMaterials.Aluminium, GTCEu.id("block/casings/solid/machine_casing_frost_proof"));
        builder.put(GTMaterials.Steel, GTCEu.id("block/casings/solid/machine_casing_solid_steel"));
        builder.put(GTMaterials.StainlessSteel, GTCEu.id("block/casings/solid/machine_casing_clean_stainless_steel"));
        builder.put(GTMaterials.Titanium, GTCEu.id("block/casings/solid/machine_casing_stable_titanium"));
        builder.put(GTMaterials.TungstenSteel, GTCEu.id("block/casings/solid/machine_casing_robust_tungstensteel"));
        builder.put(GTMaterials.Polytetrafluoroethylene, GTCEu.id("block/casings/solid/machine_casing_inert_ptfe"));
        builder.put(GTMaterials.HSSE, GTCEu.id("block/casings/solid/machine_casing_sturdy_hsse"));

        return builder.build();
    });

    // spotless:off
    public static MachineBuilder.ModelInitializer createWorldAcceleratorModel(Identifier beModeModelPath, Identifier rtModeModelPath) {
        return new com.gregtechceu.gtceu.data.model.DeferredMachineModelInitializer((ctx, provider, builder) -> ClientGTMachineModelDatagen.execute(ClientGTMachineModelDatagen.createWorldAcceleratorModel(beModeModelPath, rtModeModelPath), ctx, provider, builder));
    }

    public static final Identifier MAINTENANCE_TAPED_OVERLAY = GTCEu.id("block/overlay/machine/overlay_maintenance_taped");
    // spotless:on

    public static MachineBuilder.ModelInitializer createMaintenanceModel(Identifier overlayModel) {
        return new com.gregtechceu.gtceu.data.model.DeferredMachineModelInitializer((ctx, provider, builder) -> ClientGTMachineModelDatagen.execute(ClientGTMachineModelDatagen.createMaintenanceModel(overlayModel), ctx, provider, builder));
    }

    // spotless:off
    public static final Identifier HPCA_PART_MODEL = GTCEu.id("block/machine/template/part/hpca_part_machine");
    public static final Identifier COMPUTER_CASING_TEXTURE = GTCEu.id("block/casings/hpca/computer_casing/");
    public static final Identifier ADVANCED_COMPUTER_CASING_TEXTURE = GTCEu.id("block/casings/hpca/advanced_computer_casing/");

    public static MachineBuilder.ModelInitializer createHPCAPartModel(boolean advanced,
                                                                      Identifier normalTexture,
                                                                      Identifier damagedTexture) {
        return new com.gregtechceu.gtceu.data.model.DeferredMachineModelInitializer((ctx, provider, builder) -> ClientGTMachineModelDatagen.execute(ClientGTMachineModelDatagen.createHPCAPartModel(advanced, normalTexture, damagedTexture), ctx, provider, builder));
    }

    public static final Identifier OVERLAY_SCREEN_TEXTURE = GTCEu.id("block/overlay/machine/overlay_screen");
    public static final Identifier OVERLAY_QTANK_EMISSIVE_TEXTURE = GTCEu.id("block/overlay/machine/overlay_qtank_emissive");

    public static MachineBuilder.ModelInitializer createFisherModel() {
        return new com.gregtechceu.gtceu.data.model.DeferredMachineModelInitializer((ctx, provider, builder) -> ClientGTMachineModelDatagen.execute(ClientGTMachineModelDatagen.createFisherModel(), ctx, provider, builder));
    }

    public static MachineBuilder.ModelInitializer createItemCollectorModel(Identifier overlayDir) {
        return new com.gregtechceu.gtceu.data.model.DeferredMachineModelInitializer((ctx, provider, builder) -> ClientGTMachineModelDatagen.execute(ClientGTMachineModelDatagen.createItemCollectorModel(overlayDir), ctx, provider, builder));
    }

    // endregion

    // region helper functions

    public static NonNullBiConsumer<DataGenContext<Block, ? extends Block>, GTBlockstateProvider> createMachineModel(MachineBuilder.ModelInitializer modelInitializer) {
        return com.gregtechceu.gtceu.data.model.DeferredModelCallback.of((ctx, provider) -> ClientGTMachineModelDatagen.execute(ClientGTMachineModelDatagen.createMachineModel(modelInitializer), ctx, provider));
    }
    // spotless:on

    public static WeightedModelVariant[] addWorkableOverlays(WorkableOverlays overlays, RecipeLogic.Status status,
                                                        NativeInlineModel model) {
        return ClientGTMachineModelDatagen.addWorkableOverlays(overlays, status, model);
    }

    public static NativeInlineModel colorOverlayHullModel(Identifier overlay,
                                                          @Nullable Identifier pipeOverlay,
                                                          @Nullable Identifier emissiveOverlay,
                                                          MachineRenderState state) {
        return ClientGTMachineModelDatagen.colorOverlayHullModel(overlay, pipeOverlay, emissiveOverlay, state);
    }

    public static Identifier tieredHullModel(MachineModelBuilder builder) {
        return ClientGTMachineModelDatagen.tieredHullModel(builder);
    }

    public static Identifier tieredHullModel(int tier) {
        return ClientGTMachineModelDatagen.tieredHullModel(tier);
    }

    public static Identifier steamHullModel(boolean highPressure) {
        return ClientGTMachineModelDatagen.steamHullModel(highPressure);
    }

    public static Identifier getTieredHullTexture(int tier) {
        return ClientGTMachineModelDatagen.getTieredHullTexture(tier);
    }

    public static NativeInlineModel tieredHullTextures(NativeInlineModel model, int tier) {
        return ClientGTMachineModelDatagen.tieredHullTextures(model, tier);
    }

    public static Identifier getSteamCasingTexture(boolean steel) {
        return ClientGTMachineModelDatagen.getSteamCasingTexture(steel);
    }

    public static NativeInlineModel steamCasingTextures(NativeInlineModel model, boolean steel) {
        return ClientGTMachineModelDatagen.steamCasingTextures(model, steel);
    }

    public static void casingTexture(NativeInlineModel model, String key, Identifier texturePath) {
        ClientGTMachineModelDatagen.casingTexture(model, key, texturePath);
    }

    public static NativeInlineModel casingTextures(NativeInlineModel model, Identifier texturePath) {
        return ClientGTMachineModelDatagen.casingTextures(model, texturePath);
    }

    // endregion
    public static MachineBuilder.ModelInitializer formedLargeMiner() {
        return new com.gregtechceu.gtceu.data.model.DeferredMachineModelInitializer((ctx, provider, builder) ->
                ClientGTMachineModelDatagen.formedLargeMiner(builder));
    }
    public static MachineBuilder.ModelInitializer replaceableCasingTextures() {
        return new com.gregtechceu.gtceu.data.model.DeferredMachineModelInitializer((ctx, provider, builder) ->
                ClientGTMachineModelDatagen.replaceableCasingTextures(builder));
    }

}
