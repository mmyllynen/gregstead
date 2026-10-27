package com.gregtechceu.gtceu.data.recipe.misc;

import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTDimensionMarkers;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.ItemLike;

import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.rod;
import static com.gregtechceu.gtceu.common.data.GTMaterials.*;
import static com.gregtechceu.gtceu.common.data.GTRecipeTypes.ASSEMBLER_RECIPES;

/** Acquisition paths for original registered blocks without a manufacturing route. */
public final class MissingBlockAcquisitionRecipes {
    private MissingBlockAcquisitionRecipes() {}

    public static void init(RecipeOutput output) {
        // Material-equivalent decorative conversion provides the first GT concrete block.
        // Existing GT stone/cobble conversions otherwise only reproduce an existing block.
        com.gregtechceu.gtceu.data.recipe.VanillaRecipeHelper.addShapelessRecipe(output,
                "light_concrete_from_white_concrete", GTBlocks.LIGHT_CONCRETE.asStack(), Blocks.CONCRETE.pick(net.minecraft.world.item.DyeColor.WHITE));

        ASSEMBLER_RECIPES.recipeBuilder("reinforced_stone")
                .inputItems(Blocks.STONE, 4)
                .inputItems(rod, Steel, 4)
                .inputFluids(Concrete.getFluid(576))
                .outputItems(GTBlocks.REINFORCED_STONE.asStack(4))
                .EUt(16).duration(200).save(output);
        marker(output, "overworld_marker", Blocks.STONE, GTDimensionMarkers.OVERWORLD_MARKER);
        marker(output, "the_nether_marker", Blocks.NETHERRACK, GTDimensionMarkers.NETHER_MARKER);
        marker(output, "the_end_marker", Blocks.END_STONE, GTDimensionMarkers.END_MARKER);
    }

    private static void marker(RecipeOutput output, String name, ItemLike stone, ItemLike result) {
        ASSEMBLER_RECIPES.recipeBuilder(name)
                .inputItems(stone)
                .inputItems(Items.PAPER, 2)
                .inputItems(Items.COMPASS)
                .outputItems(result)
                .EUt(4).duration(100).save(output);
    }
}
