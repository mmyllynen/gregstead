package com.lowdragmc.lowdraglib.transfer.enchantment;
import net.minecraft.world.item.ItemStack;
public interface LegacyEnchantable {
    boolean isEnchantable(ItemStack stack);
    int getEnchantmentValue(ItemStack stack);
}
