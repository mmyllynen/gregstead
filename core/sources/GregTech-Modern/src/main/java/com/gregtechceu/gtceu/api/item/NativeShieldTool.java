package com.gregtechceu.gtceu.api.item;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/** Stack-sensitive blocking behavior used by the native player combat path. */
public interface NativeShieldTool {
    boolean canDisableBlocking(ItemStack weapon, ItemStack shield, LivingEntity defender, LivingEntity attacker);
}
