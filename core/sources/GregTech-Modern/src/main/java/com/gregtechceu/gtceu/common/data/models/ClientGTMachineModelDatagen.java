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

public class ClientGTMachineModelDatagen {

    public static final String OVERLAY_PREFIX = "overlay_";
    public static final String EMISSIVE_SUFFIX = "_emissive";

    // spotless:off
    public static final Identifier SIDED_SIDED_OVERLAY_MODEL = GTCEu.id("block/machine/template/sided/sided");
    public static final Identifier SIDED_SINGLE_OVERLAY_MODEL = GTCEu.id("block/machine/template/sided/single");
    public static final Identifier CUBE_ALL_SIDED_OVERLAY_MODEL = GTCEu.id("block/machine/template/cube_all/sided");
    public static final Identifier CUBE_ALL_SINGLE_OVERLAY_MODEL = GTCEu.id("block/machine/template/cube_all/single");

    public static final Int2ObjectMap<Identifier> TIERED_HULL_MODELS = GTMachineModels.TIERED_HULL_MODELS;

    public static final Identifier LP_STEAM_HULL_MODEL = GTCEu.id("block/casings/steam/bricked_bronze");
    public static final Identifier HP_STEAM_HULL_MODEL = GTCEu.id("block/casings/steam/bricked_steel");

    public static final Identifier HATCH_PART_MODEL = GTCEu.id("block/machine/template/part/hatch_machine");
    public static final Identifier HATCH_PART_COLOR_RING_MODEL = GTCEu.id("block/machine/template/part/hatch_machine_color_ring");
    public static final Identifier HATCH_PART_EMISSIVE_MODEL = GTCEu.id("block/machine/template/part/hatch_machine_emissive");
    public static final Identifier HATCH_PART_EMISSIVE_COLOR_RING_MODEL = GTCEu.id("block/machine/template/part/hatch_machine_emissive_color_ring");
    // spotless:on

    // region generic models

    public static MachineBuilder.ModelInitializer createBasicMachineModel(Identifier baseModel) {
        return (ctx, prov, builder) -> {
            builder.forAllStatesModels(state -> new JsonPrimitive(baseModel.toString()));
        };
    }

    public static MachineBuilder.ModelInitializer createBasicReplaceableTextureMachineModel(Identifier baseModel) {
        return (ctx, prov, builder) -> {
            builder.forAllStatesModels(state -> new JsonPrimitive(baseModel.toString()));
            builder.addReplaceableTextures("bottom", "top", "side");
        };
    }

    public static MachineBuilder.ModelInitializer createTieredHullMachineModel(Identifier parentModel) {
        return (ctx, prov, builder) -> {
            NativeInlineModel model = new NativeInlineModel()
                    .parent(parentModel);
            tieredHullTextures(model, builder.getOwner().getTier());

            builder.forAllStatesModels(state -> model.toJson());
        };
    }

    public static MachineBuilder.ModelInitializer createOverlayTieredHullMachineModel(Identifier overlayModel) {
        return (ctx, prov, builder) -> {
            NativeInlineModel model = new NativeInlineModel()
                    .parent(overlayModel);
            tieredHullTextures(model, builder.getOwner().getTier());
            builder.forAllStatesModels(state -> model.toJson());

            builder.addReplaceableTextures("bottom", "top", "side");
        };
    }

    public static MachineBuilder.ModelInitializer createOverlayCasingMachineModel(Identifier baseCasingTexture,
                                                                                  Identifier overlayModel) {
        return (ctx, prov, builder) -> {
            NativeInlineModel model = new NativeInlineModel()
                    .parent(overlayModel);
            model.texture("all", baseCasingTexture);

            builder.forAllStatesModels(state -> model.toJson());
            builder.addReplaceableTextures("all");
        };
    }

    public static MachineBuilder.ModelInitializer createColorOverlayTieredHullMachineModel(Identifier overlay,
                                                                                           @Nullable Identifier pipeOverlay,
                                                                                           @Nullable Identifier emissiveOverlay) {
        return (ctx, prov, builder) -> {
            builder.forAllStatesModels(state -> {
                NativeInlineModel model = colorOverlayHullModel(overlay, pipeOverlay, emissiveOverlay, state);
                return tieredHullTextures(model, builder.getOwner().getTier()).toJson();
            });

            builder.addReplaceableTextures("bottom", "top", "side");
        };
    }

