package com.gregtechceu.gtceu.common.machine.trait.customlogic;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.IRecipeCapabilityHolder;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CookingFuel;
import net.minecraft.world.level.storage.loot.*;
import net.minecraft.world.level.storage.loot.parameters.*;
import net.minecraft.world.level.storage.loot.providers.number.ints.ResolvableInt;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/** Resolve actual stack cooking fuel in its current world, without static fuel-table snapshots. */
public enum SteamBoilerFuelLogic implements GTRecipeType.ICustomRecipeLogic {
    SMALL, LARGE;

    public static int burnTime(MetaMachine machine, ItemStack stack) {
        if (stack.isEmpty() || !(machine.getLevel() instanceof ServerLevel level)) return 0;
        var context = new LootContext.Builder(new LootParams.Builder(level)
                .withParameter(LootContextParams.BLOCK_STATE, machine.getBlockState())
                .withParameter(LootContextParams.BLOCK_ENTITY, machine)
                .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(machine.getBlockPos()))
                .withParameter(LootContextParams.CONTAINER, new SimpleContainer(stack.copy()))
                .withParameter(net.neoforged.neoforge.common.loot.NeoForgeLootContextParams.QUERIED_STACK, stack)
                .create(LootContextParamSets.CONTAINER_PROCESS)).create(java.util.Optional.empty());
        return ResolvableInt.getFromItem(stack, DataComponents.COOKING_FUEL, CookingFuel::burnTime, context, 0);
    }

    /** Fuel references may resolve differently for the next item even when its components are identical. */
    public static void invalidateFinishedFuel(com.gregtechceu.gtceu.api.machine.trait.RecipeLogic logic) {
        var recipe = logic.getLastRecipe();
        if (recipe != null && recipe.id != null && recipe.id.getNamespace().equals("gtceu") &&
                recipe.id.getPath().startsWith(recipe.recipeType.registryName.getPath() + "/cooking_fuel/")) logic.markLastRecipeDirty();
    }

    @Override public @Nullable GTRecipe createCustomRecipe(IRecipeCapabilityHolder holder) {
        if (!(holder instanceof MetaMachine machine)) return null;
        for (var handler : holder.getCapabilitiesFlat(IO.IN, ItemRecipeCapability.CAP)) {
            for (var content : handler.getContents()) {
                if (!(content instanceof ItemStack stack) || stack.isEmpty()) continue;
                int duration = burnTime(machine, stack) / (this == LARGE ? 4 : 1);
                if (duration <= 0) continue;
                var type = this == LARGE ? GTRecipeTypes.LARGE_BOILER_RECIPES : GTRecipeTypes.STEAM_BOILER_RECIPES;
                var itemId = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(stack.getItem());
                return type.recipeBuilder("cooking_fuel/" + itemId.getNamespace() + "/" + itemId.getPath())
                        .inputItems(net.neoforged.neoforge.common.crafting.DataComponentIngredient.of(true, stack.copyWithCount(1))).duration(duration).build();
            }
        }
        return null;
    }
}
