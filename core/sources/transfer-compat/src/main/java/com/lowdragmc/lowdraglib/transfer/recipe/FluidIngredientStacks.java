package com.lowdragmc.lowdraglib.transfer.recipe;

import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.fluids.crafting.DataComponentFluidIngredient;
import net.neoforged.neoforge.fluids.crafting.FluidIngredient;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

/** Fresh output/index representatives. Input matching always uses the ingredient predicate. */
public final class FluidIngredientStacks {
    private FluidIngredientStacks() {}

    public static FluidStack[] getStacks(FluidIngredient ingredient) {
        if (ingredient instanceof StackFluidIngredient stacks) {
            return stacks.generateStacks().map(FluidStack::copy).toArray(FluidStack[]::new);
        }
        if (ingredient instanceof DataComponentFluidIngredient components) {
            return components.fluidSet().stream()
                    .map(fluid -> new FluidStack(fluid, FluidType.BUCKET_VOLUME, components.components().asPatch()))
                    .toArray(FluidStack[]::new);
        }
        if (ingredient instanceof net.neoforged.neoforge.fluids.crafting.CompoundFluidIngredient compound) {
            return compound.children().stream().flatMap(child -> java.util.Arrays.stream(getStacks(child)))
                    .toArray(FluidStack[]::new);
        }
        if (ingredient instanceof net.neoforged.neoforge.fluids.crafting.IntersectionFluidIngredient intersection) {
            return intersection.children().stream().flatMap(child -> java.util.Arrays.stream(getStacks(child)))
                    .filter(intersection::test).toArray(FluidStack[]::new);
        }
        if (ingredient instanceof net.neoforged.neoforge.fluids.crafting.DifferenceFluidIngredient difference) {
            return java.util.Arrays.stream(getStacks(difference.base()))
                    .filter(difference::test).toArray(FluidStack[]::new);
        }
        return ingredient.fluids().stream().map(fluid -> new FluidStack(fluid, FluidType.BUCKET_VOLUME))
                .toArray(FluidStack[]::new);
    }

    public static FluidStack[] getFluids(SizedFluidIngredient ingredient) {
        var stacks = getStacks(ingredient.ingredient());
        for (var stack : stacks) stack.setAmount(ingredient.amount());
        return stacks;
    }

    public static FluidIngredient of(FluidStack... stacks) {
        var children = java.util.Arrays.stream(stacks)
                .filter(stack -> !stack.isEmpty())
                // Plain stacks identify a fluid, not an exhaustive component constraint.
                // Keep explicit patches exact, as the item ingredient adapter does.
                .map(stack -> stack.isComponentsPatchEmpty() ? FluidIngredient.of(stack.getFluid())
                        : DataComponentFluidIngredient.of(true, stack)).toList();
        if (children.isEmpty()) return FluidIngredient.of(net.minecraft.core.HolderSet.empty());
        return net.neoforged.neoforge.fluids.crafting.CompoundFluidIngredient.of(children);
    }

    public static FluidIngredient of(net.minecraft.tags.TagKey<net.minecraft.world.level.material.Fluid> tag) {
        return of(net.minecraft.core.registries.BuiltInRegistries.FLUID, tag);
    }

    public static FluidIngredient of(net.minecraft.core.Registry<net.minecraft.world.level.material.Fluid> registry,
                                     net.minecraft.tags.TagKey<net.minecraft.world.level.material.Fluid> tag) {
        var values = TagReferences.get(registry, tag);
        var ingredient = FluidIngredient.of(values);
        values.addInvalidationListener(() -> invalidate(ingredient));
        return ingredient;
    }

    public static SizedFluidIngredient sized(FluidStack stack) {
        return new SizedFluidIngredient(of(stack), stack.getAmount());
    }

    public static SizedFluidIngredient sized(net.minecraft.world.level.material.Fluid fluid, int amount) {
        return SizedFluidIngredient.of(fluid, amount);
    }

    public static SizedFluidIngredient sized(net.minecraft.tags.TagKey<net.minecraft.world.level.material.Fluid> tag,
                                            int amount) {
        return new SizedFluidIngredient(of(tag), amount);
    }

    public static void invalidate(FluidIngredient ingredient) {
        ((com.lowdragmc.lowdraglib.transfer.mixin.FluidIngredientCacheAccessor) ingredient)
                .gttransfercompat$setFluids(null);
    }

    public static boolean hasNoFluids(FluidIngredient ingredient) {
        return ingredient.fluids().isEmpty();
    }
}
