package com.lowdragmc.lowdraglib.client;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.neoforged.neoforge.client.event.RecipesReceivedEvent;
import java.util.*;

/** Connection-local native recipe contents keyed by the original recipe identifier. */
public final class NativeRecipeCatalog {
    private static volatile Map<Identifier, RecipeHolder<?>> recipes = Map.of();
    private NativeRecipeCatalog() {}
    public static void receive(RecipesReceivedEvent event) {
        var updated = new LinkedHashMap<>(recipes);
        updated.values().removeIf(holder -> event.getRecipeTypes().contains(holder.value().getType()));
        for (var holder : event.getRecipeMap().values()) updated.put(holder.id().identifier(), holder);
        recipes = Collections.unmodifiableMap(updated);
    }
    public static RecipeHolder<?> get(Identifier id) { return recipes.get(id); }
    public static Collection<RecipeHolder<?>> all() { return recipes.values(); }
    public static void clear() { recipes = Map.of(); }
}
