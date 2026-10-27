package com.gregtechceu.gtceu.api.recipe.ingredient;


import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.common.crafting.ICustomIngredient;
import net.neoforged.neoforge.common.crafting.IngredientType;
import net.neoforged.neoforge.fluids.FluidStack;
import com.lowdragmc.lowdraglib.transfer.fluid.IFluidHandler;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

import com.mojang.serialization.MapCodec;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.stream.Stream;

public class FluidContainerIngredient implements com.lowdragmc.lowdraglib.transfer.recipe.StackIngredient {

    // spotless:off
    public static final MapCodec<FluidContainerIngredient> CODEC = SizedFluidIngredient.CODEC.fieldOf("fluid")
            .xmap(FluidContainerIngredient::new, FluidContainerIngredient::getFluid);
    // spotless:on
    public static final IngredientType<FluidContainerIngredient> TYPE = new IngredientType<>(CODEC);
    @Getter
    private final SizedFluidIngredient fluid;

    public FluidContainerIngredient(SizedFluidIngredient fluid) {
        this.fluid = fluid;
    }

    public FluidContainerIngredient(TagKey<Fluid> tag, int amount) {
        this(com.lowdragmc.lowdraglib.transfer.recipe.FluidIngredientStacks.sized(tag, amount));
    }

    @NotNull
    @Override
    public Stream<ItemStack> getItems() {
        return Arrays.stream(com.lowdragmc.lowdraglib.transfer.recipe.FluidIngredientStacks.getFluids(this.fluid))
                .map(stack -> stack.getFluid().getBucket().getDefaultInstance())
                .filter(stack -> !stack.isEmpty());
    }

    @Override
    public boolean test(@Nullable ItemStack stack) {
        if (stack == null || stack.isEmpty())
            return false;
        return com.lowdragmc.lowdraglib.transfer.recipe.FluidContainerRecipe.matches(stack, fluid);
    }

    @Override
    public boolean isSimple() {
        return false;
    }

    @Override
    public @NotNull IngredientType<?> getType() {
        return TYPE;
    }

    public ItemStack getExtractedStack(ItemStack input) {
        return com.lowdragmc.lowdraglib.transfer.recipe.FluidContainerRecipe.extract(input, fluid);
    }

    @Override
    public boolean equals(Object other) {
        return this == other || other instanceof FluidContainerIngredient container && fluid.equals(container.fluid);
    }

    @Override
    public int hashCode() {
        return fluid.hashCode();
    }

    public boolean extractFrom(IFluidHandler handler, boolean simulate) {
        for (int tank = 0; tank < handler.getTanks(); tank++) {
            FluidStack inTank = handler.getFluidInTank(tank);
            if (fluid.test(inTank)) {
                FluidStack toExtract = inTank.copyWithAmount(fluid.amount());
                FluidStack extractedSim = handler.drain(toExtract, IFluidHandler.FluidAction.SIMULATE);
                if (extractedSim.getAmount() >= fluid.amount()) {
                    if (!simulate)
                        handler.drain(toExtract, IFluidHandler.FluidAction.EXECUTE);
                    return true;
                }
            }
        }
        return false;
    }
}
