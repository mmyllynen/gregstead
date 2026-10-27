package dev.emi.emi.screen;

import dev.emi.emi.EmiPort;
import dev.emi.emi.config.EmiConfig;
import dev.emi.emi.runtime.EmiDrawContext;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastManager;
import net.minecraft.resources.Identifier;

public class DisabledToast implements Toast {
	private static final Identifier TEXTURE = EmiPort.id("toast/advancement");

    private Visibility wantedVisibility = Visibility.SHOW;

    @Override
    public Visibility getWantedVisibility() {
        return wantedVisibility;
    }

    @Override
    public void update(ToastManager manager, long time) {
        wantedVisibility = time > 8_000 || EmiConfig.enabled ? Visibility.HIDE : Visibility.SHOW;
    }

	@Override
	public void extractRenderState(GuiGraphicsExtractor raw, Font font, long time) {
		EmiDrawContext context = EmiDrawContext.wrap(raw);
		raw.blitSprite(RenderPipelines.GUI_TEXTURED, TEXTURE, 0, 0, this.width(), this.height());
		context.drawCenteredText(EmiPort.translatable("emi.disabled"), width() / 2, 7);
		context.drawCenteredText(EmiConfig.toggleVisibility.getBindText(), width() / 2, 18);
	}
}
