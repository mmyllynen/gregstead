package dev.emi.emi.api.widget;

import java.util.List;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;

public abstract class Widget implements Renderable {

	public abstract Bounds getBounds();

    public void tick() {}
    public boolean mouseScrolled(double x, double y, double horizontal, double vertical) { return false; }
    public boolean mouseDragged(net.minecraft.client.input.MouseButtonEvent event, double dx, double dy) { return false; }

    public Bounds getHoverBounds() { return getBounds(); }
	
	public abstract void extractRenderState(GuiGraphicsExtractor draw, int mouseX, int mouseY, float delta);

	public List<ClientTooltipComponent> getTooltip(int mouseX, int mouseY) {
		return List.of();
	}
	
	public boolean mouseClicked(net.minecraft.client.input.MouseButtonEvent event, boolean doubleClick) {
		return false;
	}

    public boolean mouseReleased(net.minecraft.client.input.MouseButtonEvent event) { return false; }

	public boolean keyPressed(net.minecraft.client.input.KeyEvent event) {
		return false;
	}
}
