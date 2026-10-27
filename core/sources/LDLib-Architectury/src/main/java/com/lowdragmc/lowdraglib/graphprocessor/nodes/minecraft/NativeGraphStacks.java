package com.lowdragmc.lowdraglib.graphprocessor.nodes.minecraft;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;

/** Native graph stack construction retains detached inputs and override order. */
public final class NativeGraphStacks {
    private NativeGraphStacks() {}
    public static ItemStack item(Object input, ItemStack defaultValue, Item item, Integer count,
                                 DataComponentPatch components, HolderLookup.Provider registries) {
        ItemStack result;
        if (input == null) result = defaultValue.copy();
        else if (input instanceof ItemStack stack) result = stack.copy();
        else if (input instanceof CompoundTag tag) result = ItemStack.OPTIONAL_CODEC.parse(registries.createSerializationContext(NbtOps.INSTANCE), tag).result().orElse(ItemStack.EMPTY);
        else result = new ItemStack(Items.AIR);
        if (item != null) result = new ItemStack(item, result.getCount());
        if (count != null) result.setCount(count);
        if (components != null && !result.isEmpty()) result.applyComponents(components);
        return result;
    }
    public static FluidStack fluid(Object input, FluidStack defaultValue, Fluid fluid, Integer amount,
                                   DataComponentPatch components, HolderLookup.Provider registries) {
        FluidStack result;
        if (input == null) result = defaultValue.copy();
        else if (input instanceof FluidStack stack) result = stack.copy();
        else if (input instanceof CompoundTag tag) result = FluidStack.OPTIONAL_CODEC.parse(registries.createSerializationContext(NbtOps.INSTANCE), tag).getOrThrow();
        else result = FluidStack.EMPTY;
        if (fluid != null) result = new FluidStack(fluid, result.getAmount());
        if (amount != null) result.setAmount(amount);
        if (components != null) result.applyComponents(components);
        return result;
    }
}
