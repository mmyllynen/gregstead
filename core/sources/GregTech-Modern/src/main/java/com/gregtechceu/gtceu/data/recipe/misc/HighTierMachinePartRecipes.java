package com.gregtechceu.gtceu.data.recipe.misc;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTCEuAPI;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;

import static com.gregtechceu.gtceu.api.GTValues.*;
import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.plate;
import static com.gregtechceu.gtceu.common.data.GTMaterials.*;
import static com.gregtechceu.gtceu.common.data.GTRecipeTypes.ASSEMBLER_RECIPES;
import static com.gregtechceu.gtceu.data.recipe.GTCraftingComponents.CIRCUIT;

/** Native fabrication paths for original registered parts beyond their original recipe ranges. */
public final class HighTierMachinePartRecipes {
    private HighTierMachinePartRecipes() {}
    private static Item item(int tier, String suffix) {
        return BuiltInRegistries.ITEM.getOrThrow(ResourceKey.create(Registries.ITEM,
                GTCEu.id(VN[tier].toLowerCase(java.util.Locale.ROOT) + "_" + suffix))).value();
    }
    public static void init(RecipeOutput output) {
        if (!GTCEuAPI.isHighTier()) return;
        for (int tier = UEV; tier <= MAX; tier++) {
            ASSEMBLER_RECIPES.recipeBuilder(VN[tier].toLowerCase(java.util.Locale.ROOT) + "_machine_casing")
                    .inputItems(item(tier - 1, "machine_casing"), 2)
                    .inputItems(CIRCUIT.get(tier - 1), 2)
                    .inputItems(plate, Tritanium, 4)
                    .inputFluids(SolderingAlloy.getFluid(L * 8))
                    .outputItems(item(tier, "machine_casing"))
                    .EUt(VA[tier - 1]).duration(600).save(output);
            for (String suffix : new String[]{"energy_input_hatch", "energy_output_hatch"}) {
                ASSEMBLER_RECIPES.recipeBuilder(VN[tier].toLowerCase(java.util.Locale.ROOT) + "_" + suffix)
                        .inputItems(item(tier - 1, suffix), 2)
                        .inputItems(item(tier, "machine_casing"))
                        .inputItems(item(tier - 1, "field_generator"))
                        .inputItems(CIRCUIT.get(tier - 1), 2)
                        .inputFluids(SolderingAlloy.getFluid(L * 8))
                        .outputItems(item(tier, suffix))
                        .EUt(VA[tier - 1]).duration(600).save(output);
            }
        }
        // The registered UEV-OpV storage tiers otherwise only have self-input NBT recipes.
        // Use the same progressive fabrication budget as other high-tier machine parts.
        for (int tier = UEV; tier <= OpV; tier++) {
            for (String suffix : new String[]{"quantum_chest", "quantum_tank"}) {
                ASSEMBLER_RECIPES.recipeBuilder(VN[tier].toLowerCase(java.util.Locale.ROOT) + "_" + suffix)
                        .inputItems(item(tier - 1, suffix), 2)
                        .inputItems(item(tier, "machine_casing"))
                        .inputItems(item(tier - 1, "field_generator"))
                        .inputItems(CIRCUIT.get(tier - 1), 2)
                        .inputItems(plate, Tritanium, 4)
                        .inputFluids(SolderingAlloy.getFluid(L * 8))
                        .outputItems(item(tier, suffix))
                        .EUt(VA[tier - 1]).duration(600).save(output);
            }
        }
        for (int tier = UHV; tier <= OpV; tier++) {
            ASSEMBLER_RECIPES.recipeBuilder(VN[tier].toLowerCase(java.util.Locale.ROOT) + "_rotor_holder")
                    .inputItems(item(tier - 1, "rotor_holder"), 2)
                    .inputItems(item(tier, "electric_motor"))
                    .inputItems(CIRCUIT.get(tier - 1), 2)
                    .inputItems(plate, Tritanium, 4)
                    .inputFluids(SolderingAlloy.getFluid(L * 8))
                    .outputItems(item(tier, "rotor_holder"))
                    .EUt(VA[tier - 1]).duration(600).save(output);
        }
    }
}
