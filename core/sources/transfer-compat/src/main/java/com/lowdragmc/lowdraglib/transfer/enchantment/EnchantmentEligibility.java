package com.lowdragmc.lowdraglib.transfer.enchantment;
import net.minecraft.core.Holder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import java.util.function.BooleanSupplier;
public final class EnchantmentEligibility {
    private EnchantmentEligibility() {}
    public static boolean anySupports(Iterable<?> components,ItemStack stack,Holder<Enchantment> enchantment) {
        for(var component:components)
            if(component instanceof EnchantmentSupport support && support.supportsEnchantment(stack,enchantment))return true;
        return false;
    }
    public static boolean primary(Iterable<?> components,ItemStack stack,Holder<Enchantment> enchantment,BooleanSupplier nativeSupports) {
        if(anySupports(components,stack,enchantment))return true;
        if(stack.is(Items.BOOK))return true;
        var primary=enchantment.value().definition().primaryItems();
        return nativeSupports.getAsBoolean() && (primary.isEmpty() || stack.is(primary.get()));
    }
}
