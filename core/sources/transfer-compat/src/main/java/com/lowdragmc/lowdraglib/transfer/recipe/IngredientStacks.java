package com.lowdragmc.lowdraglib.transfer.recipe;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.crafting.DataComponentIngredient;
import net.neoforged.neoforge.common.crafting.SizedIngredient;

/** Fresh representatives for recipes and lookup indexes, never a substitute for test(). */
public final class IngredientStacks {
    private IngredientStacks() {}

    public static Ingredient empty() {
        return EmptyIngredient.INSTANCE.toVanilla();
    }

    public static Ingredient of(ItemStack stack) {
        return stack.isEmpty() ? empty() : stack.isComponentsPatchEmpty()
                ? Ingredient.of(stack.getItem()) : DataComponentIngredient.of(true, stack);
    }

    public static Ingredient of(ItemStack... stacks) {
        return of(java.util.Arrays.stream(stacks));
    }

    public static Ingredient of(net.minecraft.world.level.ItemLike... items) {
        return Ingredient.of(items);
    }

    public static Ingredient of(net.minecraft.core.HolderSet<net.minecraft.world.item.Item> items) {
        return Ingredient.of(items);
    }

    public static Ingredient of(java.util.stream.Stream<?> values) {
        var children = values.map(value -> {
            if (value instanceof ItemStack stack) return of(stack);
            if (value instanceof net.minecraft.world.level.ItemLike item) return Ingredient.of(item);
            throw new IllegalArgumentException("Unsupported ingredient entry: " + value);
        }).toList();
        if (children.isEmpty()) return empty();
        return net.neoforged.neoforge.common.crafting.CompoundIngredient.of(children.toArray(Ingredient[]::new));
    }

    public static SizedIngredient sized(ItemStack stack) {
        return stack.isEmpty() ? new SizedIngredient(empty(), 1) : new SizedIngredient(of(stack), stack.getCount());
    }

    public static Ingredient of(net.minecraft.tags.TagKey<net.minecraft.world.item.Item> tag) {
        return Ingredient.of(TagReferences.get(net.minecraft.core.registries.BuiltInRegistries.ITEM, tag));
    }

    public static SizedIngredient sized(net.minecraft.tags.TagKey<net.minecraft.world.item.Item> tag, int count) {
        return new SizedIngredient(of(tag), count);
    }

    public static ItemStack[] getItems(Ingredient ingredient) {
        var custom = ingredient.getCustomIngredient();
        if (custom instanceof StackIngredient stacks) {
            return stacks.getItems().map(ItemStack::copy).toArray(ItemStack[]::new);
        }
        if (custom instanceof DataComponentIngredient components) {
            return components.items().map(item -> new ItemStack(item, 1, components.components()))
                    .toArray(ItemStack[]::new);
        }
        return ingredient.items().map(ItemStack::new).toArray(ItemStack[]::new);
    }

    public static ItemStack[] getItems(net.neoforged.neoforge.common.crafting.ICustomIngredient ingredient) {
        return getItems(ingredient.toVanilla());
    }

    public static ItemStack[] getItems(SizedIngredient ingredient) {
        var stacks = getItems(ingredient.ingredient());
        for (var stack : stacks) stack.setCount(ingredient.count());
        return stacks;
    }

    public static boolean hasNoItems(Ingredient ingredient) {
        return ingredient.isEmpty();
    }
}
