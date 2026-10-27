package dev.emi.emi.api.neoforge;

import dev.emi.emi.api.stack.EmiIngredient;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import net.neoforged.neoforge.fluids.crafting.FluidIngredient;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

import java.util.Arrays;

public final class NeoForgeEmiIngredient {

    public static EmiIngredient of(SizedIngredient ingredient) {
        return EmiIngredient.of(ingredient.ingredient(), ingredient.count());
    }

    private static net.minecraft.core.RegistryAccess registries() {
        var client = net.minecraft.client.Minecraft.getInstance();
        return client != null && client.level != null ? client.level.registryAccess()
                : net.minecraft.core.RegistryAccess.fromRegistryOfRegistries(net.minecraft.core.registries.BuiltInRegistries.REGISTRY);
    }

    public static EmiIngredient of(FluidIngredient ingredient) {
        return EmiIngredient.of(Arrays.stream(dev.emi.emi.recipe.NativeRecipeData.fluids(ingredient, registries())).map(NeoForgeEmiStack::of).toList());
    }

    public static EmiIngredient of(SizedFluidIngredient ingredient) {
        return of(ingredient.ingredient()).setAmount(ingredient.amount());
    }
}
