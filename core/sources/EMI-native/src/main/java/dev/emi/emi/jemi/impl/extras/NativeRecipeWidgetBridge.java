package dev.emi.emi.jemi.impl.extras;

import dev.emi.emi.api.widget.Bounds;
import dev.emi.emi.api.widget.Widget;
import dev.emi.emi.jemi.impl.JemiTooltipBuilder;
import mezz.jei.api.gui.widgets.IRecipeWidget;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import java.util.List;

/** EMI traversal adapter preserving JEI's position-relative drawing and tooltip contract. */
public final class NativeRecipeWidgetBridge extends Widget {
    private final IRecipeWidget widget;
    private final int recipeWidth, recipeHeight;
    public NativeRecipeWidgetBridge(IRecipeWidget widget, int recipeWidth, int recipeHeight) {
        this.widget = widget;
        this.recipeWidth = recipeWidth;
        this.recipeHeight = recipeHeight;
    }
    @Override public void tick() { widget.tick(); }
    @Override public boolean mouseScrolled(double x, double y, double horizontal, double vertical) {
        return widget instanceof mezz.jei.api.gui.inputs.IJeiInputHandler handler
            && handler.handleMouseScrolled(x - widget.getPosition().x(), y - widget.getPosition().y(), horizontal, vertical);
    }
    @Override public boolean mouseClicked(net.minecraft.client.input.MouseButtonEvent event, boolean doubleClick) {
        return input(event, doubleClick, mezz.jei.common.input.InputPhase.PRESS);
    }
    @Override public boolean mouseReleased(net.minecraft.client.input.MouseButtonEvent event) {
        return input(event, false, mezz.jei.common.input.InputPhase.RELEASE);
    }
    private boolean input(net.minecraft.client.input.MouseButtonEvent event, boolean doubleClick, mezz.jei.common.input.InputPhase phase) {
        return widget instanceof mezz.jei.api.gui.inputs.IJeiInputHandler handler
            && mezz.jei.common.input.UserInput.fromVanilla(event, doubleClick, phase)
                .map(input -> handler.handleInput(event.x() - widget.getPosition().x(), event.y() - widget.getPosition().y(), input)).orElse(false);
    }
    @Override public boolean mouseDragged(net.minecraft.client.input.MouseButtonEvent event, double dx, double dy) {
        return widget instanceof mezz.jei.api.gui.inputs.IJeiInputHandler handler
            && handler.handleMouseDragged(event.x() - widget.getPosition().x(), event.y() - widget.getPosition().y(),
                com.mojang.blaze3d.platform.InputConstants.Type.MOUSE.getOrCreate(event.button()), dx, dy);
    }
    @Override public Bounds getBounds() {
        var rectangle = widget.getScreenRectangle();
        return rectangle == null ? new Bounds(0, 0, recipeWidth, recipeHeight)
            : new Bounds(rectangle.left(), rectangle.top(), rectangle.width(), rectangle.height());
    }
    @Override public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        var position = widget.getPosition();
        graphics.pose().pushMatrix();
        try {
            graphics.pose().translate(position.x(), position.y());
            widget.drawWidget(graphics, mouseX - position.x(), mouseY - position.y());
        } finally { graphics.pose().popMatrix(); }
    }
    @Override public List<ClientTooltipComponent> getTooltip(int mouseX, int mouseY) {
        if (mouseX < 0 || mouseY < 0 || mouseX >= recipeWidth || mouseY >= recipeHeight
                || !getBounds().contains(mouseX, mouseY)) return List.of();
        var position = widget.getPosition();
        var tooltip = new JemiTooltipBuilder();
        if (widget instanceof mezz.jei.api.gui.widgets.ISlottedRecipeWidget slotted) {
            slotted.getSlotUnderMouse(mouseX - position.x(), mouseY - position.y())
                .ifPresent(hovered -> hovered.slot().getTooltip(tooltip));
        }
        widget.getTooltip(tooltip, mouseX - position.x(), mouseY - position.y());
        return tooltip.components();
    }
}
