package com.gregtechceu.gtceu.api.item.component;

import net.minecraft.core.Holder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;

public interface IEnchantableItem extends com.lowdragmc.lowdraglib.transfer.enchantment.EnchantmentSupport {

    boolean isEnchantable(ItemStack stack);

    int getEnchantmentValue(ItemStack stack);

    boolean supportsEnchantment(ItemStack stack, Holder<Enchantment> enchantment);
}
