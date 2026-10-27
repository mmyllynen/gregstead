package dev.emi.emi.network;

import java.util.List;
import com.google.common.collect.Lists;
import dev.emi.emi.runtime.EmiLog;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/** Actual server recipe-fill execution, independent of viewer/render initialization. */
public final class NativeRecipeFill {
    private NativeRecipeFill() {}
    public static void apply(Player player, int syncId, int action, List<Integer> sourceIndices,
                             List<Integer> targetIndices, int outputIndex, List<ItemStack> stacks) {
		if (sourceIndices == null || targetIndices == null) {
			EmiLog.error("Client requested fill but passed input and crafting slot information was invalid, aborting");
			return;
		}
		AbstractContainerMenu handler = player.containerMenu;
		if (handler == null || handler.containerId != syncId) {
			EmiLog.warn("Client requested fill but screen handler has changed, aborting");
			return;
		}
		List<Slot> slots = Lists.newArrayList();
		List<Slot> crafting = Lists.newArrayList();
		Slot output = null;
		for (int i : sourceIndices) {
			if (i < 0 || i >= handler.slots.size()) {
				EmiLog.error("Client requested fill but passed input slots don't exist, aborting");
				return;
			}
			slots.add(handler.slots.get(i));
		}
		for (int i : targetIndices) {
			if (i >= 0 && i < handler.slots.size()) {
				crafting.add(handler.slots.get(i));
			} else {
				crafting.add(null);
			}
		}
		if (outputIndex != -1) {
			if (outputIndex >= 0 && outputIndex < handler.slots.size()) {
				output = handler.slots.get(outputIndex);
			}
		}
		if (crafting.size() >= stacks.size()) {
			List<ItemStack> rubble = Lists.newArrayList();
			for (int i = 0; i < crafting.size(); i++) {
				Slot s = crafting.get(i);
				if (s != null && s.mayPickup(player) && !s.getItem().isEmpty()) {
					ItemStack taken = s.getItem();
					rubble.add(taken.copy());
					s.setByPlayer(ItemStack.EMPTY);
					s.onTake(player, taken);
				}
			}
			try {	
				for (int i = 0; i < stacks.size(); i++) {
					ItemStack stack = stacks.get(i);
					if (stack.isEmpty()) {
						continue;
					}
					int gotten = grabMatching(player, slots, rubble, crafting, stack);
					if (gotten != stack.getCount()) {
						if (gotten > 0) {
							stack.setCount(gotten);
							player.getInventory().placeItemBackInInventory(stack, net.minecraft.util.Prediction.SERVER_ONLY);
						}
						return;
					} else {
						Slot s = crafting.get(i);
						if (s != null && s.mayPlace(stack) && stack.getCount() <= s.getMaxStackSize() && stack.getCount() <= stack.getMaxStackSize()) {
							if (!s.getItem().isEmpty()) { // Make sure we don't accidentally delete any items that could have been placed in this slot
								if (s.mayPickup(player)) {
									ItemStack taken = s.getItem();
									rubble.add(taken.copy());
									s.setByPlayer(ItemStack.EMPTY);
									s.onTake(player, taken);
								} else {
									player.getInventory().placeItemBackInInventory(stack, net.minecraft.util.Prediction.SERVER_ONLY);
									continue;
								}
							}
							s.setByPlayer(stack);
						} else {
							player.getInventory().placeItemBackInInventory(stack, net.minecraft.util.Prediction.SERVER_ONLY);
						}
					}
				}
				if (output != null) {
					if (action == 1) {
						handler.clicked(output.index, 0, ContainerInput.PICKUP, player);
					} else if (action == 2) {
						handler.clicked(output.index, 0, ContainerInput.QUICK_MOVE, player);
					}
				}
			} finally {
				for (ItemStack stack : rubble) {
					player.getInventory().placeItemBackInInventory(stack, net.minecraft.util.Prediction.SERVER_ONLY);
				}
			}
		}
	    }

	private static int grabMatching(Player player, List<Slot> slots, List<ItemStack> rubble, List<Slot> crafting, ItemStack stack) {
		int amount = stack.getCount();
		int grabbed = 0;
		for (int i = 0; i < rubble.size(); i++) {
			if (grabbed >= amount) {
				return grabbed;
			}
			ItemStack r = rubble.get(i);
			if (ItemStack.isSameItemSameComponents(stack, r)) {
				int wanted = amount - grabbed;
				if (r.getCount() <= wanted) {
					grabbed += r.getCount();
					rubble.remove(i);
					i--;
				} else {
					grabbed = amount;
					r.setCount(r.getCount() - wanted);
				}
			}
		}
		for (Slot s : slots) {
			if (grabbed >= amount) {
				return grabbed;
			}
			if (crafting.contains(s) || !s.mayPickup(player)) {
				continue;
			}
			ItemStack st = s.getItem();
			if (ItemStack.isSameItemSameComponents(stack, st)) {
				int wanted = amount - grabbed;
				ItemStack taken = st.copy();
				if (st.getCount() <= wanted) {
					grabbed += st.getCount();
					s.setByPlayer(ItemStack.EMPTY);
				} else {
					grabbed = amount;
					st.setCount(st.getCount() - wanted);
				}
				s.onTake(player, taken);
			}
		}
		return grabbed;
	}

}