    public static MachineBuilder.ModelInitializer createSingleOverlayTieredHullMachineModel(Identifier overlayTexture,
                                                                                            Identifier emissiveOverlayTexture) {
        return (ctx, prov, builder) -> {
            NativeInlineModel model = new NativeInlineModel()
                    .parent(SIDED_SINGLE_OVERLAY_MODEL)
                    .texture("overlay", overlayTexture)
                    .texture("overlay_emissive", emissiveOverlayTexture);
            tieredHullTextures(model, builder.getOwner().getTier());
            builder.forAllStatesModels(state -> model.toJson());

            builder.addReplaceableTextures("bottom", "top", "side");
        };
    }

    public static MachineBuilder.ModelInitializer createWorkableTieredHullMachineModel(Identifier overlayDir) {
        return (ctx, prov, builder) -> {
            WorkableOverlays overlays = WorkableOverlays.get(overlayDir, prov.textureResources());

            builder.forAllStates(state -> {
                RecipeLogic.Status status = state.getValue(RECIPE_LOGIC_STATUS);

                NativeInlineModel model = new NativeInlineModel().parent(tieredHullModel(builder));
                return addWorkableOverlays(overlays, status, model);
            });
        };
    }

    public static MachineBuilder.ModelInitializer createOverlaySteamHullMachineModel(Identifier overlayModel) {
        return (ctx, prov, builder) -> {
            builder.forAllStatesModels(state -> {
                boolean steel = state.getOptionalValue(IS_STEEL_MACHINE).orElse(false);

                NativeInlineModel model = new NativeInlineModel()
                        .parent(overlayModel);
                steamCasingTextures(model, steel);
                return model.toJson();
            });

            builder.addReplaceableTextures("bottom", "top", "side");
        };
    }

    public static MachineBuilder.ModelInitializer createColorOverlaySteamHullMachineModel(Identifier overlay,
                                                                                          @Nullable Identifier pipeOverlay,
                                                                                          @Nullable Identifier emissiveOverlay) {
        return (ctx, prov, builder) -> {
            builder.forAllStatesModels(state -> {
                NativeInlineModel model = colorOverlayHullModel(overlay, pipeOverlay, emissiveOverlay, state);
                steamCasingTextures(model, state.getOptionalValue(IS_STEEL_MACHINE).orElse(false));
                return model.toJson();
            });

            builder.addReplaceableTextures("bottom", "top", "side");
        };
    }

    public static final Identifier VENT_OVERLAY = GTCEu.id("block/overlay/machine/overlay_steam_vent");

    // spotless:off
    public static MachineBuilder.ModelInitializer createWorkableSteamHullMachineModel(boolean highPressure, Identifier overlayDir) {
        return (ctx, prov, builder) -> {
            WorkableOverlays overlays = WorkableOverlays.get(overlayDir, prov.textureResources());
            Identifier parent = steamHullModel(highPressure);

            makeWorkableOverlayPart(builder, parent, overlays, RecipeLogic.Status.IDLE);
            makeWorkableOverlayPart(builder, parent, overlays, RecipeLogic.Status.WORKING);
            makeWorkableOverlayPart(builder, parent, overlays, RecipeLogic.Status.WAITING);
            makeWorkableOverlayPart(builder, parent, overlays, RecipeLogic.Status.SUSPEND);

            if (!builder.getOwner().defaultRenderState().hasProperty(VENT_DIRECTION)) {
                return;
            }

            for (RelativeDirection relative : RelativeDirection.VALUES) {
                Direction dir = relative.global;
                builder.part(new WeightedModelVariant(VENT_OVERLAY).rotate(
                        dir == Direction.DOWN ? 90 : dir == Direction.UP ? 270 : 0,
                        dir.getAxis().isVertical() ? 0 : ((int) dir.toYRot() + 180) % 360))
                        .condition(VENT_DIRECTION, relative);
            }
        };
    }
    // spotless:on

