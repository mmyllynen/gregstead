package com.gregtechceu.gtceu.data.recipe.misc;

import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTMachines;
import net.minecraft.data.recipes.RecipeOutput;
import net.neoforged.neoforge.common.Tags;

import static com.gregtechceu.gtceu.api.GTValues.*;
import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.*;
import static com.gregtechceu.gtceu.common.data.GTMaterials.*;
import static com.gregtechceu.gtceu.common.data.GTRecipeTypes.ASSEMBLER_RECIPES;
import static com.gregtechceu.gtceu.data.recipe.GTCraftingComponents.*;

/** Extend the original HV passthrough construction to every other registered tier. */
public final class PassthroughHatchRecipes {
    private PassthroughHatchRecipes() {}
    public static void init(RecipeOutput output) {
        for (int tier = LV; tier <= OpV; tier++) {
            if (tier == HV || tier > UV && !GTCEuAPI.isHighTier()) continue;
            int fabricationTier = Math.max(LV, tier - 1);
            String prefix = VN[tier].toLowerCase(java.util.Locale.ROOT);
            ASSEMBLER_RECIPES.recipeBuilder(prefix + "_item_passthrough_hatch")
                    .inputItems(GTMachines.HULL[tier])
                    .inputItems(CONVEYOR.get(tier))
                    .inputItems(gearSmall, Steel, 2)
                    .inputItems(Tags.Items.CHESTS_WOODEN)
                    .inputFluids(SolderingAlloy.getFluid(L))
                    .outputItems(GTMachines.ITEM_PASSTHROUGH_HATCH[tier])
                    .EUt(VA[fabricationTier]).duration(200).save(output);
            ASSEMBLER_RECIPES.recipeBuilder(prefix + "_fluid_passthrough_hatch")
                    .inputItems(GTMachines.HULL[tier])
                    .inputItems(PUMP.get(tier))
                    .inputItems(pipeSmallFluid, Steel, 2)
                    .inputItems(GTBlocks.CASING_TEMPERED_GLASS.asStack())
                    .inputFluids(SolderingAlloy.getFluid(L))
                    .outputItems(GTMachines.FLUID_PASSTHROUGH_HATCH[tier])
                    .EUt(VA[fabricationTier]).duration(200).save(output);
        }
    }
}
