package com.lowdragmc.lowdraglib.transfer.mixin;
import com.lowdragmc.lowdraglib.transfer.enchantment.Enchantability;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
@Mixin(EnchantmentHelper.class)
public abstract class EnchantmentSelectionMixin {
    @Redirect(method={"getEnchantmentCost","selectEnchantment"},at=@At(value="INVOKE",target="Lnet/minecraft/world/item/ItemStack;get(Lnet/minecraft/core/component/DataComponentType;)Ljava/lang/Object;"))
    private static Object legacyEnchantability(ItemStack stack,DataComponentType<?> type) {
        return type==DataComponents.ENCHANTABLE?Enchantability.component(stack):stack.get(type);
    }
}
