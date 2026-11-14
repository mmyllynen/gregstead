package io.github.mmyllynen.sleepwear.mixin;

import io.github.mmyllynen.sleepwear.SleepScheduler;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Level.class)
public abstract class DarknessMixin {
  @Unique private boolean sleepwear$dark;

  @Inject(method = "updateSkyBrightness", at = @At("TAIL"))
  private void sleepwear$changed(CallbackInfo ci) {
    Level level = (Level) (Object) this;
    boolean now = level.isDarkOutside();
    if (now != sleepwear$dark) {
      sleepwear$dark = now;
      SleepScheduler.darknessChanged(level);
    }
  }
}
