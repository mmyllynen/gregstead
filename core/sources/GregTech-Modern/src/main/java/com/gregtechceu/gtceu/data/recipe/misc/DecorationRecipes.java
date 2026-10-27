package com.gregtechceu.gtceu.data.recipe.misc;

import com.gregtechceu.gtceu.common.data.GTRecipeCategories;

import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import static com.gregtechceu.gtceu.api.GTValues.*;
import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.*;
import static com.gregtechceu.gtceu.common.data.GTBlocks.*;
import static com.gregtechceu.gtceu.common.data.GTMaterials.*;
import static com.gregtechceu.gtceu.common.data.GTRecipeTypes.*;

public class DecorationRecipes {

    private DecorationRecipes() {}

    public static void init(RecipeOutput provider) {
        assemblerRecipes(provider);
        dyeRecipes(provider);
        copperOxidationRecipes(provider);
    }

    private static void assemblerRecipes(RecipeOutput provider) {
        ASSEMBLER_RECIPES.recipeBuilder("metal_sheet_white")
                .inputItems(block, Concrete, 5)
                .inputItems(plate, Iron, 2)
                .circuitMeta(8)
                .outputItems(METAL_SHEETS.get(DyeColor.WHITE), 32)
                .EUt(4).duration(20)
                .addMaterialInfo(true).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("large_metal_sheet_white")
                .inputItems(block, Concrete, 5)
                .inputItems(plate, Iron, 4)
                .circuitMeta(9)
                .outputItems(LARGE_METAL_SHEETS.get(DyeColor.WHITE), 32)
                .EUt(4).duration(20)
                .addMaterialInfo(true).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("studs_black")
                .inputItems(block, Concrete, 3)
                .inputItems(plate, Rubber, 3)
                .circuitMeta(8)
                .outputItems(STUDS.get(DyeColor.BLACK), 32)
                .EUt(4).duration(20)
                .addMaterialInfo(true).save(provider);
    }

    private static void dyeRecipes(RecipeOutput provider) {
        for (int i = 0; i < CHEMICAL_DYES.length; i++) {
            var color = DyeColor.values()[i];
            var colorName = color.getName();
            CHEMICAL_BATH_RECIPES.recipeBuilder("metal_sheet_%s".formatted(colorName))
                    .inputItems(METAL_SHEETS.get(DyeColor.WHITE).asStack())
                    .inputFluids(CHEMICAL_DYES[i].getFluid(9))
                    .outputItems(METAL_SHEETS.get(color))
                    .EUt(2).duration(10)
                    .category(GTRecipeCategories.CHEM_DYES)
                    .save(provider);

            CHEMICAL_BATH_RECIPES.recipeBuilder("large_metal_sheet_%s".formatted(colorName))
                    .inputItems(LARGE_METAL_SHEETS.get(DyeColor.WHITE).asStack())
                    .inputFluids(CHEMICAL_DYES[i].getFluid(9))
                    .outputItems(LARGE_METAL_SHEETS.get(color))
                    .EUt(2).duration(10)
                    .category(GTRecipeCategories.CHEM_DYES)
                    .save(provider);

            CHEMICAL_BATH_RECIPES.recipeBuilder("studs_%s".formatted(colorName))
                    .inputItems(STUDS.get(DyeColor.BLACK).asStack())
                    .inputFluids(CHEMICAL_DYES[i].getFluid(9))
                    .outputItems(STUDS.get(color))
                    .EUt(2).duration(10)
                    .category(GTRecipeCategories.CHEM_DYES)
                    .save(provider);
        }
    }

