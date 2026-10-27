package fi.neogreg.uifixes.mixin;

import fi.neogreg.uifixes.InventoryProfilesLayout;
import fi.neogreg.uifixes.ButtonRails;
import fi.neogreg.uifixes.ModularUiLayoutAccess;
import java.util.List;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.neoforged.fml.ModList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "com.lowdragmc.lowdraglib.gui.modular.ModularUIGuiContainer", remap = false)
public abstract class ModularUiExclusionMixin extends AbstractContainerScreen<AbstractContainerMenu> implements ModularUiLayoutAccess {
    @Shadow public abstract List<Rect2i> getGuiExtraAreas();

    protected ModularUiExclusionMixin(AbstractContainerMenu menu, Inventory inventory, Component title, int width, int height) {
        super(menu, inventory, title, width, height);
    }

    @Override
    public ButtonRails.Rect neogreg$panelBounds() {
        return new ButtonRails.Rect(leftPos, topPos, imageWidth, imageHeight);
    }

    @Override
    public void neogreg$refreshButtonLayout() { getGuiExtraAreas(); }

    @Inject(method = "getGuiExtraAreas", at = @At("RETURN"), cancellable = true, require = 1)
    private void neogreg$reserveButtonRails(CallbackInfoReturnable<List<Rect2i>> cir) {
        if (ModList.get().isLoaded("inventoryprofilesnext")) {
            cir.setReturnValue(InventoryProfilesLayout.layout((AbstractContainerScreen<?>) (Object) this, cir.getReturnValue()));
        }
    }
}