    private static void makeWorkableOverlayPart(MachineModelBuilder builder, Identifier parentModel,
                                                WorkableOverlays overlays, RecipeLogic.Status status) {
        NativeInlineModel model = new NativeInlineModel().parent(parentModel);
        addWorkableOverlays(overlays, status, model);
        builder.part(new WeightedModelVariant(model.toJson(), 0, 0, false, 1)).condition(RECIPE_LOGIC_STATUS, status);
    }

    public static MachineBuilder.ModelInitializer createWorkableCasingMachineModel(Identifier baseCasingTexture,
                                                                                   Identifier overlayDir) {
        return (ctx, prov, builder) -> {
            WorkableOverlays overlays = WorkableOverlays.get(overlayDir, prov.textureResources());

            builder.forAllStates(state -> {
                RecipeLogic.Status status = state.getValue(RECIPE_LOGIC_STATUS);

                NativeInlineModel model = new NativeInlineModel()
                        .parent(CUBE_ALL_SIDED_OVERLAY_MODEL)
                        .texture("all", baseCasingTexture);
                return addWorkableOverlays(overlays, status, model);
            });
            builder.addTextureOverride("all", baseCasingTexture);
        };
    }

    // spotless:off
    public static MachineBuilder.ModelInitializer createSidedOverlayCasingMachineModel(Identifier baseCasingTexture,
                                                                                       Identifier overlayModel) {
        return (ctx, prov, builder) -> {
            NativeInlineModel model = new NativeInlineModel()
                    .parent(overlayModel);
            casingTextures(model, baseCasingTexture);
            builder.forAllStatesModels(state -> model.toJson());

            builder.addReplaceableTextures("bottom", "top", "side");
        };
    }

    public static MachineBuilder.ModelInitializer createSidedWorkableCasingMachineModel(Identifier baseCasingTexture,
                                                                                        Identifier overlayDir) {
        return (ctx, prov, builder) -> {
            WorkableOverlays overlays = WorkableOverlays.get(overlayDir, prov.textureResources());

            builder.forAllStates(state -> {
                RecipeLogic.Status status = state.getValue(RECIPE_LOGIC_STATUS);

                NativeInlineModel model = new NativeInlineModel()
                        .parent(SIDED_SIDED_OVERLAY_MODEL);
                casingTextures(model, baseCasingTexture);
                return addWorkableOverlays(overlays, status, model);
            });

            var texturePath = baseCasingTexture;
            if (!texturePath.getPath().endsWith("/")) {
                texturePath = texturePath.withSuffix("/");
            }
            builder.addTextureOverride("bottom", texturePath.withSuffix("bottom"));
            builder.addTextureOverride("top", texturePath.withSuffix("top"));
            builder.addTextureOverride("side", texturePath.withSuffix("side"));
        };
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
        return (ctx, prov, builder) -> {
            WorkableOverlays overlays = WorkableOverlays.get(overlayDir, prov.textureResources());

            builder.forAllStatesModels(state -> {
                RecipeLogic.Status status = state.getValue(RECIPE_LOGIC_STATUS);

                NativeInlineModel model = new NativeInlineModel().parent(GENERATOR_MODEL);
                tieredHullTextures(model, builder.getOwner().getTier());
                addWorkableOverlays(overlays, status, model);

                return model.toJson();
            });
        };
    }

    public static MachineBuilder.ModelInitializer createBatteryBufferModel(int inventorySize) {
        return (ctx, prov, builder) -> {
            var overlay = OUT_OVERLAYS_FOR_AMP.get(inventorySize);

            NativeInlineModel model = new NativeInlineModel()
                    .parent(TRANSFORMER_LIKE)
                    .texture("overlay_in_io", overlay.getIoPart())
                    .texture("overlay_in_tinted", overlay.getTintedPart())
                    .texture("overlay_out_io", BLANK_TEXTURE);
            tieredHullTextures(model, builder.getOwner().getTier());

            builder.forAllStatesModels(state -> model.toJson());
        };
    }

