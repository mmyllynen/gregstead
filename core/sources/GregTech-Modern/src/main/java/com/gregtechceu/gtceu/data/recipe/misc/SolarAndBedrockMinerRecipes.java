package com.gregtechceu.gtceu.data.recipe.misc;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.common.data.*;
import com.gregtechceu.gtceu.common.data.machines.GTMultiMachines;
import com.gregtechceu.gtceu.common.machine.multiblock.electric.BedrockOreMinerMachine;
import net.minecraft.core.registries.*;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import static com.gregtechceu.gtceu.api.GTValues.*;
import static com.gregtechceu.gtceu.common.data.GTMaterials.SolderingAlloy;
import static com.gregtechceu.gtceu.common.data.GTRecipeTypes.ASSEMBLER_RECIPES;
import static com.gregtechceu.gtceu.data.recipe.GTCraftingComponents.*;

/** Preserve existing low solar recipes and extend previously uncraftable registered machines. */
public final class SolarAndBedrockMinerRecipes {
    private SolarAndBedrockMinerRecipes() {}
    private static Item solar(int tier) {
        return BuiltInRegistries.ITEM.getOrThrow(ResourceKey.create(Registries.ITEM,
                GTCEu.id(VN[tier].toLowerCase(java.util.Locale.ROOT) + "_solar_panel"))).value();
    }
    public static void init(RecipeOutput output) {
        for (int tier = MV; tier <= UV; tier++) {
            // Original LV solar already requires LuV-era wafer/circuit materials.
            int fabricationTier = Math.max(LuV, tier - 1);
            ASSEMBLER_RECIPES.recipeBuilder(VN[tier].toLowerCase(java.util.Locale.ROOT) + "_solar_panel")
                    .inputItems(solar(tier - 1), 4)
                    .inputItems(CIRCUIT.get(Math.max(LuV, tier)), 2)
                    .inputItems(GTItems.NAQUADAH_WAFER, 2)
                    .inputItems(GTBlocks.CASING_TEMPERED_GLASS.asStack())
                    .inputFluids(SolderingAlloy.getFluid(L * 8))
                    .outputItems(solar(tier))
                    .EUt(VA[fabricationTier]).duration(400).save(output);
        }
        for (int tier = MV; tier <= EV; tier++) {
            ASSEMBLER_RECIPES.recipeBuilder(VN[tier].toLowerCase(java.util.Locale.ROOT) + "_bedrock_ore_miner")
                    .inputItems(GTMachines.MINER[tier - 1])
                    .inputItems(CIRCUIT.get(tier), 2)
                    .inputItems(MOTOR.get(tier), 2)
                    .inputItems(PISTON.get(tier), 2)
                    .inputItems(new ItemStack(BedrockOreMinerMachine.getCasingState(tier), 4))
                    .inputItems(GTItems.COMPONENT_GRINDER_DIAMOND)
                    .inputFluids(SolderingAlloy.getFluid(L * 8))
                    .outputItems(GTMultiMachines.BEDROCK_ORE_MINER[tier])
                    .EUt(VA[tier - 1]).duration(400).save(output);
        }
    }
}
