package com.lowdragmc.lowdraglib.transfer.recipe;

import java.util.stream.Stream;
import net.neoforged.neoforge.fluids.FluidStack;

/** Component-bearing representatives kept separate from native holder membership. */
public interface StackFluidIngredient {
    Stream<FluidStack> generateStacks();
}
