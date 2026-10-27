package com.lowdragmc.lowdraglib.transfer.enchantment;
import net.minecraft.core.Holder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
public interface EnchantmentSupport { boolean supportsEnchantment(ItemStack stack,Holder<Enchantment> enchantment); }
