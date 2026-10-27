package com.lowdragmc.lowdraglib.integration.rei;

import com.lowdragmc.lowdraglib.gui.ingredient.IRecipeIngredientSlot;
import com.lowdragmc.lowdraglib.gui.widget.DraggableScrollableWidgetGroup;
import com.lowdragmc.lowdraglib.integration.jei.ModularWrapper;
import me.shedaniel.math.Point;
import me.shedaniel.rei.api.client.gui.widgets.Tooltip;
import me.shedaniel.rei.api.client.gui.widgets.TooltipContext;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.world.item.Item;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;

/**
 * @author KilaBash
 * @date 2022/11/27
 * @implNote ModularWrapperWidget
 */
@OnlyIn(Dist.CLIENT)
public class ModularWrapperWidget extends Widget {
    final ModularWrapper<?> modular;

    public ModularWrapperWidget(ModularWrapper<?> modular) {
        this.modular = modular;
    }

    @Override
    public void extractRenderState(@Nonnull GuiGraphicsExtractor graphics, int pMouseX, int pMouseY, float pPartialTick) {
        modular.draw(graphics, pMouseX, pMouseY, pPartialTick);
        var tooltip = getTooltip(TooltipContext.ofMouse(Item.TooltipContext.of(Minecraft.getInstance().level)));
        if (tooltip != null) {
            tooltip.queue();
        }
    }

    @Override
    public @Nullable Tooltip getTooltip(TooltipContext context) {
        if (modular.tooltipTexts == null || modular.tooltipTexts.isEmpty()) {
            return super.getTooltip(context);
        }
        Point mouse = context.getPoint();
        var hovered = modular.modularUI.mainGroup.getHoverElement(mouse.x + modular.getLeft(), mouse.y + modular.getTop());
        if (hovered instanceof IRecipeIngredientSlot) {
            // don't skip the tooltip if the hovered widget is in a draggable group, as those are skipped when creating tooltips
            if (!(hovered.getParent() instanceof DraggableScrollableWidgetGroup draggable && draggable.isUseScissor())) {
                return super.getTooltip(context);
            }
        }

        var tooltip = Tooltip.create(mouse, modular.tooltipTexts);
        if (modular.tooltipComponent != null) {
            tooltip.add(modular.tooltipComponent);
        }
        return tooltip;
    }

    @Override
    public List<? extends GuiEventListener> children() {
        return Collections.emptyList();
    }

    @Override
    public boolean mouseClicked(net.minecraft.client.input.MouseButtonEvent event, boolean doubleClick) {
        return modular.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseReleased(net.minecraft.client.input.MouseButtonEvent event) {
        return modular.mouseReleased(event);
    }

    @Override
    public void mouseMoved(double pMouseX, double pMouseY) {
        modular.mouseMoved(pMouseX, pMouseY);
    }

    @Override
    public boolean mouseDragged(net.minecraft.client.input.MouseButtonEvent event, double pDragX, double pDragY) {
        return modular.mouseDragged(event, pDragX, pDragY);
    }

    @Override
    public boolean mouseScrolled(double pMouseX, double pMouseY, double pScrollX, double pScrollY) {
        return modular.mouseScrolled(pMouseX, pMouseY, pScrollX, pScrollY);
    }

    @Override
    public boolean keyPressed(net.minecraft.client.input.KeyEvent event) {
        modular.focused = false;
        if (modular.modularUI.mainGroup.keyPressed(event.key(), event.keycode(), event.modifiers())) {
            return true;
        }
        return super.keyPressed(event);
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
