package fi.gregstead.toolbeltslot.mixin;

import fi.gregstead.toolbeltslot.ToolbeltSlot;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Hud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Hud.class)
public abstract class HotbarIndicatorMixin {
    /** After the vanilla selection frame, before items, counts and durability bars. */
    @Inject(method = "extractItemHotbar", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;blitSprite(Lcom/mojang/renderpearl/api/pipeline/RenderPipeline;Lnet/minecraft/resources/Identifier;IIII)V",
        ordinal = 1, shift = At.Shift.AFTER))
    private void gregstead$markBoundSlot(GuiGraphicsExtractor graphics, DeltaTracker delta, CallbackInfo ci) {
        int slot = ToolbeltSlot.boundSlot();
        if (slot < 0) return;
        int x = graphics.guiWidth() / 2 - 90 + slot * 20;
        int y = graphics.guiHeight() - 21;
        int gold = 0xFFD4AF37;
        graphics.fill(x, y, x + 20, y + 1, gold);
        graphics.fill(x, y + 19, x + 20, y + 20, gold);
        graphics.fill(x, y + 1, x + 1, y + 19, gold);
        graphics.fill(x + 19, y + 1, x + 20, y + 19, gold);
    }
}
