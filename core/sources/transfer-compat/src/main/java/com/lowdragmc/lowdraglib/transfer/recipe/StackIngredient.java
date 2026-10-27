package com.lowdragmc.lowdraglib.transfer.recipe;

import java.util.stream.Stream;
import net.minecraft.core.Holder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.neoforged.neoforge.common.crafting.ICustomIngredient;

/** Component-bearing representatives; matching remains the custom ingredient's test. */
public interface StackIngredient extends ICustomIngredient {
    Stream<ItemStack> getItems();

    @Override
    default Stream<Holder<Item>> items() {
        return getItems().filter(stack -> !stack.isEmpty()).map(ItemStack::typeHolder).distinct();
    }

    @Override
    default SlotDisplay display() {
        return new SlotDisplay.Composite(getItems().filter(stack -> !stack.isEmpty())
                .<SlotDisplay>map(stack -> new SlotDisplay.ItemStackSlotDisplay(
                        ItemStackTemplate.fromNonEmptyStack(stack))).toList());
    }
}
