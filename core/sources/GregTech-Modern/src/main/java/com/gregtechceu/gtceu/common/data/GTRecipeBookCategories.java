package com.gregtechceu.gtceu.common.data;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Machine recipe category; actual machine UI grouping remains GTRecipeCategory. */
public final class GTRecipeBookCategories {
    private static final DeferredRegister<RecipeBookCategory> REGISTER =
            DeferredRegister.create(Registries.RECIPE_BOOK_CATEGORY, "gtceu");
    public static final DeferredHolder<RecipeBookCategory, RecipeBookCategory> MACHINE =
            REGISTER.register("machine", RecipeBookCategory::new);
    private GTRecipeBookCategories() {}
    public static void init(IEventBus bus) { REGISTER.register(bus); }
}
