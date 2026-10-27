package com.lowdragmc.lowdraglib.transfer.recipe;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.jetbrains.annotations.Nullable;

/** Tests/drains a detached single crafting item and permits container item replacement. */
public final class FluidContainerRecipe {
    private FluidContainerRecipe() {}

    public static boolean matches(ItemStack input, SizedFluidIngredient ingredient) {
        return drain(input, ingredient, true) != null;
    }

    public static ItemStack extract(ItemStack input, SizedFluidIngredient ingredient) {
        var remainder = drain(input, ingredient, false);
        return remainder == null ? input : remainder;
    }

    @SuppressWarnings("deprecation")
    private static @Nullable ItemStack drain(ItemStack input, SizedFluidIngredient ingredient, boolean simulate) {
        if (input.isEmpty()) return null;
        // forStack cannot swap a water bucket into an empty bucket. Use an owning slot instead.
        var slot = new ItemStacksResourceHandler(NonNullList.of(ItemStack.EMPTY, input.copyWithCount(1)));
        var access = ItemAccess.forHandlerIndexStrict(slot, 0);
        var fluids = access.getCapability(Capabilities.Fluid.ITEM);
        if (fluids == null) return null;
        for (int tank = 0; tank < fluids.size(); tank++) {
            var resource = fluids.getResource(tank);
            if (resource.isEmpty() || !ingredient.test(resource.toStack(fluids.getAmountAsInt(tank)))) continue;
            try (var transaction = Transaction.open(Transaction.getCurrentOpenedTransaction())) {
                if (fluids.extract(resource, ingredient.amount(), transaction) != ingredient.amount()) continue;
                var result = slot.getResource(0).toStack(slot.getAmountAsInt(0));
                if (!simulate) transaction.commit();
                return result;
            }
        }
        return null;
    }
}