    // spotless:off
    public static final Identifier CHARGER_IDLE = GTCEu.id("block/machines/charger/overlay_charger_idle");
    public static final Identifier CHARGER_RUNNING = GTCEu.id("block/machines/charger/overlay_charger_running");
    public static final Identifier CHARGER_RUNNING_EMISSIVE = GTCEu.id("block/machines/charger/overlay_charger_running_emissive");
    public static final Identifier CHARGER_FINISHED = GTCEu.id("block/machines/charger/overlay_charger_finished");
    public static final Identifier CHARGER_FINISHED_EMISSIVE = GTCEu.id("block/machines/charger/overlay_charger_finished_emissive");
    
    public static MachineBuilder.ModelInitializer createChargerModel() {
        return (ctx, prov, builder) -> {
            builder.forAllStatesModels(renderState -> {
                ChargerMachine.State state = renderState.getValue(CHARGER_STATE);

                NativeInlineModel model = new NativeInlineModel()
                        .parent(SIDED_SIDED_OVERLAY_MODEL);
                tieredHullTextures(model, builder.getOwner().getTier());

                switch (state) {
                    case IDLE -> {
                        model.texture("overlay_front", CHARGER_IDLE);
                    }
                    case RUNNING -> {
                        model.texture("overlay_front", CHARGER_RUNNING);
                        model.texture("overlay_front_emissive", CHARGER_RUNNING_EMISSIVE);
                    }
                    case FINISHED -> {
                        model.texture("overlay_front", CHARGER_FINISHED);
                        model.texture("overlay_front_emissive", CHARGER_FINISHED_EMISSIVE);
                    }
                }
                return model.toJson();
            });
        };
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
        return (ctx, prov, builder) -> {
            final EnergyIOOverlay energyIn = IN_OVERLAYS_FOR_AMP.get(amperage);
            final EnergyIOOverlay energyOut = OUT_OVERLAYS_FOR_AMP.get(amperage);

            NativeInlineModel euToFeModel = new NativeInlineModel()
                    .parent(TRANSFORMER_LIKE)
                    .texture("overlay_in_io", energyIn.getIoPart())
                    .texture("overlay_in_tinted", energyIn.getTintedPart())
                    .texture("overlay_in_io_emissive", energyIn.getIoPartEmissive())
                    .texture("overlay_out_io_emissive", CONVERTER_FE_OUT_EMISSIVE)
                    .texture("overlay_out_io", CONVERTER_FE_OUT);
            tieredHullTextures(euToFeModel, builder.getOwner().getTier());

            NativeInlineModel feToEuModel = new NativeInlineModel()
                    .parent(TRANSFORMER_LIKE)
                    .texture("overlay_in_io", energyOut.getIoPart())
                    .texture("overlay_in_tinted", energyOut.getTintedPart())
                    .texture("overlay_in_io_emissive", energyOut.getIoPartEmissive())
                    .texture("overlay_out_io_emissive", CONVERTER_FE_IN_EMISSIVE)
                    .texture("overlay_out_io", CONVERTER_FE_IN);
            tieredHullTextures(feToEuModel, builder.getOwner().getTier());

            builder.partialState()
                    .with(IS_FE_TO_EU, false)
                    .setModels(new WeightedModelVariant(euToFeModel.toJson(), 0, 0, false, 1))
                    .partialState()
                    .with(IS_FE_TO_EU, true)
                    .setModels(new WeightedModelVariant(feToEuModel.toJson(), 0, 0, false, 1));
        };
    }

    public static MachineBuilder.ModelInitializer createCrateModel(boolean wooden) {
        return (ctx, prov, builder) -> {
            String modelPath = "block/machine/template/crate/" + (wooden ? "wooden" : "metal") + "_crate";
            Identifier baseModel = GTCEu.id(modelPath);
            Identifier tapedModel = GTCEu.id(modelPath + "_taped");

            builder.forAllStatesModels(state -> {
                if (state.getOptionalValue(IS_TAPED).orElse(false)) {
                    return new JsonPrimitive(tapedModel.toString());
                } else {
                    return new JsonPrimitive(baseModel.toString());
                }
            });
        };
    }

