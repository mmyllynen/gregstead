package com.lowdragmc.lowdraglib.transfer.food;

import com.lowdragmc.lowdraglib.transfer.interaction.UseResult;
import com.lowdragmc.lowdraglib.transfer.item.wrapper.InvWrapper;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.EventHooks;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.jetbrains.annotations.Nullable;

/** Automatic armor consumption retaining native food effects and returned item stacks. */
public final class AutomaticFood {
    private AutomaticFood() {}
    public static UseResult<ItemStack> eat(Player player, ItemStack food) {
        if (food.get(DataComponents.FOOD) == null || !player.getFoodData().needsFood()) return UseResult.fail(food);
        ItemStack original = food.copy();
        ItemStack result = EventHooks.onItemUseFinish(player, original, player.getUseItemRemainingTicks(),
                food.finishUsingItem(player.level(), player));
        return UseResult.success(result);
    }
    public static @Nullable UseResult<ItemStack> first(Player player) {
        if (Transaction.getLifecycle() == Transaction.Lifecycle.OPEN || Transaction.getLifecycle() == Transaction.Lifecycle.CLOSING)
            throw new IllegalStateException("Automatic food effects cannot run inside a transfer transaction");
        var inventory = new InvWrapper(player.getInventory());
        for (int slot = 0; slot < inventory.getSlots(); slot++) {
            ItemStack stack = inventory.getStackInSlot(slot);
            if (stack.get(DataComponents.FOOD) != null) {
                var result = eat(player, stack);
                inventory.setStackInSlot(slot, result.getObject());
                return result;
            }
        }
        return null;
    }
}
