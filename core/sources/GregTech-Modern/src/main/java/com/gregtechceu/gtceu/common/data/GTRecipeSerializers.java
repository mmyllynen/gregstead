package com.gregtechceu.gtceu.common.data;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.recipe.*;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class GTRecipeSerializers {

    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister
            .create(Registries.RECIPE_SERIALIZER, GTCEu.MOD_ID);

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<StrictSizeShapedRecipe>> CRAFTING_SHAPED_STRICT_SIZE = RECIPE_SERIALIZERS
            .register("crafting_shaped_strict_size", () -> StrictSizeShapedRecipe.Serializer.INSTANCE);

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<StrictShapedRecipe>> CRAFTING_SHAPED_STRICT = RECIPE_SERIALIZERS
            .register("crafting_shaped_strict", () -> StrictShapedRecipe.Serializer.INSTANCE);
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<ShapedEnergyTransferRecipe>> CRAFTING_SHAPED_ENERGY_TRANSFER = RECIPE_SERIALIZERS
            .register("crafting_shaped_energy_transfer", () -> ShapedEnergyTransferRecipe.Serializer.INSTANCE);
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<ShapedFluidContainerRecipe>> CRAFTING_SHAPED_FLUID_CONTAINER = RECIPE_SERIALIZERS
            .register("crafting_shaped_fluid_container", () -> ShapedFluidContainerRecipe.Serializer.INSTANCE);

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<ToolHeadReplaceRecipe>> CRAFTING_TOOL_HEAD_REPLACE = RECIPE_SERIALIZERS
            .register("crafting_tool_head_replace",
                    () -> NativeSpecialCraftingSerializer.create(ToolHeadReplaceRecipe::new));
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<FacadeCoverRecipe>> CRAFTING_FACADE_COVER = RECIPE_SERIALIZERS
            .register("crafting_facade_cover",
                    () -> NativeSpecialCraftingSerializer.create(FacadeCoverRecipe::new));
}