    public static MachineBuilder.ModelInitializer createDiodeModel() {
        return (ctx, prov, builder) -> {
            builder.forAllStatesModels(renderState -> {
                DiodePartMachine.AmpMode mode = renderState.getValue(DIODE_AMP_MODE);
                final EnergyIOOverlay energyIn = IN_OVERLAYS_FOR_AMP.get(mode.getAmpValue());
                final EnergyIOOverlay energyOut = OUT_OVERLAYS_FOR_AMP.get(mode.getAmpValue());

                NativeInlineModel model = new NativeInlineModel()
                        .parent(TRANSFORMER_LIKE)
                        .texture("overlay_in_io_emissive", energyIn.getIoPartEmissive())
                        .texture("overlay_in_io", energyIn.getIoPart())
                        .texture("overlay_in_tinted", energyIn.getTintedPart())
                        .texture("overlay_out_io_emissive", energyOut.getIoPartEmissive())
                        .texture("overlay_out_io", energyOut.getIoPart())
                        .texture("overlay_out_tinted", energyOut.getTintedPart());
                tieredHullTextures(model, builder.getOwner().getTier());
                return model.toJson();
            });

            builder.addReplaceableTextures("bottom", "top", "side");
        };
    }

    public static MachineBuilder.ModelInitializer createTransformerModel(int baseAmp) {
        return (ctx, prov, builder) -> {
            builder.forAllStatesModels(renderState -> {
                boolean transformUp = renderState.getValue(IS_TRANSFORM_UP);
                EnergyIOOverlay frontFace = (transformUp ? OUT_OVERLAYS_FOR_AMP : IN_OVERLAYS_FOR_AMP)
                        .get(baseAmp);
                EnergyIOOverlay otherFace = (transformUp ? IN_OVERLAYS_FOR_AMP : OUT_OVERLAYS_FOR_AMP)
                        .get(baseAmp * 4);

                NativeInlineModel model = new NativeInlineModel()
                        .parent(TRANSFORMER_LIKE)
                        .texture("overlay_in_io", frontFace.getIoPart())
                        .texture("overlay_in_io_emissive", frontFace.getIoPartEmissive())
                        .texture("overlay_in_tinted", frontFace.getTintedPart())
                        .texture("overlay_out_io_emissive", otherFace.getIoPartEmissive())
                        .texture("overlay_out_tinted", otherFace.getTintedPart());
                tieredHullTextures(model, builder.getOwner().getTier());
                return model.toJson();
            });
        };
    }

    // spotless:off
    public static final Identifier ROTOR_HOLDER_BLOCK = GTCEu.id("block/machine/template/rotor_holder/block");
    public static final Identifier ROTOR_HOLDER_OVERLAY = GTCEu.id("block/machine/template/rotor_holder/overlay");
    public static final Identifier ROTOR_HOLDER_ROTOR_IDLE = GTCEu.id("block/machine/template/rotor_holder/rotor_idle");
    public static final Identifier ROTOR_HOLDER_ROTOR_SPINNING = GTCEu.id("block/machine/template/rotor_holder/rotor_spinning");
    // spotless:on

    public static MachineBuilder.ModelInitializer createRotorHolderModel() {
        return (ctx, prov, builder) -> {
                        var blockModel = new NativeInlineModel()
                    .parent(ROTOR_HOLDER_BLOCK);
            tieredHullTextures(blockModel, builder.getOwner().getTier());

            builder.part(new WeightedModelVariant(blockModel.toJson(), 0, 0, false, 1)).end();
            builder.part(ROTOR_HOLDER_OVERLAY).condition(IS_FORMED, true).end();

            makeRotorHolderState(builder, ROTOR_HOLDER_ROTOR_IDLE, false, false);
            makeRotorHolderState(builder, ROTOR_HOLDER_ROTOR_IDLE.withSuffix(EMISSIVE_SUFFIX), false, true);
            makeRotorHolderState(builder, ROTOR_HOLDER_ROTOR_SPINNING, true, false);
            makeRotorHolderState(builder, ROTOR_HOLDER_ROTOR_SPINNING.withSuffix(EMISSIVE_SUFFIX), true, true);

            builder.addReplaceableTextures("bottom", "top", "side");
        };
    }

