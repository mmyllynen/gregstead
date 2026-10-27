package dev.latvian.mods.kubejs.block.drop;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import dev.latvian.mods.kubejs.util.NativeLootNumbers;
import net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProvider;
import org.jspecify.annotations.Nullable;

public record BlockDrops(ItemStack[] items, @Nullable ContextIntProvider rolls, @Nullable Item defaultItem) {
	public static final BlockDrops EMPTY = new BlockDrops(new ItemStack[0], null, null);

	public static BlockDrops createDefault(Item item) {
		return new BlockDrops(new ItemStack[0], NativeLootNumbers.exactly(1F), item);
	}

	public static BlockDrops createStack(ItemStack item) {
		return new BlockDrops(new ItemStack[]{item}, NativeLootNumbers.exactly(1F), null);
	}
}
