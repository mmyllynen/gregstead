package com.lowdragmc.lowdraglib.gui.modular;

import com.lowdragmc.lowdraglib.LDLib;
import com.lowdragmc.lowdraglib.Platform;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.util.DrawerHelper;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.networking.s2c.SPacketUIWidgetUpdate;
import com.lowdragmc.lowdraglib.utils.ByteBufUtil;
import dev.emi.emi.runtime.EmiDrawContext;
import dev.emi.emi.screen.EmiScreenManager;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.minecraft.ChatFormatting;
import net.minecraft.util.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.MouseButtonInfo;
import org.lwjgl.sdl.SDLKeyboard;
import com.lowdragmc.lowdraglib.transfer.input.GuiInput;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import com.lowdragmc.lowdraglib.transfer.util.Tuple;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.common.NeoForge;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@OnlyIn(Dist.CLIENT)
public class ModularUIGuiContainer extends AbstractContainerScreen<ModularUIContainer> {

    public final ModularUI modularUI;
    public Widget lastFocus;
    public boolean focused;
    public int dragSplittingLimit;
    public int dragSplittingButton;
    // hover tips
    @Nullable
    public List<Component> tooltipTexts;
    @Nullable
    public TooltipComponent tooltipComponent;
    @Nullable
    public Font tooltipFont;
    @Nullable
    public ItemStack tooltipStack = ItemStack.EMPTY;
    // drag element
    protected Tuple<Object, IGuiTexture> draggingElement;
    protected int pressedButton = -1;

    public ModularUIGuiContainer(ModularUI modularUI, int windowId) {
        super(new ModularUIContainer(modularUI, windowId), modularUI.entityPlayer.getInventory(), Component.literal("modularUI"), modularUI.getWidth(), modularUI.getHeight());
        this.modularUI = modularUI;
        modularUI.setModularUIGui(this);
    }

    public void setHoverTooltip(List<Component> tooltipTexts, ItemStack tooltipStack, @Nullable Font tooltipFont, @Nullable TooltipComponent tooltipComponent) {
        this.tooltipTexts = tooltipTexts;
        this.tooltipStack = tooltipStack;
        this.tooltipFont = tooltipFont;
        this.tooltipComponent = tooltipComponent;
    }

    public boolean setDraggingElement(Object element, IGuiTexture renderer) {
        if (draggingElement != null) return false;
        draggingElement = new Tuple<>(element, renderer);
        return true;
    }

    @Nullable
    public Object getDraggingElement() {
        if (draggingElement == null) return null;
        return draggingElement.getA();
    }

    @Override
    public void init() {
        super.init();
        this.modularUI.updateScreenSize(width, height);
    }

    @Override
    public void removed() {
        super.removed();
    }

    @Override
    public void containerTick() {
        super.containerTick();
        if (modularUI.holder.isInvalid() && modularUI.entityPlayer instanceof ServerPlayer serverPlayer) {
            serverPlayer.closeContainer();
        }
        modularUI.mainGroup.updateScreen();
        modularUI.addTick();
    }

    public void handleWidgetUpdate(SPacketUIWidgetUpdate packet) {
        if (packet.windowId == getMenu().containerId) {
            ByteBufUtil.readCustomData(packet.updateData, buf -> {
                int updateId = buf.readVarInt();
                modularUI.mainGroup.readUpdateInfo(updateId, buf);
            }, Platform.getFrozenRegistry());
        }
    }

