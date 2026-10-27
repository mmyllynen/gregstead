package com.gregtechceu.gtceu.api.recipe.lookup.ingredient.item;

import com.gregtechceu.gtceu.api.recipe.ingredient.SizedIngredientExtensions;
import com.gregtechceu.gtceu.api.recipe.lookup.ingredient.AbstractMapIngredient;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.crafting.DataComponentIngredient;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.experimental.ExtensionMethod;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@ExtensionMethod(SizedIngredientExtensions.class)
public class ItemDataComponentMapIngredient extends ItemStackMapIngredient {

    protected DataComponentIngredient componentIngredient;

    public ItemDataComponentMapIngredient(ItemStack s, DataComponentIngredient ingredient, Ingredient vanilla) {
        super(s, vanilla);
        this.componentIngredient = ingredient;
    }

    @NotNull
    public static List<AbstractMapIngredient> from(@NotNull DataComponentIngredient ingredient) {
        Ingredient vanilla = ingredient.toVanilla();
        ObjectArrayList<AbstractMapIngredient> list = new ObjectArrayList<>();
        for (ItemStack s : com.lowdragmc.lowdraglib.transfer.recipe.IngredientStacks.getItems(vanilla)) {
            list.add(new ItemDataComponentMapIngredient(s, ingredient, vanilla));
        }
        return list;
    }

    @NotNull
    public static List<AbstractMapIngredient> from(@NotNull ItemStack stack) {
        ObjectArrayList<AbstractMapIngredient> list = new ObjectArrayList<>();

        Ingredient strict = DataComponentIngredient.of(true, stack);
        list.add(new ItemDataComponentMapIngredient(stack,
                (DataComponentIngredient) strict.getCustomIngredient(), strict));

        Ingredient partial = DataComponentIngredient.of(false, stack);
        list.add(new ItemDataComponentMapIngredient(stack,
                (DataComponentIngredient) partial.getCustomIngredient(), partial));

        return list;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof ItemDataComponentMapIngredient other) {
            if (!ItemStack.isSameItem(this.stack, other.stack)) {
                return false;
            }
            if (this.componentIngredient == other.componentIngredient) {
                return true;
            }

            if (this.componentIngredient != null) {
                if (other.componentIngredient != null) {
                    if (this.componentIngredient.componentsExhaustive() != other.componentIngredient.componentsExhaustive()) {
                        return false;
                    }
                    if (!this.componentIngredient.components().equals(other.componentIngredient.components())) {
                        return false;
                    }

                    if (this.componentIngredient.componentsExhaustive()) {
                        for (ItemStack tStack : com.lowdragmc.lowdraglib.transfer.recipe.IngredientStacks.getItems(this.ingredient)) {
                            for (ItemStack oStack : com.lowdragmc.lowdraglib.transfer.recipe.IngredientStacks.getItems(other.ingredient)) {
                                if (ItemStack.isSameItemSameComponents(tStack, oStack)) return true;
                            }
                        }
                    } else {
                        boolean thisContains = this.componentIngredient.itemSet()
                                .stream().allMatch(holder -> other.componentIngredient.itemSet().contains(holder));
                        boolean otherContains = other.componentIngredient.itemSet()
                                .stream().allMatch(holder -> this.componentIngredient.itemSet().contains(holder));
                        return thisContains && otherContains;
                    }
                } else {
                    return this.componentIngredient.test(other.stack);
                }
            } else {
                return other.componentIngredient.test(this.stack);
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return "DataComponentItemStackMapIngredient{item=" + BuiltInRegistries.ITEM.getKey(stack.getItem()) + "}";
    }

    @Override
    public boolean isSpecialIngredient() {
        return true;
    }
}