    private static void makeRotorHolderState(MachineModelBuilder builder,
                                             Identifier model,
                                             boolean spinning, boolean emissive) {
        builder.partialState()
                .with(IS_FORMED, true)
                .with(HAS_ROTOR, true)
                .with(IS_ROTOR_SPINNING, spinning)
                .with(IS_EMISSIVE_ROTOR, emissive)
                .setModel(model);
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
        return (ctx, prov, builder) -> {
            WorkableOverlays rtOverlays = WorkableOverlays.get(rtModeModelPath, prov.textureResources());
            WorkableOverlays beOverlays = WorkableOverlays.get(beModeModelPath, prov.textureResources());

            builder.forAllStates(state -> {
                boolean rtMode = state.getValue(IS_RANDOM_TICK_MODE);
                WorkableOverlays overlays = rtMode ? rtOverlays : beOverlays;

                boolean active = state.getValue(IS_ACTIVE);
                boolean workingEnabled = state.getValue(IS_WORKING_ENABLED);
                RecipeLogic.Status status = active ?
                        workingEnabled ?
                                RecipeLogic.Status.WORKING :
                                RecipeLogic.Status.SUSPEND :
                        RecipeLogic.Status.IDLE;

                NativeInlineModel model = new NativeInlineModel()
                        .parent(SIDED_SIDED_OVERLAY_MODEL);
                tieredHullTextures(model, builder.getOwner().getTier());

                return addWorkableOverlays(overlays, status, model);
            });
        };
    }

    public static final Identifier MAINTENANCE_TAPED_OVERLAY = GTCEu.id("block/overlay/machine/overlay_maintenance_taped");
    // spotless:on

    public static MachineBuilder.ModelInitializer createMaintenanceModel(Identifier overlayModel) {
        return (ctx, prov, builder) -> {
            builder.forAllStatesModels(state -> {
                var baseModel = new NativeInlineModel()
                        .parent(overlayModel);
                tieredHullTextures(baseModel, builder.getOwner().getTier());

                if (state.getValue(IS_TAPED)) {
                    baseModel.texture("overlay_2", MAINTENANCE_TAPED_OVERLAY);
                }
                return baseModel.toJson();
            });

            builder.addReplaceableTextures("bottom", "top", "side");
        };
    }

    // spotless:off
    public static final Identifier HPCA_PART_MODEL = GTCEu.id("block/machine/template/part/hpca_part_machine");
    public static final Identifier COMPUTER_CASING_TEXTURE = GTCEu.id("block/casings/hpca/computer_casing/");
    public static final Identifier ADVANCED_COMPUTER_CASING_TEXTURE = GTCEu.id("block/casings/hpca/advanced_computer_casing/");

    public static MachineBuilder.ModelInitializer createHPCAPartModel(boolean advanced,
                                                                      Identifier normalTexture,
                                                                      Identifier damagedTexture) {
        return (ctx, prov, builder) -> {
            Identifier textures = advanced ? ADVANCED_COMPUTER_CASING_TEXTURE : COMPUTER_CASING_TEXTURE;
            HPCAOverlay overlay = HPCAOverlay.get(normalTexture, damagedTexture, prov.textureResources());

            var baseModel = new NativeInlineModel().parent(HPCA_PART_MODEL);
            casingTexture(baseModel, "bottom", textures);
            casingTexture(baseModel, "top", textures);
            casingTexture(baseModel, "front", textures);
            casingTexture(baseModel, "back", textures);
            casingTexture(baseModel, "side", textures);

            Identifier baseModelId = baseModel.emit(ctx.getId().withPrefix("block/").withSuffix("_base"), prov.modelOutput);
            builder.forAllStatesModels(state -> {
                boolean damaged = state.getValue(IS_HPCA_PART_DAMAGED);
                boolean active = state.getValue(IS_ACTIVE);

                return new NativeInlineModel().parent(baseModelId)
                        .texture("overlay", overlay.getTexture(active, damaged))
                        .texture("overlay_emissive", overlay.getEmissiveTexture(active, damaged)).toJson();
            });
        };
    }

