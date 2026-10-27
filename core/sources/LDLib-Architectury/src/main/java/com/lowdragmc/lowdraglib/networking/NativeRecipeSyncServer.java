package com.lowdragmc.lowdraglib.networking;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;

/** Preserve full recipe access for built-in and registered custom compass recipe views. */
public final class NativeRecipeSyncServer {
    private NativeRecipeSyncServer() {}
    public static void request(OnDatapackSyncEvent event) {
        event.sendRecipes(BuiltInRegistries.RECIPE_TYPE);
    }
}