    private static void copperOxidationRecipes(RecipeOutput provider) {
        registerOxidationChain(provider, "copper_block", Items.COPPER_BLOCK.weathering().pick(net.minecraft.world.level.block.WeatheringCopper.WeatherState.UNAFFECTED), Items.COPPER_BLOCK.weathering().pick(net.minecraft.world.level.block.WeatheringCopper.WeatherState.EXPOSED),
                Items.COPPER_BLOCK.weathering().pick(net.minecraft.world.level.block.WeatheringCopper.WeatherState.WEATHERED), Items.COPPER_BLOCK.weathering().pick(net.minecraft.world.level.block.WeatheringCopper.WeatherState.OXIDIZED));
        registerOxidationChain(provider, "cut_copper", Items.CUT_COPPER.weathering().pick(net.minecraft.world.level.block.WeatheringCopper.WeatherState.UNAFFECTED), Items.CUT_COPPER.weathering().pick(net.minecraft.world.level.block.WeatheringCopper.WeatherState.EXPOSED),
                Items.CUT_COPPER.weathering().pick(net.minecraft.world.level.block.WeatheringCopper.WeatherState.WEATHERED), Items.CUT_COPPER.weathering().pick(net.minecraft.world.level.block.WeatheringCopper.WeatherState.OXIDIZED));
        registerOxidationChain(provider, "cut_copper_stairs", Items.CUT_COPPER_STAIRS.weathering().pick(net.minecraft.world.level.block.WeatheringCopper.WeatherState.UNAFFECTED), Items.CUT_COPPER_STAIRS.weathering().pick(net.minecraft.world.level.block.WeatheringCopper.WeatherState.EXPOSED),
                Items.CUT_COPPER_STAIRS.weathering().pick(net.minecraft.world.level.block.WeatheringCopper.WeatherState.WEATHERED), Items.CUT_COPPER_STAIRS.weathering().pick(net.minecraft.world.level.block.WeatheringCopper.WeatherState.OXIDIZED));
        registerOxidationChain(provider, "cut_copper_slab", Items.CUT_COPPER_SLAB.weathering().pick(net.minecraft.world.level.block.WeatheringCopper.WeatherState.UNAFFECTED), Items.CUT_COPPER_SLAB.weathering().pick(net.minecraft.world.level.block.WeatheringCopper.WeatherState.EXPOSED),
                Items.CUT_COPPER_SLAB.weathering().pick(net.minecraft.world.level.block.WeatheringCopper.WeatherState.WEATHERED), Items.CUT_COPPER_SLAB.weathering().pick(net.minecraft.world.level.block.WeatheringCopper.WeatherState.OXIDIZED));

        // Waxing recipes
        CHEMICAL_BATH_RECIPES.recipeBuilder("waxing_copper_block")
                .inputItems(Items.COPPER_BLOCK.weathering().pick(net.minecraft.world.level.block.WeatheringCopper.WeatherState.UNAFFECTED))
                .inputFluids(Wax, L / 2)
                .outputItems(Items.COPPER_BLOCK.waxed().pick(net.minecraft.world.level.block.WeatheringCopper.WeatherState.UNAFFECTED))
                .EUt(VA[ULV]).duration(10)
                .save(provider);
        CHEMICAL_BATH_RECIPES.recipeBuilder("waxing_exposed_copper")
                .inputItems(Items.COPPER_BLOCK.weathering().pick(net.minecraft.world.level.block.WeatheringCopper.WeatherState.EXPOSED))
                .inputFluids(Wax, L / 2)
                .outputItems(Items.COPPER_BLOCK.waxed().pick(net.minecraft.world.level.block.WeatheringCopper.WeatherState.EXPOSED))
                .EUt(VA[ULV]).duration(10)
                .save(provider);
        CHEMICAL_BATH_RECIPES.recipeBuilder("waxing_weathered_copper")
                .inputItems(Items.COPPER_BLOCK.weathering().pick(net.minecraft.world.level.block.WeatheringCopper.WeatherState.WEATHERED))
                .inputFluids(Wax, L / 2)
                .outputItems(Items.COPPER_BLOCK.waxed().pick(net.minecraft.world.level.block.WeatheringCopper.WeatherState.WEATHERED))
                .EUt(VA[ULV]).duration(10)
                .save(provider);
        CHEMICAL_BATH_RECIPES.recipeBuilder("waxing_oxidized_copper")
                .inputItems(Items.COPPER_BLOCK.weathering().pick(net.minecraft.world.level.block.WeatheringCopper.WeatherState.OXIDIZED))
                .inputFluids(Wax, L / 2)
                .outputItems(Items.COPPER_BLOCK.waxed().pick(net.minecraft.world.level.block.WeatheringCopper.WeatherState.OXIDIZED))
                .EUt(VA[ULV]).duration(10)
                .save(provider);
        CHEMICAL_BATH_RECIPES.recipeBuilder("waxing_cut_copper")
                .inputItems(Items.CUT_COPPER.weathering().pick(net.minecraft.world.level.block.WeatheringCopper.WeatherState.UNAFFECTED))
                .inputFluids(Wax, L / 2)
                .outputItems(Items.CUT_COPPER.waxed().pick(net.minecraft.world.level.block.WeatheringCopper.WeatherState.UNAFFECTED))
                .EUt(VA[ULV]).duration(10)
                .save(provider);
        CHEMICAL_BATH_RECIPES.recipeBuilder("waxing_exposed_cut_copper")
                .inputItems(Items.CUT_COPPER.weathering().pick(net.minecraft.world.level.block.WeatheringCopper.WeatherState.EXPOSED))
                .inputFluids(Wax, L / 2)
                .outputItems(Items.CUT_COPPER.waxed().pick(net.minecraft.world.level.block.WeatheringCopper.WeatherState.EXPOSED))
                .EUt(VA[ULV]).duration(10)
                .save(provider);
        CHEMICAL_BATH_RECIPES.recipeBuilder("waxing_weathered_cut_copper")
                .inputItems(Items.CUT_COPPER.weathering().pick(net.minecraft.world.level.block.WeatheringCopper.WeatherState.WEATHERED))
                .inputFluids(Wax, L / 2)
                .outputItems(Items.CUT_COPPER.waxed().pick(net.minecraft.world.level.block.WeatheringCopper.WeatherState.WEATHERED))
                .EUt(VA[ULV]).duration(10)
                .save(provider);
        CHEMICAL_BATH_RECIPES.recipeBuilder("waxing_oxidized_cut_copper")
                .inputItems(Items.CUT_COPPER.weathering().pick(net.minecraft.world.level.block.WeatheringCopper.WeatherState.OXIDIZED))
                .inputFluids(Wax, L / 2)
                .outputItems(Items.CUT_COPPER.waxed().pick(net.minecraft.world.level.block.WeatheringCopper.WeatherState.OXIDIZED))
                .EUt(VA[ULV]).duration(10)
                .save(provider);
        CHEMICAL_BATH_RECIPES.recipeBuilder("waxing_cut_copper_stairs")
                .inputItems(Items.CUT_COPPER_STAIRS.weathering().pick(net.minecraft.world.level.block.WeatheringCopper.WeatherState.UNAFFECTED))
                .inputFluids(Wax, L / 2)
                .outputItems(Items.CUT_COPPER_STAIRS.waxed().pick(net.minecraft.world.level.block.WeatheringCopper.WeatherState.UNAFFECTED))
                .EUt(VA[ULV]).duration(10)
                .save(provider);
        CHEMICAL_BATH_RECIPES.recipeBuilder("waxing_exposed_cut_copper_stairs")
                .inputItems(Items.CUT_COPPER_STAIRS.weathering().pick(net.minecraft.world.level.block.WeatheringCopper.WeatherState.EXPOSED))
                .inputFluids(Wax, L / 2)
                .outputItems(Items.CUT_COPPER_STAIRS.waxed().pick(net.minecraft.world.level.block.WeatheringCopper.WeatherState.EXPOSED))
                .EUt(VA[ULV]).duration(10)
                .save(provider);
        CHEMICAL_BATH_RECIPES.recipeBuilder("waxing_weathered_cut_copper_stairs")
                .inputItems(Items.CUT_COPPER_STAIRS.weathering().pick(net.minecraft.world.level.block.WeatheringCopper.WeatherState.WEATHERED))
                .inputFluids(Wax, L / 2)
                .outputItems(Items.CUT_COPPER_STAIRS.waxed().pick(net.minecraft.world.level.block.WeatheringCopper.WeatherState.WEATHERED))
                .EUt(VA[ULV]).duration(10)
                .save(provider);
        CHEMICAL_BATH_RECIPES.recipeBuilder("waxing_oxidized_cut_copper_stairs")
                .inputItems(Items.CUT_COPPER_STAIRS.weathering().pick(net.minecraft.world.level.block.WeatheringCopper.WeatherState.OXIDIZED))
                .inputFluids(Wax, L / 2)
                .outputItems(Items.CUT_COPPER_STAIRS.waxed().pick(net.minecraft.world.level.block.WeatheringCopper.WeatherState.OXIDIZED))
                .EUt(VA[ULV]).duration(10)
                .save(provider);
        CHEMICAL_BATH_RECIPES.recipeBuilder("waxing_cut_copper_slab")
                .inputItems(Items.CUT_COPPER_SLAB.weathering().pick(net.minecraft.world.level.block.WeatheringCopper.WeatherState.UNAFFECTED))
                .inputFluids(Wax, L / 2)
                .outputItems(Items.CUT_COPPER_SLAB.waxed().pick(net.minecraft.world.level.block.WeatheringCopper.WeatherState.UNAFFECTED))
                .EUt(VA[ULV]).duration(10)
                .save(provider);
        CHEMICAL_BATH_RECIPES.recipeBuilder("waxing_exposed_cut_copper_slab")
                .inputItems(Items.CUT_COPPER_SLAB.weathering().pick(net.minecraft.world.level.block.WeatheringCopper.WeatherState.EXPOSED))
                .inputFluids(Wax, L / 2)
                .outputItems(Items.CUT_COPPER_SLAB.waxed().pick(net.minecraft.world.level.block.WeatheringCopper.WeatherState.EXPOSED))
                .EUt(VA[ULV]).duration(10)
                .save(provider);
        CHEMICAL_BATH_RECIPES.recipeBuilder("waxing_weathered_cut_copper_slab")
                .inputItems(Items.CUT_COPPER_SLAB.weathering().pick(net.minecraft.world.level.block.WeatheringCopper.WeatherState.WEATHERED))
                .inputFluids(Wax, L / 2)
                .outputItems(Items.CUT_COPPER_SLAB.waxed().pick(net.minecraft.world.level.block.WeatheringCopper.WeatherState.WEATHERED))
                .EUt(VA[ULV]).duration(10)
                .save(provider);
        CHEMICAL_BATH_RECIPES.recipeBuilder("waxing_oxidized_cut_copper_slab")
                .inputItems(Items.CUT_COPPER_SLAB.weathering().pick(net.minecraft.world.level.block.WeatheringCopper.WeatherState.OXIDIZED))
                .inputFluids(Wax, L / 2)
                .outputItems(Items.CUT_COPPER_SLAB.waxed().pick(net.minecraft.world.level.block.WeatheringCopper.WeatherState.OXIDIZED))
                .EUt(VA[ULV]).duration(10)
                .save(provider);
    }

    private static final String[] OXIDATION_STAGES = { "raw", "exposed", "weathered", "oxidized" };

    private static void registerOxidationChain(RecipeOutput provider, String name, Item... items) {
        for (int i = 0; i < items.length - 1; i++) {
            CHEMICAL_BATH_RECIPES
                    .recipeBuilder(
                            "%s_to_%s_%s_oxidation".formatted(OXIDATION_STAGES[i], OXIDATION_STAGES[i + 1], name))
                    .inputItems(items[i])
                    .inputFluids(Oxygen, 100)
                    .outputItems(items[i + 1])
                    .EUt(VA[ULV]).duration(10)
                    .save(provider);
        }
    }
}
