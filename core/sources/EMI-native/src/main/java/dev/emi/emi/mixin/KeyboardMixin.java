package dev.emi.emi.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.emi.emi.runtime.EmiLog;
import dev.emi.emi.screen.EmiScreenManager;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(KeyboardHandler.class)
public class KeyboardMixin {
    @WrapOperation(method = "keyPress(JILnet/minecraft/client/input/KeyEvent;)V", at = @At(value = "INVOKE", target =
        "Lnet/minecraft/client/gui/screens/Screen;keyPressed(Lnet/minecraft/client/input/KeyEvent;)Z"))
    private boolean emi$keyPress(Screen screen, KeyEvent event, Operation<Boolean> original) {
        try {
            if (screen instanceof AbstractContainerScreen<?> && EmiScreenManager.keyPressed(event)) return true;
        } catch (Exception e) {
            EmiLog.error("Error while handling key press", e);
        }
        return original.call(screen, event);
    }

    @WrapOperation(method = "charTyped(JLnet/minecraft/client/input/CharacterEvent;)V", at = @At(value = "INVOKE", target =
        "Lnet/minecraft/client/gui/screens/Screen;charTyped(Lnet/minecraft/client/input/CharacterEvent;)Z"))
    private boolean emi$charTyped(Screen screen, CharacterEvent event, Operation<Boolean> original) {
        try {
            if (screen instanceof AbstractContainerScreen<?> && EmiScreenManager.search.charTyped(event)) return true;
        } catch (Exception e) {
            EmiLog.error("Error while handling char", e);
        }
        return original.call(screen, event);
    }
}
