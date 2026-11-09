package dev.emi.emi.integration.ae2;

import appeng.menu.me.items.CraftingTermMenu;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;

/** Loaded by the client plugin registry only when the pinned AE2 runtime is present. */
public final class Ae2EmiPlugin implements EmiPlugin {
    @Override
    public void register(EmiRegistry registry) {
        registry.addRecipeHandler(CraftingTermMenu.TYPE, new Ae2CraftingHandler());
    }
}
