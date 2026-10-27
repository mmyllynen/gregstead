package fi.neogreg.uifixes.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;

/** Screen-init listeners must see the new menu, not the previously open inventory. */
@Pseudo
@Mixin(targets = "com.lowdragmc.lowdraglib.gui.factory.ClientUIFactorySupport", remap = false)
public abstract class ModularUiOpenMixin {
    @WrapOperation(method = "open", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/client/gui/Gui;setScreen(Lnet/minecraft/client/gui/screens/Screen;)V"), require = 1)
    private static void neogreg$bindMenuBeforeScreenInit(Gui gui, Screen screen, Operation<Void> original) {
        var player = Minecraft.getInstance().player;
        var menu = ((AbstractContainerScreen<?>) screen).getMenu();
        var previous = player.containerMenu;
        player.containerMenu = menu;
        try {
            original.call(gui, screen);
        } catch (RuntimeException | Error failure) {
            if (player.containerMenu == menu) player.containerMenu = previous;
            throw failure;
        }
    }
}
