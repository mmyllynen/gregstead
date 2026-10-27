package com.gregtechceu.gtceu.api.recipe.ingredient;


import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.registries.codec.HolderSetCodec;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.crafting.DataComponentIngredient;
import net.neoforged.neoforge.common.crafting.IngredientType;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Data component ingredient that clears the item cache when tags are reloaded.
 *
 * @see DataComponentIngredient DataComponentIngredient, the parent class
 * @see ExDataComponentFluidIngredient ExDataComponentFluidIngredient, its fluid equivalent
 */
public class ExDataComponentIngredient extends DataComponentIngredient implements com.lowdragmc.lowdraglib.transfer.recipe.StackIngredient {

    // spotless:off
    public static final MapCodec<ExDataComponentIngredient> CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            HolderSetCodec.create(Registries.ITEM, BuiltInRegistries.ITEM.holderByNameCodec(), false).fieldOf("items").forGetter(ExDataComponentIngredient::itemSet),
            DataComponentPatch.CODEC.fieldOf("components").forGetter(ExDataComponentIngredient::components),
            Codec.BOOL.optionalFieldOf("strict", false).forGetter(ExDataComponentIngredient::componentsExhaustive)
    ).apply(builder, ExDataComponentIngredient::new));
    // spotless:on

    public static final IngredientType<ExDataComponentIngredient> TYPE = new IngredientType<>(CODEC);

    private final @NotNull List<ItemStack> stacks;

    public ExDataComponentIngredient(HolderSet<Item> items, DataComponentPatch components, boolean strict) {
        super(items, components, strict);
        this.stacks = items.stream()
                .map(i -> new ItemStack(i, 1, components))
                .collect(Collectors.toCollection(ArrayList::new));
        items.addInvalidationListener(this.stacks::clear);
    }

    private List<ItemStack> regenerateStacksIfEmpty() {
        if (this.stacks.isEmpty()) {
            this.itemSet().stream()
                    .map(i -> new ItemStack(i, 1, components()))
                    .forEach(this.stacks::add);
        }
        return this.stacks;
    }

    @Override
    public boolean test(@NotNull ItemStack stack) {
        if (this.componentsExhaustive()) {
            for (ItemStack stack2 : this.regenerateStacksIfEmpty()) {
                if (ItemStack.isSameItemSameComponents(stack, stack2)) return true;
            }
            return false;
        } else {
            return super.test(stack);
        }
    }

    @Override
    public @NotNull Stream<ItemStack> getItems() {
        return this.regenerateStacksIfEmpty().stream();
    }

    @Override
    public @NotNull IngredientType<?> getType() {
        return TYPE;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ExDataComponentIngredient)) return false;
        return super.equals(obj);
    }

    private static DataComponentPatch asPatch(DataComponentMap map) {
        var builder = DataComponentPatch.builder();
        for (var component : map) builder.set(component);
        return builder.build();
    }

    /**
     * Creates a new ingredient matching any item from the list, containing the given components
     */
    public static @NotNull Ingredient of(boolean strict, DataComponentMap map, HolderSet<Item> items) {
        return of(strict, asPatch(map), items);
    }

    /**
     * Creates a new ingredient matching any item in the tag, containing the given components
     */
    public static @NotNull Ingredient of(boolean strict, DataComponentPatch predicate, TagKey<Item> tag) {
        return of(strict, predicate, com.lowdragmc.lowdraglib.transfer.recipe.TagReferences.get(BuiltInRegistries.ITEM, tag));
    }

    /**
     * Creates a new ingredient matching any item from the list, containing the given components
     */
    public static @NotNull Ingredient of(boolean strict, DataComponentPatch predicate, HolderSet<Item> items) {
        return new ExDataComponentIngredient(items, predicate, strict).toVanilla();
    }
}
