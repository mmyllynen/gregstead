package com.lowdragmc.lowdraglib.transfer;

import net.neoforged.fml.common.Mod;

/** Shared native transfer/persistence API, loaded in the same game module layer as its consumers. */
@Mod("gttransfercompat")
public final class TransferCompat {
    private static final net.neoforged.neoforge.registries.DeferredRegister<net.neoforged.neoforge.common.crafting.IngredientType<?>> INGREDIENT_TYPES =
            net.neoforged.neoforge.registries.DeferredRegister.create(
                    net.neoforged.neoforge.registries.NeoForgeRegistries.INGREDIENT_TYPES, "gttransfercompat");
    static {
        INGREDIENT_TYPES.register("empty", () -> com.lowdragmc.lowdraglib.transfer.recipe.EmptyIngredient.TYPE);
    }
    public TransferCompat(net.neoforged.bus.api.IEventBus bus) {
        INGREDIENT_TYPES.register(bus);
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(
                (net.neoforged.neoforge.event.TagsUpdatedEvent event) -> {
                    if (event.shouldUpdateStaticData()) {
                        com.lowdragmc.lowdraglib.transfer.recipe.TagReferences.invalidate();
                    }
                });
    }
}
