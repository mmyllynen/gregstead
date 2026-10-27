package fi.neogreg.uifixes.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.IEventBus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** Core Lib 1.1.25 posts Foreground while still translated to the widget origin. */
@Mixin(targets = "com.supermartijn642.core.gui.WidgetContainerScreen", remap = false)
public abstract class WidgetContainerScreenMixin {
    @WrapOperation(
        method = "extractRenderState",
        at = @At(value = "INVOKE", target = "Lnet/neoforged/bus/api/IEventBus;post(Lnet/neoforged/bus/api/Event;)Lnet/neoforged/bus/api/Event;"),
        require = 1
    )
    private Event neogreg$foregroundInScreenCoordinates(IEventBus bus, Event event,
            Operation<Event> original, GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        // NeoForge 26.3 dispatches this event outside the container translation.
        // Restore the caller's transform even if another mod's listener throws.
        var pose = graphics.pose();
        pose.pushMatrix();
        try {
            pose.identity();
            return original.call(bus, event);
        } finally {
            pose.popMatrix();
        }
    }
}
