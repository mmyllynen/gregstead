package dev.latvian.mods.kubejs.item.creativetab;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.function.Supplier;

@FunctionalInterface
public interface CreativeTabIconSupplier {
	CreativeTabIconSupplier DEFAULT = () -> ItemStack.EMPTY;

	record Wrapper(CreativeTabIconSupplier supplier) implements Supplier<ItemStack> {
		@Override
		public ItemStack get() {
			try {
				var i = supplier.getIcon();
				return i.isEmpty() ? Items.DYE.pick(net.minecraft.world.item.DyeColor.PURPLE).getDefaultInstance() : i;
			} catch (Exception ex) {
				ex.printStackTrace();
			}

			return Items.DYE.pick(net.minecraft.world.item.DyeColor.PURPLE).getDefaultInstance();
		}
	}

	ItemStack getIcon();
}
