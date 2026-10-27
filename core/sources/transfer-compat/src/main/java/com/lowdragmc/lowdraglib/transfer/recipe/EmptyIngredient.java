package com.lowdragmc.lowdraglib.transfer.recipe;

import com.mojang.serialization.MapCodec;
import java.util.stream.Stream;
import net.minecraft.core.Holder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.crafting.ICustomIngredient;
import net.neoforged.neoforge.common.crafting.IngredientType;

/** Explicit empty sentinel for content/GUI defaults; never matches a recipe input. */
public final class EmptyIngredient implements ICustomIngredient {
    public static final EmptyIngredient INSTANCE = new EmptyIngredient();
    public static final MapCodec<EmptyIngredient> CODEC = MapCodec.unit(INSTANCE);
    public static final IngredientType<EmptyIngredient> TYPE = new IngredientType<>(CODEC);
    private EmptyIngredient() {}
    @Override public boolean test(ItemStack stack) { return false; }
    @Override public Stream<Holder<Item>> items() { return Stream.empty(); }
    @Override public boolean isSimple() { return true; }
    @Override public IngredientType<?> getType() { return TYPE; }
}