    @Override
    public void extractRenderState(@Nonnull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
        this.hoveredSlot = null;
        tooltipTexts = null;
        tooltipComponent = null;
        this.extractBackground(graphics, mouseX, mouseY, partialTicks);
        NeoForge.EVENT_BUS.post(new ScreenEvent.Render.Background(this, graphics, mouseX, mouseY, partialTicks));
        modularUI.mainGroup.drawInBackground(graphics, mouseX, mouseY, partialTicks);
        if (LDLib.isEmiLoaded()) {
            EmiScreenManager.render(EmiDrawContext.wrap(graphics), mouseX, mouseY, partialTicks);
        }
        graphics.nextStratum();
        com.lowdragmc.lowdraglib.transfer.client.GuiOverlays.render(graphics, () -> modularUI.mainGroup.drawInForeground(graphics, mouseX, mouseY, partialTicks));
        if (draggingElement != null) {
            draggingElement.getB().draw(graphics, mouseX, mouseY, mouseX - 20, mouseY - 20, 40, 40);
        }
        NeoForge.EVENT_BUS.post(new ScreenEvent.Render.Foreground(this, graphics, mouseX, mouseY, partialTicks));
        // Native carried extraction owns quick-craft counts, cursor offsets and foreground stratum.
        super.extractCarriedItem(graphics, mouseX, mouseY);
        if (LDLib.isEmiLoaded()) {
            graphics.nextStratum();
            EmiScreenManager.drawForeground(EmiDrawContext.wrap(graphics), mouseX, mouseY, partialTicks);
        }
        if (draggingElement == null && tooltipTexts != null && !tooltipTexts.isEmpty()) {
            DrawerHelper.drawTooltip(graphics, mouseX, mouseY, tooltipTexts, tooltipStack, tooltipComponent,
                    tooltipFont == null ? Minecraft.getInstance().font : tooltipFont);
        }
    }

    public void setHoveredSlot(Slot hoveredSlot) {
        // to make sure the widget won't trigger xei slot lookup
        if (modularUI.mainGroup.isClientSideWidget() && !modularUI.mainGroup.isAllowXEIIngredientOverMouse()) {
            return;
        }
        this.hoveredSlot = hoveredSlot;
    }

    public void renderFloatingItem(GuiGraphicsExtractor graphics, ItemStack stack, int x, int y, @Nullable String amountText) {
        graphics.item(stack, x, y);
        graphics.itemDecorations(this.font, stack, x, y, amountText);
    }


    public boolean switchFocus(@Nonnull Widget widget) {
        if (focused) return false;
        focused = true;
        if (lastFocus == widget) return false;
        Widget l = lastFocus;
        lastFocus = widget;
        if (l != null) l.setFocus(false);
        return true;
    }

    public Set<Slot> getQuickCraftSlots() {
        return this.quickCraftSlots;
    }

