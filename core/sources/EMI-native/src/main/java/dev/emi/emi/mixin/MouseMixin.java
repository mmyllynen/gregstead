package dev.emi.emi.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.emi.emi.runtime.EmiLog;
import dev.emi.emi.screen.EmiScreenManager;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.MouseButtonEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(MouseHandler.class)
public class MouseMixin {
    @WrapOperation(method = "onButton(JLnet/minecraft/client/input/MouseButtonInfo;I)V", at = @At(value = "INVOKE", target =
        "Lnet/minecraft/client/gui/screens/Screen;mouseClicked(Lnet/minecraft/client/input/MouseButtonEvent;Z)Z"))
    private boolean emi$click(Screen screen, MouseButtonEvent event, boolean doubleClick, Operation<Boolean> original) {
        try {
            if (screen instanceof AbstractContainerScreen<?> && EmiScreenManager.mouseClicked(event, doubleClick)) return true;
        } catch (Exception e) {
            EmiLog.error("Error while handling mouse press", e);
        }
        return original.call(screen, event, doubleClick);
    }

    @WrapOperation(method = "onButton(JLnet/minecraft/client/input/MouseButtonInfo;I)V", at = @At(value = "INVOKE", target =
        "Lnet/minecraft/client/gui/screens/Screen;mouseReleased(Lnet/minecraft/client/input/MouseButtonEvent;)Z"))
    private boolean emi$release(Screen screen, MouseButtonEvent event, Operation<Boolean> original) {
        try {
            if (screen instanceof AbstractContainerScreen<?> && EmiScreenManager.mouseReleased(event)) return true;
        } catch (Exception e) {
            EmiLog.error("Error while handling mouse release", e);
        }
        return original.call(screen, event);
    }

    @WrapOperation(method = "handleAccumulatedMovement()V", at = @At(value = "INVOKE", target =
        "Lnet/minecraft/client/gui/screens/Screen;mouseDragged(Lnet/minecraft/client/input/MouseButtonEvent;DD)Z"))
    private boolean emi$drag(Screen screen, MouseButtonEvent event, double dx, double dy, Operation<Boolean> original) {
        try {
            if (screen instanceof AbstractContainerScreen<?> && EmiScreenManager.mouseDragged(event, dx, dy)) return true;
        } catch (Exception e) {
            EmiLog.error("Error while handling mouse drag", e);
        }
        return original.call(screen, event, dx, dy);
    }

    @WrapOperation(method = "onScroll(JDD)V", at = @At(value = "INVOKE", target =
        "Lnet/minecraft/client/gui/screens/Screen;mouseScrolled(DDDD)Z"))
    private boolean emi$scroll(Screen screen, double x, double y, double horizontal, double vertical, Operation<Boolean> original) {
        try {
            if (screen instanceof AbstractContainerScreen<?> && EmiScreenManager.mouseScrolled(x, y, vertical)) return true;
        } catch (Exception e) {
            EmiLog.error("Error while handling mouse scroll", e);
        }
        return original.call(screen, x, y, horizontal, vertical);
    }
}
