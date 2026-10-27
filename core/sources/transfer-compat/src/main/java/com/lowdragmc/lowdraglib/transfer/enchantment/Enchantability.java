package com.lowdragmc.lowdraglib.transfer.enchantment;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantable;
import org.jetbrains.annotations.Nullable;
public final class Enchantability {
    private Enchantability() {}
    public static boolean defaultPredicate(ItemStack stack) {
        var defaults=stack.getItem().components();
        return defaults.getOrDefault(DataComponents.MAX_STACK_SIZE,1)==1
                && defaults.getOrDefault(DataComponents.MAX_DAMAGE,0)>0;
    }
    public static @Nullable Enchantable component(ItemStack stack) {
        if(!stack.getComponentsPatch().isPatched(DataComponents.ENCHANTABLE) && stack.getItem() instanceof LegacyEnchantable source) {
            int value=source.getEnchantmentValue(stack);
            return value>0?new Enchantable(value):null;
        }
        return stack.get(DataComponents.ENCHANTABLE);
    }
}
