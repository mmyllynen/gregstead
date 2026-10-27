package dev.emi.emi.runtime;

import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.*;
import net.neoforged.neoforge.client.event.RecipesReceivedEvent;
import java.util.ArrayList;

/** Complete native recipe contents for this client connection, including registered custom types. */
public final class NativeRecipeContents {
    private record State(RecipeMap recipes, boolean ready) {}
    private static volatile State state = new State(RecipeMap.EMPTY, false);
    private NativeRecipeContents() {}
    public static void receive(RecipesReceivedEvent event) {
        var updated = new ArrayList<>(state.recipes().values());
        updated.removeIf(holder -> event.getRecipeTypes().contains(holder.value().getType()));
        updated.addAll(event.getRecipeMap().values());
        state = new State(RecipeMap.createClient(updated), true);
    }
    public static boolean isReady() { return state.ready(); }
    public static RecipeMap map() { return state.recipes(); }
    public static RecipeHolder<?> get(Identifier id) { return state.recipes().byKey(ResourceKey.create(Registries.RECIPE, id)); }
    public static void clear() { state = new State(RecipeMap.EMPTY, false); }
}
