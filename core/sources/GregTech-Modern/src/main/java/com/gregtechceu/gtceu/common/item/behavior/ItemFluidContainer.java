package com.gregtechceu.gtceu.common.item.behavior;

import com.gregtechceu.gtceu.api.item.component.IRecipeRemainder;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidType;
import com.lowdragmc.lowdraglib.transfer.fluid.FluidItemProbe;
import com.lowdragmc.lowdraglib.transfer.fluid.IFluidHandler.FluidAction;

public class ItemFluidContainer implements IRecipeRemainder {

    @Override
    public ItemStack getRecipeRemained(ItemStack itemStack) {
        return java.util.Optional.ofNullable(FluidItemProbe.handler(itemStack)).map(handler -> {
            var drained = handler.drain(FluidType.BUCKET_VOLUME, FluidAction.SIMULATE);
            if (drained.getAmount() != FluidType.BUCKET_VOLUME) return ItemStack.EMPTY;
            handler.drain(FluidType.BUCKET_VOLUME, FluidAction.EXECUTE);
            return ItemStack.EMPTY;
        }).orElse(itemStack);
    }
}
