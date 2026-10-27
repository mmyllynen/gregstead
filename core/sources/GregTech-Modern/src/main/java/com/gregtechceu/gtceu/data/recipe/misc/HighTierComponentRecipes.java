package com.gregtechceu.gtceu.data.recipe.misc;

import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.GTCEu;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;

import static com.gregtechceu.gtceu.api.GTValues.*;
import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.*;
import static com.gregtechceu.gtceu.common.data.GTMaterials.*;
import static com.gregtechceu.gtceu.common.data.GTRecipeTypes.ASSEMBLER_RECIPES;
import static com.gregtechceu.gtceu.data.recipe.GTCraftingComponents.CIRCUIT;

/** Complete the original optional component tiers with a previous-tier fabrication path. */
public final class HighTierComponentRecipes {
    private HighTierComponentRecipes() {}

    public static void init(RecipeOutput output) {
        if (!GTCEuAPI.isHighTier()) return;
        String[] components = {"electric_motor", "electric_pump", "electric_piston", "conveyor_module", "robot_arm",
                "emitter", "sensor", "field_generator", "fluid_regulator"};
        for (int tier = UHV; tier <= OpV; tier++) {
            String prefix = VN[tier].toLowerCase(java.util.Locale.ROOT);
            String previous = VN[tier - 1].toLowerCase(java.util.Locale.ROOT);
            for (String component : components) {
                Item input = BuiltInRegistries.ITEM.getOrThrow(ResourceKey.create(Registries.ITEM, GTCEu.id(previous + "_" + component))).value();
                Item result = BuiltInRegistries.ITEM.getOrThrow(ResourceKey.create(Registries.ITEM, GTCEu.id(prefix + "_" + component))).value();
                ASSEMBLER_RECIPES.recipeBuilder(prefix + "_" + component)
                        .inputItems(input, 4)
                        .inputItems(CIRCUIT.get(tier - 1), 2)
                        .inputItems(plate, Tritanium, 4)
                        .inputItems(wireFine, Europium, 16)
                        .inputFluids(SolderingAlloy.getFluid(L * 8))
                        .outputItems(result)
                        .EUt(VA[tier - 1]).duration(600)
                        .save(output);
            }
        }
    }
}
