package com.lowdragmc.lowdraglib.integration.emi;

import com.lowdragmc.lowdraglib.gui.ingredient.IRecipeIngredientSlot;
import com.lowdragmc.lowdraglib.gui.util.DrawerHelper;
import com.lowdragmc.lowdraglib.gui.widget.DraggableScrollableWidgetGroup;
import com.lowdragmc.lowdraglib.integration.jei.ModularWrapper;
import dev.emi.emi.api.widget.Bounds;
import dev.emi.emi.api.widget.Widget;
import lombok.Getter;
import lombok.Setter;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author KilaBash
 * @date 2023/4/3
 * @implNote ModularWrapperWidget
 */
@OnlyIn(Dist.CLIENT)
public class ModularWrapperWidget extends Widget implements ContainerEventHandler {
    @Getter @Setter
    @Nullable
    private GuiEventListener focused;
    @Getter @Setter
    private boolean isDragging;
    public final ModularWrapper<?> modular;
    public final List<Widget> slots;

    public ModularWrapperWidget(ModularWrapper<?> modular, List<Widget> slots) {
        this.modular = modular;
        this.slots = slots;
    }

    @Override
    public Bounds getBounds() {
        return new Bounds(0, 0, modular.getWidget().getSize().width, modular.getWidget().getSize().height);
    }

    @Override
    public void extractRenderState(@Nonnull GuiGraphicsExtractor graphics, int pMouseX, int pMouseY, float pPartialTick) {
        modular.draw(graphics, pMouseX, pMouseY, pPartialTick);
    }

    @Override
    public List<ClientTooltipComponent> getTooltip(int mouseX, int mouseY) {
        if (modular.tooltipTexts == null || modular.tooltipTexts.isEmpty()) {
            return super.getTooltip(mouseX, mouseY);
        }
        var hovered = modular.modularUI.mainGroup.getHoverElement(mouseX + modular.getLeft(), mouseY + modular.getTop());
        if (hovered instanceof IRecipeIngredientSlot) {
            // don't skip the tooltip if the hovered widget is in a draggable group, as those are skipped when creating tooltips
            if (!(hovered.getParent() instanceof DraggableScrollableWidgetGroup draggable && draggable.isUseScissor())) {
                return super.getTooltip(mouseX, mouseY);
            }
        }

        List<ClientTooltipComponent> tooltips = modular.tooltipTexts.stream()
                .map(Component::getVisualOrderText)
                .map(ClientTooltipComponent::create)
                .collect(Collectors.toList());
        if (modular.tooltipComponent != null) {
            tooltips.add(DrawerHelper.getClientTooltipComponent(modular.tooltipComponent));
        }
        return tooltips;
    }

    @Override
    public List<? extends GuiEventListener> children() {
        return Collections.emptyList();
    }

    @Override
    public boolean mouseClicked(net.minecraft.client.input.MouseButtonEvent event, boolean doubleClick) {
        return modular.mouseClicked(local(event), doubleClick);
    }
    private net.minecraft.client.input.MouseButtonEvent local(net.minecraft.client.input.MouseButtonEvent event) {
        return new net.minecraft.client.input.MouseButtonEvent(event.x() + modular.getLeft(), event.y() + modular.getTop(), event.buttonInfo());
    }
    @Override
    public boolean mouseReleased(net.minecraft.client.input.MouseButtonEvent event) {
        return modular.mouseReleased(local(event));
    }

    @Override
    public void mouseMoved(double pMouseX, double pMouseY) {
        modular.mouseMoved(pMouseX + modular.getLeft(), pMouseY + modular.getTop());
    }

    @Override
    public boolean mouseDragged(net.minecraft.client.input.MouseButtonEvent event, double pDragX, double pDragY) {
        return modular.mouseDragged(local(event), pDragX, pDragY);
    }

    @Override
    public boolean mouseScrolled(double pMouseX, double pMouseY, double pScrollX, double pScrollY) {
        return modular.mouseScrolled(pMouseX + modular.getLeft(), pMouseY + modular.getTop(), pScrollX, pScrollY);
    }

    @Override
    public boolean keyPressed(net.minecraft.client.input.KeyEvent event) {
        modular.focused = false;
        return modular.modularUI.mainGroup.keyPressed(event.key(), event.keycode(), event.modifiers());
    }

    @Override
    public boolean keyReleased(net.minecraft.client.input.KeyEvent event) {
        return modular.keyReleased(event);
    }

    @Override
    public boolean charTyped(net.minecraft.client.input.CharacterEvent event) {
        return modular.charTyped(event);
    }
}
