package com.gregtechceu.gtceu.data.model;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.registry.registrate.provider.GTBlockstateProvider;

import net.minecraft.resources.Identifier;

import static com.gregtechceu.gtceu.common.data.models.GTMachineModels.*;

public class BlockstateModelLoader {

    public static void init(GTBlockstateProvider provider) {
        for (int tier : GTValues.ALL_TIERS) {
            Identifier model = TIERED_HULL_MODELS.get(tier);
            NativeMachineTemplates.casing(provider, model, SIDED_SIDED_OVERLAY_MODEL, model);
        }
        NativeMachineTemplates.casing(provider, LP_STEAM_HULL_MODEL, SIDED_SIDED_OVERLAY_MODEL, LP_STEAM_HULL_MODEL);
        NativeMachineTemplates.casing(provider, HP_STEAM_HULL_MODEL, SIDED_SIDED_OVERLAY_MODEL, HP_STEAM_HULL_MODEL);
        NativeMachineTemplates.steamVent(provider, VENT_OVERLAY);
    }
}
