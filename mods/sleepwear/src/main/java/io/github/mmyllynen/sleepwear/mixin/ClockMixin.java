package io.github.mmyllynen.sleepwear.mixin;

import io.github.mmyllynen.sleepwear.SleepScheduler;
import net.minecraft.world.clock.ServerClockManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerClockManager.class)
public abstract class ClockMixin {
  @Inject(method = "modifyClock", at = @At("TAIL"))
  private void sleepwear$changed(CallbackInfo ci) {
    SleepScheduler.clocksChanged();
  }
}