    public static final Identifier OVERLAY_SCREEN_TEXTURE = GTCEu.id("block/overlay/machine/overlay_screen");
    public static final Identifier OVERLAY_QTANK_EMISSIVE_TEXTURE = GTCEu.id("block/overlay/machine/overlay_qtank_emissive");

    public static MachineBuilder.ModelInitializer createFisherModel() {
        return (ctx, prov, builder) -> {
            NativeInlineModel model = new NativeInlineModel()
                    .parent(GTCEu.id("block/overlay/2_layer/top_emissive"))
                    .texture("overlay", OVERLAY_SCREEN_TEXTURE)
                    .texture("overlay_emissive", OVERLAY_QTANK_EMISSIVE_TEXTURE);
            tieredHullTextures(model, builder.getOwner().getTier());

            builder.forAllStatesModels(state -> model.toJson());
        };
    }

    public static MachineBuilder.ModelInitializer createItemCollectorModel(Identifier overlayDir) {
        return (ctx, prov, builder) -> {
            WorkableOverlays overlays = WorkableOverlays.get(overlayDir, prov.textureResources());

            builder.forAllStates(state -> {
                boolean active = state.getValue(IS_ACTIVE);
                boolean workingEnabled = state.getValue(IS_WORKING_ENABLED);
                RecipeLogic.Status status = active ?
                                            workingEnabled ?
                                            RecipeLogic.Status.WORKING :
                                            RecipeLogic.Status.SUSPEND :
                                            RecipeLogic.Status.IDLE;

                NativeInlineModel model = new NativeInlineModel()
                        .parent(SIDED_SIDED_OVERLAY_MODEL);
                tieredHullTextures(model, builder.getOwner().getTier());

                return addWorkableOverlays(overlays, status, model);
            });
        };
    }

    // endregion

    // region helper functions

    public static NonNullBiConsumer<DataGenContext<Block, ? extends Block>, GTBlockstateProvider> createMachineModel(MachineBuilder.ModelInitializer modelInitializer) {
        return (ctx, prov) -> {
            Block block = ctx.getEntry();
            if (!(block instanceof MetaMachineBlock machineBlock)) {
                throw new IllegalArgumentException(
                        "passed block must be a machine block, is " + block.getClass().getName());
            }
            MachineDefinition definition = machineBlock.getDefinition();

            Identifier modelLocation = ctx.getId().withPrefix("block/machine/");
            MachineModelBuilder builder = MachineModelBuilder.begin(definition).get();
            modelInitializer.configureModel(ctx, prov, builder);
            Identifier model = prov.getBuilder().parent(Identifier.withDefaultNamespace("block/block"))
                    .customLoader(() -> builder, ignored -> {}).build(modelLocation);

            var generator = prov.multiVariantGenerator(block,
                    new Variant(model));
            PropertyDispatch<VariantMutator> dispatch = GTBlockstateProvider.createFacingDispatch(
                    definition.getRotationState(), definition.isAllowExtendedFacing());
            if (dispatch != null) {
                generator = generator.with(dispatch);
            }
            prov.addVanillaGenerator(block, generator);
        };
    }
    // spotless:on

    public static WeightedModelVariant[] addWorkableOverlays(WorkableOverlays overlays, RecipeLogic.Status status,
                                                        NativeInlineModel model) {
        for (var entry : overlays.getTextures().entrySet()) {
            var face = entry.getKey();
            var textures = entry.getValue();

            Identifier overlay = textures.getTexture(status);
            Identifier overlayEmissive = textures.getEmissiveTexture(status);

            if (overlay != BLANK_TEXTURE) {
                model.texture(OVERLAY_PREFIX + face.getName(), overlay);
            }
            if (overlayEmissive != BLANK_TEXTURE) {
                model.texture(OVERLAY_PREFIX + face.getName() + EMISSIVE_SUFFIX, overlayEmissive);
            }
        }
        return new WeightedModelVariant[] {
                new WeightedModelVariant(model.toJson(), 0, 0, false, 1) };
    }

