package com.lowdragmc.lowdraglib.transfer.combat;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
public interface LegacyItemAttack {
    boolean legacyHurtEnemy(ItemStack stack,LivingEntity target,LivingEntity attacker);
}
