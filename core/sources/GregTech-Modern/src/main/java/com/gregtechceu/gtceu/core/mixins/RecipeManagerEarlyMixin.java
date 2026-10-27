package com.gregtechceu.gtceu.core.mixins;

import com.lowdragmc.lowdraglib.transfer.recipe.FilteredRecipeLookup;
import com.lowdragmc.lowdraglib.transfer.recipe.RecipeRemovalLoadingEvent;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.neoforged.neoforge.common.NeoForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = RecipeManager.class, priority = 500)
public abstract class RecipeManagerEarlyMixin {
    @Redirect(method = "<init>", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/core/HolderLookup$Provider;lookupOrThrow(Lnet/minecraft/resources/ResourceKey;)Lnet/minecraft/core/HolderLookup$RegistryLookup;"))
    private HolderLookup.RegistryLookup<Recipe<?>> gtceu$removeRecipes(HolderLookup.Provider registries,
            ResourceKey<? extends Registry<? extends Recipe<?>>> key) {
        var event = new RecipeRemovalLoadingEvent(registries);
        NeoForge.EVENT_BUS.post(event);
        return new FilteredRecipeLookup(registries.lookupOrThrow(key), event.removed());
    }
}
