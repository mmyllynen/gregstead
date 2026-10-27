package com.gregtechceu.gtceu.client;

import com.tterrag.registrate.util.RegistrateItemColors;
import net.minecraft.world.item.ItemStack;

/** Native dynamic container fluid layer corresponds to original GregTech layer one. */
public final class NativeFluidItemTint {
    private NativeFluidItemTint() {}
    public static int apply(ItemStack stack, int nativeColor) {
        var policy = RegistrateItemColors.get(stack.getItem());
        return policy == null ? nativeColor : policy.getColor(stack, 1) | 0xff000000;
    }
}
