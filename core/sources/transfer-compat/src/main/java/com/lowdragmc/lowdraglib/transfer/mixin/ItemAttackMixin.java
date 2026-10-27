package com.lowdragmc.lowdraglib.transfer.mixin;
import com.lowdragmc.lowdraglib.transfer.combat.LegacyItemAttack;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(ItemStack.class)
public abstract class ItemAttackMixin {
    @Unique private boolean gttransfercompat$legacyAttackPending;
    @Inject(method="hurtEnemy",at=@At("HEAD"),cancellable=true)
    private void legacyAttack(LivingEntity target,LivingEntity attacker,CallbackInfoReturnable<Boolean> callback) {
        var stack=(ItemStack)(Object)this;
        var usedItem=stack.getItem();
        gttransfercompat$legacyAttackPending=usedItem instanceof LegacyItemAttack;
        if(usedItem instanceof LegacyItemAttack attack) {
            boolean result=attack.legacyHurtEnemy(stack,target,attacker);
            if(result && attacker instanceof Player player)player.awardStat(Stats.ITEM_USED.get(usedItem));
            callback.setReturnValue(result);
        }
    }
    @Inject(method="postHurtEnemy",at=@At("HEAD"),cancellable=true)
    private void legacyPostAttack(LivingEntity target,LivingEntity attacker,CallbackInfo callback) {
        var stack=(ItemStack)(Object)this;
        if(gttransfercompat$legacyAttackPending || stack.getItem() instanceof LegacyItemAttack) {
            gttransfercompat$legacyAttackPending=false;
            stack.getItem().postHurtEnemy(stack,target,attacker);
            callback.cancel();
        }
    }
}
