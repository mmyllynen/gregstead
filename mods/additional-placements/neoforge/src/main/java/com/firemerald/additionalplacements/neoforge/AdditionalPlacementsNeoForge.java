package com.firemerald.additionalplacements.neoforge;

import com.firemerald.additionalplacements.AdditionalPlacementsMod;
import com.firemerald.additionalplacements.generation.APGenerationTypes;
import com.firemerald.additionalplacements.generation.Registration;
import net.neoforged.fml.common.Mod;

@Mod(AdditionalPlacementsMod.MOD_ID)
public final class AdditionalPlacementsNeoForge {
    public static boolean dynamicRegistration = false;

    public AdditionalPlacementsNeoForge() {
        Registration.addRegistration(new APGenerationTypes());
        AdditionalPlacementsMod.LOGGER.warn("During block registration you may receive several reports of \"Potentially Dangerous alternative prefix `additionalplacements`\". Ignore these, they are intended.");
    }
}
