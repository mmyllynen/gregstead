package fi.neogreg.calm.mixin;

import fi.neogreg.calm.NeoGregCalm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Keep protected players out of combat candidate selection and continuation. */
@Mixin(Mob.class)
public abstract class MobCombatEligibilityMixin {
    @Inject(method = "canAttack(Lnet/minecraft/world/entity/LivingEntity;)Z",
            at = @At("HEAD"), cancellable = true, require = 1)
    private void neogreg$rejectProtectedPlayer(LivingEntity target, CallbackInfoReturnable<Boolean> result) {
        if (NeoGregCalm.blocksPlayerTarget((Mob) (Object) this, target)) {
            result.setReturnValue(false);
        }
    }
}
