package dev.emi.emi.recipe;

import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;

/** Anvil eligibility follows native registered holders and NeoForge item overrides. */
public final class NativeEnchantmentSupport {
    private NativeEnchantmentSupport() {}
    public static boolean supports(ItemStack stack, Enchantment enchantment, RegistryAccess registries) {
        return registries.lookupOrThrow(Registries.ENCHANTMENT).listElements()
            .filter(holder -> holder.value() == enchantment).anyMatch(stack::supportsEnchantment);
    }
}
