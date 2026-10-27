package fi.neogreg.uifixes.mixin;

import fi.neogreg.uifixes.BackpackLayout;
import fi.neogreg.uifixes.ButtonRails;
import fi.neogreg.uifixes.ContainerLayoutAccess;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.p3pp3rf1y.sophisticatedbackpacks.client.gui.BackpackScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;

@Pseudo
@Mixin(targets = "net.p3pp3rf1y.sophisticatedbackpacks.client.gui.BackpackScreen", remap = false)
public abstract class BackpackLayoutMixin extends AbstractContainerScreen<AbstractContainerMenu> implements ContainerLayoutAccess {
    protected BackpackLayoutMixin(AbstractContainerMenu menu, Inventory inventory, Component title, int width, int height) {
        super(menu, inventory, title, width, height);
    }

    @Override
    public ButtonRails.Rect neogreg$panelBounds() {
        return new ButtonRails.Rect(leftPos, topPos, imageWidth, imageHeight);
    }

    @Override
    public void neogreg$refreshButtonLayout() { BackpackLayout.layout((BackpackScreen) (Object) this); }
}