    public static NativeInlineModel colorOverlayHullModel(Identifier overlay,
                                                          @Nullable Identifier pipeOverlay,
                                                          @Nullable Identifier emissiveOverlay,
                                                          MachineRenderState state) {
        Identifier parent;
        if (state.getOptionalValue(IS_PAINTED).orElse(false)) {
            parent = emissiveOverlay != null ? HATCH_PART_EMISSIVE_COLOR_RING_MODEL : HATCH_PART_COLOR_RING_MODEL;
        } else {
            parent = emissiveOverlay != null ? HATCH_PART_EMISSIVE_MODEL : HATCH_PART_MODEL;
        }
        NativeInlineModel model = new NativeInlineModel().parent(parent)
                .texture("overlay", overlay);
        if (emissiveOverlay != null) {
            model.texture("overlay_emissive", emissiveOverlay);
        }
        if (pipeOverlay != null) {
            model.texture("overlay_pipe", pipeOverlay);
        }
        return model;
    }

    public static Identifier tieredHullModel(MachineModelBuilder builder) {
        return tieredHullModel(builder.getOwner().getTier());
    }

    public static Identifier tieredHullModel(int tier) {
        return TIERED_HULL_MODELS.get(tier);
    }

    public static Identifier steamHullModel(boolean highPressure) {
        return highPressure ? HP_STEAM_HULL_MODEL : LP_STEAM_HULL_MODEL;
    }

    public static Identifier getTieredHullTexture(int tier) {
        return GTCEu.id("block/casings/voltage/%s/".formatted(GTValues.VN[tier].toLowerCase(Locale.ROOT)));
    }

    public static NativeInlineModel tieredHullTextures(NativeInlineModel model, int tier) {
        return casingTextures(model, getTieredHullTexture(tier));
    }

    public static Identifier getSteamCasingTexture(boolean steel) {
        return GTCEu.id("block/casings/steam/%s/".formatted(steel ? "steel" : "bronze"));
    }

    public static NativeInlineModel steamCasingTextures(NativeInlineModel model, boolean steel) {
        return casingTextures(model, getSteamCasingTexture(steel));
    }

    public static void casingTexture(NativeInlineModel model, String key, Identifier texturePath) {
        model.texture(key, texturePath.withSuffix(key));
    }

    public static NativeInlineModel casingTextures(NativeInlineModel model, Identifier texturePath) {
        if (!texturePath.getPath().endsWith("/")) {
            texturePath = texturePath.withSuffix("/");
        }
        casingTexture(model, "bottom", texturePath);
        casingTexture(model, "top", texturePath);
        casingTexture(model, "side", texturePath);
        return model;
    }

    // endregion
    @SuppressWarnings({"rawtypes", "unchecked"})
    public static void execute(NonNullBiConsumer callback, Object context, Object provider) {
        callback.accept(context, provider);
    }

    public static void execute(MachineBuilder.ModelInitializer initializer, Object context, Object provider, Object builder) {
        initializer.configureModel((DataGenContext<Block, ? extends Block>) context,
                (GTBlockstateProvider) provider, (MachineModelBuilder) builder);
    }

    public static void executeThen(MachineBuilder.ModelInitializer initializer, java.util.function.Consumer<MachineModelBuilder> after,
                                   Object context, Object provider, Object builder) {
        execute(initializer, context, provider, builder);
        after.accept((MachineModelBuilder) builder);
    }
    public static void executeComposed(MachineBuilder.ModelInitializer initializer, java.util.function.UnaryOperator<MachineModelBuilder> before,
                                       Object context, Object provider, Object builder) {
        execute(initializer, context, provider, before.apply((MachineModelBuilder) builder));
    }

    public static void formedLargeMiner(Object value) {
        ((MachineModelBuilder) value).replaceForAllStates((state, models) -> {
            if (!state.getValue(IS_FORMED)) return models;
            for (int i = 0; i < models.length; i++) models[i] = models[i].withInlineParent(GTCEu.id("block/machine/large_miner_active"));
            return models;
        });
    }
    public static void replaceableCasingTextures(Object value) {
        ((MachineModelBuilder) value).addReplaceableTextures("bottom", "top", "side");
    }

}