    public boolean getQuickCrafting() {
        return this.isQuickCrafting;
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        double mouseX = event.x(), mouseY = event.y();
        int pButton = GuiInput.widgetButton(event.button());
        nativeModifiers = event.modifiers();
        nativeDoubleClick = doubleClick;
        pressedButton = pButton;
        focused = false;
        if (modularUI.mainGroup.mouseClicked(mouseX, mouseY, pButton)) return true;
        for (GuiEventListener guiEventListener : this.children()) {
            if (!guiEventListener.mouseClicked(event, doubleClick)) continue;
            this.setFocused(guiEventListener);
            if (pButton == 0) {
                this.setDragging(true);
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double pDragX, double pDragY) {
        double mouseX = event.x(), mouseY = event.y();
        int pButton = GuiInput.widgetButton(event.button());
        nativeModifiers = event.modifiers();
        focused = false;
        if (modularUI.mainGroup.mouseDragged(mouseX, mouseY, pButton, pDragX, pDragY)) return true;
        if (this.getFocused() != null && this.isDragging() && pButton == 0) {
            return this.getFocused().mouseDragged(event, pDragX, pDragY);
        }
        return false;
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        double mouseX = event.x(), mouseY = event.y();
        int pButton = GuiInput.widgetButton(event.button());
        nativeModifiers = event.modifiers();
        pressedButton = -1;
        focused = false;
        var result = modularUI.mainGroup.mouseReleased(mouseX, mouseY, pButton);
        draggingElement = null;
        if (result) return true;
        this.setDragging(false);
        return this.getChildAt(mouseX, mouseY).filter(guiEventListener -> guiEventListener.mouseReleased(event)).isPresent();
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        int keyCode = event.key(), scanCode = event.keycode(), modifiers = event.modifiers();
        focused = false;
        if (modularUI.mainGroup.keyPressed(keyCode, scanCode, modifiers)) return true;
        return super.keyPressed(event);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double pScrollX, double pScrollY) {
        focused = false;
        if (modularUI.mainGroup.mouseWheelMove(mouseX, mouseY, pScrollX, pScrollY)) return true;
        return this.getChildAt(mouseX, mouseY).filter(element -> element.mouseScrolled(mouseX, mouseY, pScrollX, pScrollY)).isPresent();
    }

    @Override
    public boolean keyReleased(KeyEvent event) {
        int keyCode = event.key(), scanCode = event.keycode(), modifiers = event.modifiers();
        focused = false;
        if (modularUI.mainGroup.keyReleased(keyCode, scanCode, modifiers)) return true;
        return this.getFocused() != null && this.getFocused().keyReleased(event);
    }

    @Override
    public boolean charTyped(CharacterEvent event) {
        int codePoint = event.codepoint(), modifiers = SDLKeyboard.SDL_GetModState();
        focused = false;
        if (modularUI.mainGroup.charTyped(codePoint, modifiers)) return true;
        return this.getFocused() != null && this.getFocused().charTyped(event);
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        focused = false;
        modularUI.mainGroup.mouseMoved(mouseX, mouseY);
    }

    private int nativeModifiers;
    private boolean nativeDoubleClick;

    private MouseButtonEvent nativeMouseEvent(double x, double y, int widgetButton) {
        return new MouseButtonEvent(x, y, new MouseButtonInfo(GuiInput.nativeButton(widgetButton), nativeModifiers));
    }

    // Existing recipe-viewer/widget integrations use the internal button numbering.
    public boolean mouseClicked(double x, double y, int button) {
        return mouseClicked(new MouseButtonEvent(x, y,
                new MouseButtonInfo(GuiInput.nativeButton(button), SDLKeyboard.SDL_GetModState())), false);
    }
    public boolean mouseDragged(double x, double y, int button, double dx, double dy) {
        return mouseDragged(new MouseButtonEvent(x, y,
                new MouseButtonInfo(GuiInput.nativeButton(button), SDLKeyboard.SDL_GetModState())), dx, dy);
    }
    public boolean mouseReleased(double x, double y, int button) {
        return mouseReleased(new MouseButtonEvent(x, y,
                new MouseButtonInfo(GuiInput.nativeButton(button), SDLKeyboard.SDL_GetModState())));
    }
    public boolean keyPressed(int key, int keycode, int modifiers) {
        return keyPressed(new KeyEvent(key, keycode, modifiers));
    }
    public boolean keyReleased(int key, int keycode, int modifiers) {
        return keyReleased(new KeyEvent(key, keycode, modifiers));
    }
    public boolean charTyped(int codepoint, int modifiers) {
        // Native CharacterEvent is a Unicode scalar value; modifier state is separate.
        focused = false;
        if (modularUI.mainGroup.charTyped(codepoint, modifiers)) return true;
        return getFocused() != null && getFocused().charTyped(new CharacterEvent(codepoint));
    }

    public boolean isButtonPressed(int button) {
        return pressedButton == button;
    }

    public void superMouseClicked(double mouseX, double mouseY, int mouseButton) {
        super.mouseClicked(nativeMouseEvent(mouseX, mouseY, mouseButton), nativeDoubleClick);
    }

    public void superMouseDragged(double pMouseX, double pMouseY, int pButton, double pDragX, double pDragY) {
        super.mouseDragged(nativeMouseEvent(pMouseX, pMouseY, pButton), pDragX, pDragY);
    }

    public void superMouseReleased(double mouseX, double mouseY, int state) {
        super.mouseReleased(nativeMouseEvent(mouseX, mouseY, state));
    }

    public boolean superKeyPressed(int keyCode, int scanCode, int modifiers) {
        return super.keyPressed(new KeyEvent(keyCode, scanCode, modifiers));
    }

    public boolean superMouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    public boolean superKeyReleased(int keyCode, int scanCode, int modifiers) {
        return super.keyReleased(new KeyEvent(keyCode, scanCode, modifiers));
    }

    public boolean superCharTyped(int codePoint, int modifiers) {
        return super.charTyped(new CharacterEvent(codePoint));
    }

    public void superMouseMoved(double mouseX, double mouseY) {
        super.mouseMoved(mouseX, mouseY);
    }


    public List<Rect2i> getGuiExtraAreas() {
        return modularUI.mainGroup.getGuiExtraAreas(modularUI.mainGroup.toRectangleBox(), new ArrayList<>());
    }
}
