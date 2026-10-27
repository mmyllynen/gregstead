package com.gregtechceu.gtceu.data.recipe.misc;

import net.minecraft.data.recipes.RecipeOutput;

import static com.gregtechceu.gtceu.api.GTValues.*;
import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.dust;
import static com.gregtechceu.gtceu.common.data.GTMaterials.*;
import static com.gregtechceu.gtceu.common.data.GTRecipeTypes.*;

/** Initial production routes for registered dust forms with only circular packing recipes. */
public final class MissingDustAcquisitionRecipes {
    private MissingDustAcquisitionRecipes() {}

    public static void init(RecipeOutput output) {
        // Preserve the exact material-unit balances of the existing decomposition recipes.
        MIXER_RECIPES.recipeBuilder("perlite_synthesis")
                .inputItems(dust, Obsidian, 2)
                .inputFluids(Water.getFluid(1000))
                .outputItems(dust, Perlite, 3)
                .duration(108).EUt(VA[LV]).save(output);
        MIXER_RECIPES.recipeBuilder("calcium_carbonate_synthesis")
                .inputItems(dust, Calcium)
                .inputItems(dust, Carbon)
                .inputFluids(Oxygen.getFluid(3000))
                .outputItems(dust, CalciumCarbonate, 5)
                .duration(200).EUt(VA[MV]).save(output);
        MIXER_RECIPES.recipeBuilder("potassium_sulfate_synthesis")
                .inputItems(dust, Potassium, 2)
                .inputItems(dust, Sulfur)
                .inputFluids(Oxygen.getFluid(4000))
                .outputItems(dust, PotassiumSulfate, 7)
                .duration(336).EUt(VA[MV]).save(output);

        // Lutetium already has a fusion fluid producer, but lacked a fluid-to-dust route.
        VACUUM_RECIPES.recipeBuilder("lutetium_dust_from_fluid")
                .inputFluids(Lutetium.getFluid(L))
                .outputItems(dust, Lutetium)
                .duration(400).EUt(VA[IV]).save(output);
    }
}
