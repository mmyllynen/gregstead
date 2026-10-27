package com.gregtechceu.gtceu.core.mixins;

import com.gregtechceu.gtceu.api.item.NativeShieldTool;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/** Original stack-sensitive tool behaviors feed the native blocking cooldown path. */
@Mixin(Player.class)
public abstract class PlayerToolShieldMixin {
    @Redirect(method = "blockUsingItem", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/LivingEntity;getSecondsToDisableBlocking()F"))
    private float gtceu$toolBlockingCooldown(LivingEntity attacker) {
        Player defender = (Player) (Object) this;
        ItemStack shield = defender.getItemBlockingWith();
        ItemStack weapon = attacker.getWeaponItem();
        if (shield != null && weapon.getItem() instanceof NativeShieldTool tool) {
            return tool.canDisableBlocking(weapon, shield, defender, attacker) ? 5.0F : 0.0F;
        }
        return attacker.getSecondsToDisableBlocking();
    }
}
