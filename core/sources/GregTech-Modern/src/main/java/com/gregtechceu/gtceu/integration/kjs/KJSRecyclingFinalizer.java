package com.gregtechceu.gtceu.integration.kjs;

import com.gregtechceu.gtceu.api.data.chemical.material.ItemMaterialData;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.data.recipe.builder.GTRecipeBuilder;
import com.gregtechceu.gtceu.data.recipe.misc.RecyclingRecipes;
import com.gregtechceu.gtceu.integration.kjs.recipe.GTRecipeSchema;

import com.google.gson.JsonElement;
import net.minecraft.resources.RegistryOps;
import dev.latvian.mods.kubejs.recipe.RecipesKubeEvent;
import dev.latvian.mods.kubejs.plugin.builtin.event.ServerEvents;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.Registry;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.crafting.Recipe;
import net.neoforged.neoforge.common.conditions.ICondition;

import java.util.HashSet;
import java.util.Map;
import java.util.stream.Stream;

/** Completes the generic recycling pass deferred until scripted recipe edits are finished. */
public final class KJSRecyclingFinalizer {
    private KJSRecyclingFinalizer() {}

    public static void finish(RecipesKubeEvent event, RegistryOps<JsonElement> ops,
                              Map<Identifier, JsonElement> recipes) {
        if (!ServerEvents.RECIPES.hasListeners()) return;
        var removed = new HashSet<Identifier>();
        event.removedRecipes.forEach(recipe -> removed.add(recipe.getOrCreateId()));
        RecipeOutput output = new RecipeOutput() {
            @Override public Advancement.Builder advancement() { return Advancement.Builder.advancement(); }
            @Override public <T> HolderGetter<T> lookup(ResourceKey<? extends Registry<? extends T>> key) {
                return event.registries.lookupOrThrow(key);
            }
            @Override public <T> Stream<Holder.Reference<T>> listContextElements(ResourceKey<? extends Registry<? extends T>> key) {
                return event.registries.lookupOrThrow(key).listElements();
            }
            @Override public void accept(ResourceKey<Recipe<?>> key, Recipe<?> recipe,
                                         AdvancementHolder advancement, ICondition... conditions) {
                if (removed.contains(key.identifier())) return;
                var json = Recipe.DIRECT_CODEC.encodeStart(ops, recipe).getOrThrow();
                ICondition.writeConditions(event.registries, json.getAsJsonObject(), conditions);
                var requests = ((KJSDeferredRecipeRemovals) event).gtceu$getDeferredRemovals();
                if (!requests.isEmpty()) {
                    var type = event.getRecipeFunction(json.getAsJsonObject().get("type").getAsString());
                    if (type == null) throw new IllegalStateException("Deferred GT recipe has no KubeJS schema: " + key);
                    var view = type.schemaType.schema.deserialize(dev.latvian.mods.kubejs.script.SourceLine.UNKNOWN,
                            type, key.identifier(), json.getAsJsonObject());
                    view.afterLoaded(new dev.latvian.mods.kubejs.util.ErrorStack());
                    for (var request : requests) {
                        if (request.filter().test(new dev.latvian.mods.kubejs.recipe.filter.RecipeMatchContext.Impl(
                                request.context(), view))) return;
                    }
                }
                recipes.put(key.identifier(), json);
            }
        };
        // Material-info flags on scripted recipes use the same arithmetic as native GT builders.
        Stream.concat(event.originalRecipes.values().stream(), event.addedRecipes.stream())
                .filter(recipe -> !recipe.removed && recipe instanceof GTRecipeSchema.GTKubeRecipe)
                .map(recipe -> (GTRecipeSchema.GTKubeRecipe) recipe)
                .filter(recipe -> recipe.itemMaterialInfo || recipe.fluidMaterialInfo || recipe.removeMaterialInfo)
                .sorted(java.util.Comparator.comparing(recipe -> recipe.getOrCreateId().toString()))
                .forEach(scripted -> {
                    var json = recipes.get(scripted.getOrCreateId());
                    if (json == null) return;
                    var decoded = Recipe.DIRECT_CODEC.parse(ops, json).getOrThrow();
                    if (!(decoded instanceof GTRecipe gt)) return;
                    var scriptId = scripted.getOrCreateId();
                    var typePrefix = gt.recipeType.registryName.getPath() + "/";
                    var builderId = scriptId.withPath(path -> path.startsWith(typePrefix)
                            ? path.substring(typePrefix.length()) : path);
                    var builder = new GTRecipeBuilder(gt, gt.recipeType)
                            .id(builderId)
                            .addMaterialInfo(scripted.itemMaterialInfo, scripted.fluidMaterialInfo)
                            .rebuildMaterialInfoFromInputs();
                    if (scripted.removeMaterialInfo) builder.removePreviousMaterialInfo();
                    // Update only material metadata: keep the already-serialized scripted recipe unchanged.
                    RecipeOutput metadataOnly = (RecipeOutput) java.lang.reflect.Proxy.newProxyInstance(
                            RecipeOutput.class.getClassLoader(), new Class<?>[]{RecipeOutput.class},
                            (proxy, method, args) -> method.getName().equals("accept") ? null : method.invoke(output, args));
                    builder.save(metadataOnly);
                });
        RecyclingRecipes.init(output);
        ItemMaterialData.resolveItemMaterialInfos(output);
    }
}
