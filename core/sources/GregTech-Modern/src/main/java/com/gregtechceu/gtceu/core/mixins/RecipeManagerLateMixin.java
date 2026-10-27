package com.gregtechceu.gtceu.core.mixins;

import com.lowdragmc.lowdraglib.transfer.recipe.RecipeMapLoadingEvent;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeMap;
import net.neoforged.neoforge.common.NeoForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = RecipeManager.class, priority = 1500)
public abstract class RecipeManagerLateMixin {
    @Redirect(method = "<init>", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/item/crafting/RecipeMap;create(Lnet/minecraft/core/HolderLookup;)Lnet/minecraft/world/item/crafting/RecipeMap;"))
    private RecipeMap gtceu$loadRecipes(HolderLookup<Recipe<?>> lookup, HolderLookup.Provider registries) {
        // Keep callback recipe lookups consistent with the early filtered view.
        var context = registries;
        if (lookup instanceof HolderLookup.RegistryLookup<Recipe<?>> recipes) {
            context = HolderLookup.Provider.create(registries.listRegistries().map(registry ->
                    registry.key().equals(net.minecraft.core.registries.Registries.RECIPE) ? recipes : registry));
        }
        var event = new RecipeMapLoadingEvent(context, RecipeMap.create(lookup));
        NeoForge.EVENT_BUS.post(event);
        return event.recipes();
    }
}
