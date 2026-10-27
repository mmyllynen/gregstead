package com.gregtechceu.gtceu.api.recipe.ingredient;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.Holder;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.FluidIngredient;
import net.neoforged.neoforge.fluids.crafting.FluidIngredientType;
import java.util.stream.Stream;

/** Explicit nonmatching default content, rather than an invalid native simple holder set. */
public final class EmptyFluidIngredient extends FluidIngredient {
    public static final EmptyFluidIngredient INSTANCE = new EmptyFluidIngredient();
    public static final MapCodec<EmptyFluidIngredient> CODEC = MapCodec.unit(INSTANCE);
    public static final FluidIngredientType<EmptyFluidIngredient> TYPE = new FluidIngredientType<>(CODEC);
    private EmptyFluidIngredient() {}
    @Override public boolean test(FluidStack stack) { return false; }
    @Override protected Stream<Holder<Fluid>> generateFluids() { return Stream.empty(); }
    @Override public boolean isSimple() { return false; }
    @Override public FluidIngredientType<?> getType() { return TYPE; }
    @Override public int hashCode() { return EmptyFluidIngredient.class.hashCode(); }
    @Override public boolean equals(Object other) { return other instanceof EmptyFluidIngredient; }
}
