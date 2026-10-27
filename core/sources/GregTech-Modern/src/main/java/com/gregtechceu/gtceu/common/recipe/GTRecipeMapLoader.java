package com.gregtechceu.gtceu.common.recipe;

import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.lookup.StagingRecipeDB;
import com.gregtechceu.gtceu.data.recipe.builder.GTRecipeBuilder;
import com.lowdragmc.lowdraglib.transfer.recipe.RecipeMapLoadingEvent;
import com.lowdragmc.lowdraglib.transfer.recipe.RegistryRecipeOutput;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeMap;
import net.neoforged.neoforge.common.conditions.ICondition;
import java.util.ArrayList;
import java.util.LinkedHashMap;

public final class GTRecipeMapLoader {
    private GTRecipeMapLoader() {}
    public static void remove(com.lowdragmc.lowdraglib.transfer.recipe.RecipeRemovalLoadingEvent event) {
        com.gregtechceu.gtceu.common.data.GTRecipes.recipeRemoval(event::remove);
    }
    public static void load(RecipeMapLoadingEvent event) {
        com.gregtechceu.gtceu.api.recipe.lookup.MapIngredientPool.clear();
        com.gregtechceu.gtceu.common.item.armor.PowerlessJetpack.FUELS.clear();
        var byKey = new LinkedHashMap<ResourceKey<Recipe<?>>, RecipeHolder<?>>();
        event.recipes().values().forEach(holder -> byKey.put(holder.id(), holder));
        for (var holder : event.recipes().values()) {
            if (holder.value() instanceof GTRecipe recipe) {
                new GTRecipeBuilder(recipe, recipe.recipeType)
                        .id(holder.id().identifier().withPath(path -> path.substring(path.indexOf('/') + 1)))
                        .onSave(recipe.recipeType.getRecipeBuilder().onSave)
                        .save(new RegistryRecipeOutput(event.registries()) {
                            @Override public void accept(ResourceKey<Recipe<?>> key, Recipe<?> value,
                                                         AdvancementHolder advancement, ICondition... conditions) {
                                byKey.put(key, new RecipeHolder<>(key, value));
                            }
                        });
            }
        }
        var recipes = RecipeMap.createClient(new ArrayList<>(byKey.values()));
        event.recipes(recipes);
        for (var type : BuiltInRegistries.RECIPE_TYPE) {
            if (!(type instanceof GTRecipeType gtType)) continue;
            gtType.db().clear();
            gtType.getCategoryMap().clear();
            var staging = new StagingRecipeDB();
            for (var entry : gtType.getProxyRecipes().entrySet()) {
                entry.getValue().clear();
                for (var holder : recipes.values()) {
                    if (holder.value().getType() == entry.getKey()) {
                        var proxy = gtType.toGTRecipe(holder);
                        if (proxy != null) entry.getValue().add(proxy);
                    }
                }
            }
            for (var holder : recipes.values()) {
                if (holder.value() instanceof GTRecipe recipe && recipe.getType() == gtType) {
                    recipe.setId(holder.id().identifier());
                    staging.add(recipe);
                }
            }
            gtType.getProxyRecipes().values().forEach(proxies -> proxies.forEach(proxy -> staging.add(proxy.value())));
            staging.populateDB(gtType.db());
        }
    }
}
