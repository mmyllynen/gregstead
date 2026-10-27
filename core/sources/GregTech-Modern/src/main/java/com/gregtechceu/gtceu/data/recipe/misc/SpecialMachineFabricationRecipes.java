package com.gregtechceu.gtceu.data.recipe.misc;

import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.data.GTMachines;
import com.gregtechceu.gtceu.common.data.machines.GTResearchMachines;
import net.minecraft.data.recipes.RecipeOutput;

import static com.gregtechceu.gtceu.api.GTValues.*;
import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.plate;
import static com.gregtechceu.gtceu.common.data.GTMaterials.*;
import static com.gregtechceu.gtceu.common.data.GTRecipeTypes.ASSEMBLER_RECIPES;
import static com.gregtechceu.gtceu.data.recipe.GTCraftingComponents.CIRCUIT;

/** Endgame fabrication for registered special machines required by the full acquisition scope. */
public final class SpecialMachineFabricationRecipes {
    private SpecialMachineFabricationRecipes() {}

    private static void recipe(RecipeOutput output, String name, MachineDefinition predecessor,
                               MachineDefinition result) {
        ASSEMBLER_RECIPES.recipeBuilder(name)
                .inputItems(predecessor.asStack())
                .inputItems(GTItems.ZERO_POINT_MODULE.asStack(4))
                .inputItems(CIRCUIT.get(UV), 4)
                .inputItems(GTItems.FIELD_GENERATOR_UV.asStack(2))
                .inputItems(plate, Tritanium, 8)
                .inputFluids(SolderingAlloy.getFluid(L * 8))
                .outputItems(result.asStack())
                .EUt(VA[UV]).duration(1200).save(output);
    }

    public static void init(RecipeOutput output) {
        recipe(output, "creative_energy", GTMachines.HULL[UV], GTMachines.CREATIVE_ENERGY);
        recipe(output, "creative_chest", GTMachines.QUANTUM_CHEST[UV], GTMachines.CREATIVE_ITEM);
        recipe(output, "creative_tank", GTMachines.QUANTUM_TANK[UV], GTMachines.CREATIVE_FLUID);
        recipe(output, "creative_computation_provider", GTResearchMachines.HIGH_PERFORMANCE_COMPUTING_ARRAY,
                GTMachines.CREATIVE_COMPUTATION_PROVIDER);
        recipe(output, "creative_data_access_hatch", GTResearchMachines.ADVANCED_DATA_ACCESS_HATCH,
                GTResearchMachines.CREATIVE_DATA_ACCESS_HATCH);
    }
}
