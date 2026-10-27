package com.gregtechceu.gtceu.api.recipe;

import java.util.function.Function;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import com.mojang.serialization.MapCodec;

/** Native persistent/wire representation for GT's category-only special recipes. */
public final class NativeSpecialCraftingSerializer {
    private NativeSpecialCraftingSerializer() {}
    public static <T extends CraftingRecipe> RecipeSerializer<T> create(Function<CraftingBookCategory,T> factory) {
        MapCodec<T> codec=CraftingBookCategory.CODEC.optionalFieldOf("category",CraftingBookCategory.MISC)
            .xmap(factory,CraftingRecipe::category);
        StreamCodec<RegistryFriendlyByteBuf,T> stream=CraftingBookCategory.STREAM_CODEC.map(factory,CraftingRecipe::category).cast();
        return new RecipeSerializer<>(codec,stream);
    }
}
