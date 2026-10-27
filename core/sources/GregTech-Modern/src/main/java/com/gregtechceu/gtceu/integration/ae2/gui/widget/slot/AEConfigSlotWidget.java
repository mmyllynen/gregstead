package com.gregtechceu.gtceu.integration.ae2.gui.widget.slot;

import com.gregtechceu.gtceu.integration.ae2.gui.widget.ConfigWidget;
import com.gregtechceu.gtceu.integration.ae2.slot.IConfigurableSlot;

import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.math.Position;
import com.lowdragmc.lowdraglib.math.Size;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import appeng.api.stacks.GenericStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.lowdragmc.lowdraglib.gui.util.DrawerHelper.drawGradientRect;

public class AEConfigSlotWidget extends Widget {

    protected ConfigWidget parentWidget;
    protected int index;
    protected final static int REMOVE_ID = 1000;
    protected final static int UPDATE_ID = 1001;
    protected final static int AMOUNT_CHANGE_ID = 1002;
    protected final static int PICK_UP_ID = 1003;
    protected boolean select = false;

    public AEConfigSlotWidget(Position pos, Size size, ConfigWidget widget, int index) {
        super(pos, size);
        this.parentWidget = widget;
        this.index = index;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void drawInForeground(@NotNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
        super.drawInForeground(graphics, mouseX, mouseY, partialTicks);
        IConfigurableSlot slot = this.parentWidget.getDisplay(this.index);
        if (slot.getConfig() == null) {
            if (mouseOverConfig(mouseX, mouseY)) {
                List<Component> hoverStringList = new ArrayList<>();
                hoverStringList.add(Component.translatable("gtceu.gui.config_slot"));
                if (parentWidget.isAutoPull()) {
                    hoverStringList.add(Component.translatable("gtceu.gui.config_slot.auto_pull_managed"));
                } else {
                    if (!parentWidget.isStocking()) {
                        hoverStringList.add(Component.translatable("gtceu.gui.config_slot.set"));
                        hoverStringList.add(Component.translatable("gtceu.gui.config_slot.scroll"));
                    } else {
                        hoverStringList.add(Component.translatable("gtceu.gui.config_slot.set_only"));
                    }
                    hoverStringList.add(Component.translatable("gtceu.gui.config_slot.remove"));
                }
                graphics.setTooltipForNextFrame(Minecraft.getInstance().font, hoverStringList, Optional.empty(), mouseX, mouseY, null, true);
            }
        } else {
            GenericStack item = null;
            if (mouseOverConfig(mouseX, mouseY)) {
                item = slot.getConfig();
            } else if (mouseOverStock(mouseX, mouseY)) {
                item = slot.getStock();
            }
            if (item != null) {
                graphics.setTooltipForNextFrame(Minecraft.getInstance().font, GenericStack.wrapInItemStack(item), mouseX,
                        mouseY);
            }
        }
    }

    public void setSelect(boolean val) {
        this.select = val;
    }

    protected boolean mouseOverConfig(double mouseX, double mouseY) {
        Position position = getPosition();
        return isMouseOver(position.x, position.y, 18, 18, mouseX, mouseY);
    }

    protected boolean mouseOverStock(double mouseX, double mouseY) {
        Position position = getPosition();
        return isMouseOver(position.x, position.y + 18, 18, 18, mouseX, mouseY);
    }

    @OnlyIn(Dist.CLIENT)
    public static void drawSelectionOverlay(GuiGraphicsExtractor graphics, int x, int y, int width, int height) {
        com.lowdragmc.lowdraglib.client.scene.NativeTankHover.draw(graphics, x, y, width, height, 0x80ffffff);
    }

    // Method for server-side validation of an attempted new configured item
    protected boolean isStackValidForSlot(GenericStack stack) {
        if (stack == null || stack.amount() < 0) return true;
        if (!parentWidget.isStocking()) return true;
        return !parentWidget.hasStackInConfig(stack);
    }
}
