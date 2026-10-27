package com.lowdragmc.lowdraglib.gui.widget;

import com.lowdragmc.lowdraglib.LDLib;
import com.lowdragmc.lowdraglib.core.mixins.accessor.SlotAccessor;
import com.lowdragmc.lowdraglib.editor.annotation.Configurable;
import com.lowdragmc.lowdraglib.registry.annotation.LDLRegister;
import com.lowdragmc.lowdraglib.editor.configurator.ConfiguratorGroup;
import com.lowdragmc.lowdraglib.editor.configurator.IConfigurableWidget;
import com.lowdragmc.lowdraglib.editor.configurator.WrapperConfigurator;
import com.lowdragmc.lowdraglib.gui.ingredient.IRecipeIngredientSlot;
import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.modular.ModularUIGuiContainer;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.texture.ResourceBorderTexture;
import com.lowdragmc.lowdraglib.gui.util.DrawerHelper;
import com.lowdragmc.lowdraglib.integration.jei.IngredientIO;
import com.lowdragmc.lowdraglib.integration.jei.JEIPlugin;
import com.lowdragmc.lowdraglib.misc.CycleItemStackHandler;
import com.lowdragmc.lowdraglib.math.Position;
import com.lowdragmc.lowdraglib.math.Size;
import com.lowdragmc.lowdraglib.misc.TagOrCycleItemStackTransfer;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Window;
import com.lowdragmc.lowdraglib.transfer.input.GuiInput;
import com.lowdragmc.lowdraglib.transfer.menu.QuickCraftPreview;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.stack.EmiStackInteraction;
import dev.emi.emi.screen.EmiScreenManager;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.TagKey;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import com.lowdragmc.lowdraglib.transfer.item.IItemHandlerModifiable;
import com.lowdragmc.lowdraglib.transfer.item.ItemStackHandler;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SuppressWarnings("UnusedReturnValue")
@LDLRegister(name = "item_slot", group = "widget.container", registry = "ldlib:widget")
@Accessors(chain = true)
public class SlotWidget extends Widget implements IRecipeIngredientSlot, IConfigurableWidget {
    public final static ResourceBorderTexture ITEM_SLOT_TEXTURE = new ResourceBorderTexture("ldlib:textures/gui/slot.png", 18, 18, 1, 1);

    @Nullable
    protected static Slot HOVER_SLOT = null;
    @Nullable
    protected Slot slotReference;
    @Configurable(name = "ldlib.gui.editor.name.canTakeItems")
    @Setter
    protected boolean canTakeItems;
    @Configurable(name = "ldlib.gui.editor.name.canPutItems")
    @Setter
    protected boolean canPutItems;
    public boolean isPlayerContainer;
    public boolean isPlayerHotBar;
    @Configurable(name = "ldlib.gui.editor.name.drawHoverOverlay")
    @Setter
    public boolean drawHoverOverlay = true;
    @Configurable(name = "ldlib.gui.editor.name.drawHoverTips")
    @Setter
    public boolean drawHoverTips = true;

    @Setter
    protected Runnable changeListener;
    @Setter
    protected BiConsumer<SlotWidget, List<Component>> onAddedTooltips;
    @Setter
    protected Function<ItemStack, ItemStack> itemHook;
    @Setter
    @Getter
    protected IngredientIO ingredientIO = IngredientIO.RENDER_ONLY;
    @Setter
    @Getter
    protected float XEIChance = 1f;
    @Getter
    private ItemStack lastItem = ItemStack.EMPTY;

    public SlotWidget() {
        super(Position.of(0, 0), Size.of(18, 18));
    }

    @Override
    public void initTemplate() {
        setBackgroundTexture(ITEM_SLOT_TEXTURE);
        this.canTakeItems = true;
        this.canPutItems = true;
    }

    public SlotWidget(Container inventory, int slotIndex, int xPosition, int yPosition, boolean canTakeItems, boolean canPutItems) {
        super(Position.of(xPosition, yPosition), Size.of(18, 18));
        setBackgroundTexture(SlotWidget.ITEM_SLOT_TEXTURE);
        this.canTakeItems = canTakeItems;
        this.canPutItems = canPutItems;
        setContainerSlot(inventory, slotIndex);
    }

    public SlotWidget(IItemHandlerModifiable itemHandler, int slotIndex, int xPosition, int yPosition, boolean canTakeItems, boolean canPutItems) {
        super(Position.of(xPosition, yPosition), Size.of(18, 18));
        setBackgroundTexture(SlotWidget.ITEM_SLOT_TEXTURE);
        this.canTakeItems = canTakeItems;
        this.canPutItems = canPutItems;
        setHandlerSlot(itemHandler, slotIndex);
    }

    protected Slot createSlot(Container inventory, int index) {
        return new WidgetSlot(inventory, index, 0, 0);
    }

    protected Slot createSlot(IItemHandlerModifiable itemHandler, int index) {
        return new WidgetSlotItemTransfer(itemHandler, index, 0, 0);
    }

    public SlotWidget setContainerSlot(Container inventory, int slotIndex) {
        updateSlot(createSlot(inventory, slotIndex));
        return this;
    }

    public SlotWidget setHandlerSlot(IItemHandlerModifiable itemHandler, int slotIndex) {
        updateSlot(createSlot(itemHandler, slotIndex));
        return this;
    }

    protected void updateSlot(Slot slot) {
        if (this.slotReference != null && this.gui != null && !isClientSideWidget) {
            getGui().removeNativeSlot(this.slotReference);
        }
        this.slotReference = slot;
        if (this.gui != null && !isClientSideWidget) {
            getGui().addNativeSlot(this.slotReference, this);
        }
    }

    public ItemStack getItem() {
        return slotReference == null ? ItemStack.EMPTY : slotReference.getItem();
    }

    public void setItem(ItemStack stack) {
        if (slotReference != null) {
            slotReference.set(stack);
        }
    }

    public void setItem(ItemStack stack, boolean notify) {
        if (slotReference != null) {
            var lastListener = changeListener;
            if (!notify) {
                changeListener = null;
            }
            slotReference.set(stack);
            changeListener = lastListener;
        }
    }

    @Override
    public final void setSize(Size size) {
        // you cant modify size.
    }

    @Override
    public void setGui(ModularUI gui) {
        if (!isClientSideWidget && this.gui != gui) {
            if (this.gui != null && slotReference != null) {
                this.gui.removeNativeSlot(slotReference);
            }
            if (gui != null && slotReference != null) {
                gui.addNativeSlot(slotReference, this);
            }
        }
        super.setGui(gui);
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        this.lastItem = getItem();
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void updateScreen() {
        super.updateScreen();
        this.lastItem = getItem();
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void drawInForeground(@Nonnull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
        if (slotReference != null && drawHoverTips && isMouseOverElement(mouseX, mouseY) && getHoverElement(mouseX, mouseY) == this) {
            ItemStack stack = slotReference.getItem();
            if (gui != null) {
                gui.getModularUIGui().setHoveredSlot(slotReference);
            }
            if (!stack.isEmpty() && gui != null) {
                gui.getModularUIGui().setHoverTooltip(getFullTooltipTexts(), stack, null, stack.getTooltipImage().orElse(null));
            } else {
                super.drawInForeground(graphics, mouseX, mouseY, partialTicks);
            }
        } else {
            super.drawInForeground(graphics, mouseX, mouseY, partialTicks);
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void drawInBackground(@Nonnull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
        super.drawInBackground(graphics, mouseX, mouseY, partialTicks);
        Position pos = getPosition();
        if (slotReference != null) {
            ItemStack itemStack = getRealStack(slotReference.getItem());
            ModularUIGuiContainer modularUIGui = gui == null ? null : gui.getModularUIGui();
            if (modularUIGui != null && modularUIGui.getQuickCrafting()
                    && modularUIGui.getQuickCraftSlots().contains(slotReference)
                    && gui.getModularUIContainer().canDragTo(slotReference)) {
                itemStack = QuickCraftPreview.stack(slotReference, gui.getModularUIContainer().getCarried(),
                        modularUIGui.getQuickCraftSlots().size(), modularUIGui.dragSplittingLimit);
            }
            if (!itemStack.isEmpty()) {
                DrawerHelper.drawItemStack(graphics, itemStack, pos.x + 1, pos.y + 1, -1, null);
            }
        }
        drawOverlay(graphics, mouseX, mouseY, partialTicks);
        if (drawHoverOverlay && isMouseOverElement(mouseX, mouseY) && getHoverElement(mouseX, mouseY) == this) {
            DrawerHelper.drawSolidRect(graphics, getPosition().x + 1, getPosition().y + 1, 16, 16, 0x80FFFFFF);
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (slotReference != null && isMouseOverElement(mouseX, mouseY) && gui != null) {
            var stack = slotReference.getItem();
            if (canPutItems && stack.isEmpty() || canTakeItems && !stack.isEmpty()) {
                ModularUIGuiContainer modularUIGui = gui.getModularUIGui();
                boolean last = modularUIGui.getQuickCrafting();
                InputConstants.Key mouseKey = InputConstants.Type.MOUSE.getOrCreate(GuiInput.nativeButton(button));
                HOVER_SLOT = slotReference;
                gui.getModularUIGui().superMouseClicked(mouseX, mouseY, button);
                HOVER_SLOT = null;
                if (last != modularUIGui.getQuickCrafting()) {
                    modularUIGui.dragSplittingButton = button;
                    if (button == 0) {
                        modularUIGui.dragSplittingLimit = 0;
                    } else if (button == 1) {
                        modularUIGui.dragSplittingLimit = 1;
                    } else if (Minecraft.getInstance().options.keyPickItem.matches(mouseKey)) {
                        modularUIGui.dragSplittingLimit = 2;
                    }
                }
                return true;
            } else if (LDLib.isEmiLoaded()) {
                return EMICallWrapper.mouseClicked(getXEICurrentIngredient(), button);
            } else {
                return false;
            }
        }
        return false;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        Window window = Minecraft.getInstance().getWindow();
        double mouseX = Minecraft.getInstance().mouseHandler.xpos() * window.getGuiScaledWidth() / window.getScreenWidth();
        double mouseY = Minecraft.getInstance().mouseHandler.ypos() * window.getGuiScaledHeight() / window.getScreenHeight();
        if (isMouseOverElement(mouseX, mouseY)) {
            if (LDLib.isEmiLoaded()) {
                if (EMICallWrapper.keyPressed(getXEICurrentIngredient(), keyCode, scanCode, modifiers)) {
                    return true;
                }
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (isMouseOverElement(mouseX, mouseY) && gui != null) {
            HOVER_SLOT = slotReference;
            gui.getModularUIGui().superMouseReleased(mouseX, mouseY, button);
            HOVER_SLOT = null;
            return getIngredientIO() == IngredientIO.RENDER_ONLY && (canPutItems || canTakeItems);
        }
        return false;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (isMouseOverElement(mouseX, mouseY) && gui != null) {
            gui.getModularUIGui().superMouseDragged(mouseX, mouseY, button, dragX, dragY);
            return true;
        }
        return false;
    }

    @Override
    protected void onPositionUpdate() {
        if (gui != null) {
            Position position = getPosition();
            if (slotReference != null) {
                ((SlotAccessor) slotReference).setX(position.x + 1 - gui.getGuiLeft());
                ((SlotAccessor) slotReference).setY(position.y + 1 - gui.getGuiTop());
            }
        }
    }

    public SlotWidget(IItemHandlerModifiable itemHandler, int slotIndex, int xPosition, int yPosition) {
        this(itemHandler, slotIndex, xPosition, yPosition, true, true);
    }

    public SlotWidget(Container inventory, int slotIndex, int xPosition, int yPosition) {
        this(inventory, slotIndex, xPosition, yPosition, true, true);
    }

    public SlotWidget setBackgroundTexture(IGuiTexture backgroundTexture) {
        this.backgroundTexture = backgroundTexture;
        return this;
    }

    public boolean canPutStack(ItemStack stack) {
        return isEnabled() && canPutItems;
    }

    public boolean canTakeStack(Player player) {
        return isEnabled() && canTakeItems;
    }

    public boolean isEnabled() {
        return this.isActive() && isVisible();
    }

    public boolean canMergeSlot(ItemStack stack) {
        return isEnabled();
    }

    public void onSlotChanged() {
        if (gui == null) return;
        gui.holder.markAsDirty();
    }

    public ItemStack slotClick(int dragType, ContainerInput clickTypeIn, Player player) {
        return null;
    }

    @Nullable
    public final Slot getHandler() {
        return slotReference;
    }

    public SlotWidget setLocationInfo(boolean isPlayerContainer, boolean isPlayerHotBar) {
        this.isPlayerHotBar = isPlayerHotBar;
        this.isPlayerContainer = isPlayerContainer;
        return this;
    }

    @Override
    public List<Component> getTooltipTexts() {
        List<Component> tooltips = getAdditionalToolTips(new ArrayList<>());
        tooltips.addAll(tooltipTexts);
        return tooltips;
    }

    public List<Component> getAdditionalToolTips(List<Component> list) {
        if (this.onAddedTooltips != null) {
            this.onAddedTooltips.accept(this, list);
        }
        return list;
    }

    @Override
    public List<Component> getFullTooltipTexts() {
        if (slotReference != null) {
            var stack = slotReference.getItem();
            if (!stack.isEmpty()) {
                var tips = new ArrayList<>(DrawerHelper.getItemToolTip(stack));
                tips.addAll(getTooltipTexts());
                return tips;
            }
        }
        return Collections.emptyList();
    }


    @Nullable
    @Override
    public Object getXEIIngredientOverMouse(double mouseX, double mouseY) {
        if (self().isMouseOverElement(mouseX, mouseY)) {
            var handler = getHandler();
            if (handler == null) return null;
            ItemStack realStack = getRealStack(handler.getItem());

            if (handler instanceof WidgetSlotItemTransfer widgetSlotItemTransfer) {
                if (widgetSlotItemTransfer.itemHandler instanceof CycleItemStackHandler cycleItemStackHandler) {
                    return getXEIIngredientsFromCycleTransferClickable(cycleItemStackHandler, widgetSlotItemTransfer.index);
                } else if (widgetSlotItemTransfer.itemHandler instanceof TagOrCycleItemStackTransfer transfer) {
                    return getXEIIngredientsFromTagOrCycleTransferClickable(transfer, widgetSlotItemTransfer.index);
                }
            }

            if (LDLib.isJeiLoaded() && !realStack.isEmpty()) {
                return JEIPlugin.getItemIngredient(realStack, getPosition().x, getPosition().y, getSize().width, getSize().height);
            } else if (LDLib.isReiLoaded()) {
                return REICallWrapper.getReiIngredients(realStack);
            } else if (LDLib.isEmiLoaded()) {
                return EMICallWrapper.getEmiIngredients(realStack, getXEIChance());
            }
            return realStack;
        }
        return null;
    }

    @Override
    public List<Object> getXEIIngredients() {
        if (slotReference == null || slotReference.getItem().isEmpty()) return Collections.emptyList();
        var handler = getHandler();
        if (handler == null) return Collections.emptyList();
        ItemStack realStack = getRealStack(handler.getItem());

        if (handler instanceof WidgetSlotItemTransfer widgetSlotItemTransfer) {
            if (widgetSlotItemTransfer.itemHandler instanceof CycleItemStackHandler cycleItemStackHandler) {
                return getXEIIngredientsFromCycleTransferClickable(cycleItemStackHandler, widgetSlotItemTransfer.index);
            } else if (widgetSlotItemTransfer.itemHandler instanceof TagOrCycleItemStackTransfer transfer) {
                return getXEIIngredientsFromTagOrCycleTransferClickable(transfer, widgetSlotItemTransfer.index);
            }
        }

        if (LDLib.isJeiLoaded()) {
            var ingredient = JEIPlugin.getItemIngredient(realStack, getPosition().x, getPosition().y, getSize().width, getSize().height);
            return ingredient == null ? Collections.emptyList() : List.of(ingredient);
        } else if (LDLib.isReiLoaded()) {
            return REICallWrapper.getReiIngredients(realStack);
        } else if (LDLib.isEmiLoaded()) {
            return EMICallWrapper.getEmiIngredients(realStack, getXEIChance());
        }
        return List.of(realStack);
    }

    public ItemStack getRealStack(ItemStack itemStack) {
        if (itemHook != null) return itemHook.apply(itemStack);
        return itemStack;
    }

    @Override
    public Object getXEICurrentIngredient() {
        if (slotReference == null || slotReference.getItem().isEmpty()) return null;
        var handler = getHandler();
        if (handler == null) return null;
        ItemStack realStack = getRealStack(handler.getItem());

        if (LDLib.isJeiLoaded()) {
            return JEIPlugin.getItemIngredient(realStack, getPosition().x, getPosition().y, getSize().width, getSize().height);
        } else if (LDLib.isEmiLoaded()) {
            return EMICallWrapper.getEmiIngredient(realStack, getXEIChance());
        }
        return null;
    }

    private List<Object> getXEIIngredientsFromCycleTransfer(CycleItemStackHandler transfer, int index) {
        var stream = transfer.getStackList(index).stream().map(this::getRealStack);
        if (LDLib.isJeiLoaded()) {
            return stream.filter(stack -> !stack.isEmpty()).collect(Collectors.toList());
        } else if (LDLib.isReiLoaded()) {
            return REICallWrapper.getReiIngredients(stream);
        } else if (LDLib.isEmiLoaded()) {
            return EMICallWrapper.getEmiIngredients(stream, getXEIChance());
        }
        return Collections.emptyList();
    }

    private List<Object> getXEIIngredientsFromCycleTransferClickable(CycleItemStackHandler transfer, int index) {
        var stream = transfer.getStackList(index).stream().map(this::getRealStack);
        if (LDLib.isJeiLoaded()) {
            return stream
                    .filter(stack -> !stack.isEmpty())
                    .map(item -> JEIPlugin.getItemIngredient(item, getPosition().x, getPosition().y, getSize().width, getSize().height))
                    .toList();
        } else if (LDLib.isReiLoaded()) {
            return REICallWrapper.getReiIngredients(stream);
        } else if (LDLib.isEmiLoaded()) {
            return EMICallWrapper.getEmiIngredients(stream, getXEIChance());
        }
        return Collections.emptyList();
    }

    private List<Object> getXEIIngredientsFromTagOrCycleTransfer(TagOrCycleItemStackTransfer transfer, int index) {
        Either<List<Pair<TagKey<Item>, Integer>>, List<ItemStack>> either = transfer
                .getStacks()
                .get(index);
        var ref = new Object() {
            List<Object> returnValue = Collections.emptyList();
        };
        either.ifLeft(list -> {
            if (LDLib.isJeiLoaded()) {
                ref.returnValue = list.stream()
                        .flatMap(pair -> BuiltInRegistries.ITEM
                                .get(pair.getFirst())
                                .stream()
                                .flatMap(HolderSet.ListBacked::stream)
                                .map(item -> getRealStack(new ItemStack(item.value(), pair.getSecond()))))
                        .collect(Collectors.toList());
            } else if (LDLib.isReiLoaded()) {
                ref.returnValue = REICallWrapper.getReiIngredients(this::getRealStack, list);
            } else if (LDLib.isEmiLoaded()) {
                ref.returnValue = EMICallWrapper.getEmiIngredients(list, getXEIChance());
            }
        }).ifRight(items -> {
            var stream = items.stream().map(this::getRealStack);
            if (LDLib.isJeiLoaded()) {
                ref.returnValue = stream
                        .filter(stack -> !stack.isEmpty())
                        .collect(Collectors.toList());
            } else if (LDLib.isReiLoaded()) {
                ref.returnValue = REICallWrapper.getReiIngredients(stream);
            } else if (LDLib.isEmiLoaded()) {
                ref.returnValue = EMICallWrapper.getEmiIngredients(stream, getXEIChance());
            }
        });
        return ref.returnValue;
    }

    private List<Object> getXEIIngredientsFromTagOrCycleTransferClickable(TagOrCycleItemStackTransfer transfer, int index) {
        Either<List<Pair<TagKey<Item>, Integer>>, List<ItemStack>> either = transfer
                .getStacks()
                .get(index);
        var ref = new Object() {
            List<Object> returnValue = Collections.emptyList();
        };
        either.ifLeft(list -> {
            if (LDLib.isJeiLoaded()) {
                ref.returnValue = list.stream()
                        .flatMap(pair -> BuiltInRegistries.ITEM
                                .get(pair.getFirst())
                                .stream()
                                .flatMap(HolderSet.ListBacked::stream)
                                .map(item -> JEIPlugin.getItemIngredient(getRealStack(new ItemStack(item.value(), pair.getSecond())), getPosition().x, getPosition().y, getSize().width, getSize().height)))
                        .collect(Collectors.toList());
            } else if (LDLib.isReiLoaded()) {
                ref.returnValue = REICallWrapper.getReiIngredients(this::getRealStack, list);
            } else if (LDLib.isEmiLoaded()) {
                ref.returnValue = EMICallWrapper.getEmiIngredients(list, getXEIChance());
            }
        }).ifRight(items -> {
            var stream = items.stream().map(this::getRealStack);
            if (LDLib.isJeiLoaded()) {
                ref.returnValue = stream
                        .filter(stack -> !stack.isEmpty())
                        .map(item -> JEIPlugin.getItemIngredient(item, getPosition().x, getPosition().y, getSize().width, getSize().height))
                        .toList();
            } else if (LDLib.isReiLoaded()) {
                ref.returnValue = REICallWrapper.getReiIngredients(stream);
            } else if (LDLib.isEmiLoaded()) {
                ref.returnValue = EMICallWrapper.getEmiIngredients(stream, getXEIChance());
            }
        });
        return ref.returnValue;
    }


    /** Capacity queries must never clear/restore real storage or fire its listeners. */
    private static int slotCapacity(IItemHandlerModifiable handler, int index, ItemStack stack) {
        if (stack.isEmpty() || !handler.isItemValid(index, stack)) return 0;
        if (handler instanceof com.lowdragmc.lowdraglib.misc.ItemTransferList combined) {
            int offset = 0;
            for (var child : combined.transfers) {
                if (index - offset < child.getSlots()) return slotCapacity(child, index - offset, stack);
                offset += child.getSlots();
            }
            return 0;
        }
        net.neoforged.neoforge.transfer.ResourceHandler<net.neoforged.neoforge.transfer.item.ItemResource> nativeHandler = null;
        if (handler instanceof com.lowdragmc.lowdraglib.transfer.item.NativeItemView view) {
            nativeHandler = view.nativeView();
        } else if (handler instanceof net.neoforged.neoforge.transfer.ResourceHandler<?> resourceHandler) {
            @SuppressWarnings("unchecked")
            var typed = (net.neoforged.neoforge.transfer.ResourceHandler<net.neoforged.neoforge.transfer.item.ItemResource>) resourceHandler;
            nativeHandler = typed;
        }
        if (nativeHandler != null) {
            return (int) Math.max(0L, Math.min((long) stack.getMaxStackSize(), nativeHandler.getCapacityAsLong(index,
                    net.neoforged.neoforge.transfer.item.ItemResource.of(stack))));
        }
        // Legacy views may simulate insertion, but must never be edited to ask their capacity.
        var current = handler.getStackInSlot(index);
        if (!current.isEmpty() && !ItemStack.isSameItemSameComponents(current, stack)) return 0;
        int maximum = Math.max(0, Math.min(stack.getMaxStackSize(), handler.getSlotLimit(index)));
        var probe = stack.copyWithCount(maximum);
        int accepted = Math.max(0, maximum - handler.insertItem(index, probe, true).getCount());
        return Math.min(maximum, current.getCount() + accepted);
    }

    protected class WidgetSlot extends Slot {

        public WidgetSlot(Container inventory, int index, int xPosition, int yPosition) {
            super(inventory, index, xPosition, yPosition);
        }

        @Override
        public boolean mayPlace(@Nonnull ItemStack stack) {
            return SlotWidget.this.canPutStack(stack) && super.mayPlace(stack);
        }

        @Override
        public boolean mayPickup(@Nonnull Player playerIn) {
            return SlotWidget.this.canTakeStack(playerIn) && super.mayPickup(playerIn);
        }

        @Override
        public void set(@Nonnull ItemStack stack) {
//            if(!SlotWidget.this.canPutStack(stack)) return;
            super.set(stack);
        }

        @Override
        public void setChanged() {
            if (changeListener != null) {
                changeListener.run();
            }
            SlotWidget.this.onSlotChanged();
        }

        @Override
        public boolean isActive() {
            return SlotWidget.this.isEnabled() && (HOVER_SLOT == null || HOVER_SLOT == this);
        }

    }

    public class WidgetSlotItemTransfer extends Slot {
        private static final Container emptyInventory = new SimpleContainer(0);
        @Getter
        private final IItemHandlerModifiable itemHandler;
        private final int index;

        public WidgetSlotItemTransfer(IItemHandlerModifiable itemHandler, int index, int xPosition, int yPosition) {
            super(emptyInventory, index, xPosition, yPosition);
            this.itemHandler = itemHandler;
            this.index = index;
        }

        @Override
        public boolean mayPlace(@Nonnull ItemStack stack) {
            return SlotWidget.this.canPutStack(stack) && (!stack.isEmpty() && this.itemHandler.isItemValid(this.index, stack));
        }

        @Override
        public boolean mayPickup(@Nullable Player playerIn) {
            return SlotWidget.this.canTakeStack(playerIn) && !this.itemHandler.extractItem(index, 1, true).isEmpty();
        }

        @Override
        @Nonnull
        public ItemStack getItem() {
            return this.itemHandler.getStackInSlot(index);
        }

        @Override
        public void set(@Nonnull ItemStack stack) {
            this.itemHandler.setStackInSlot(index, stack);
            this.setChanged();
        }

        @Override
        public void onQuickCraft(@Nonnull ItemStack oldStackIn, @Nonnull ItemStack newStackIn) {

        }

        @Override
        public int getMaxStackSize() {
            return this.itemHandler.getSlotLimit(this.index);
        }

        @Override
        public int getMaxStackSize(@Nonnull ItemStack stack) {
            return slotCapacity(itemHandler, index, stack);
        }

        @NotNull
        @Override
        public ItemStack remove(int amount) {
            var result = this.itemHandler.extractItem(index, amount, false);
            if (changeListener != null && !getItem().isEmpty()) {
                changeListener.run();
            }
            return result;
        }

        @Override
        public void setChanged() {
            if (changeListener != null) {
                changeListener.run();
            }
            SlotWidget.this.onSlotChanged();
        }

        @Override
        public boolean isActive() {
            return SlotWidget.this.isEnabled() && (HOVER_SLOT == null || HOVER_SLOT == this);
        }

    }

    @Override
    public void buildConfigurator(ConfiguratorGroup father) {
        var handler = new ItemStackHandler();
        handler.setStackInSlot(0, Blocks.STONE.asItem().getDefaultInstance());
        father.addConfigurators(new WrapperConfigurator("ldlib.gui.editor.group.preview", new SlotWidget() {
            @Override
            public void updateScreen() {
                super.updateScreen();
                setHoverTooltips(SlotWidget.this.tooltipTexts);
                this.backgroundTexture = SlotWidget.this.backgroundTexture;
                this.hoverTexture = SlotWidget.this.hoverTexture;
                this.drawHoverOverlay = SlotWidget.this.drawHoverOverlay;
                this.drawHoverTips = SlotWidget.this.drawHoverTips;
                this.overlay = SlotWidget.this.overlay;
            }
        }.setCanPutItems(false).setCanTakeItems(false).setHandlerSlot(handler, 0)));

        IConfigurableWidget.super.buildConfigurator(father);
    }

    public static final class REICallWrapper {
        public static List<Object> getReiIngredients(Stream<ItemStack> stream) {
            return List.of(EntryIngredient.of(stream.map(EntryStacks::of).toList()));
        }

        public static List<Object> getReiIngredients(UnaryOperator<ItemStack> realStack, List<Pair<TagKey<Item>, Integer>> list) {
            return list.stream()
                    .map(pair -> EntryIngredients.ofTag(com.lowdragmc.lowdraglib.Platform.getFrozenRegistry(), pair.getFirst(), holder -> EntryStacks.of(realStack.apply(new ItemStack(holder.value(), pair.getSecond())))))
                    .collect(Collectors.toList());
        }

        public static List<Object> getReiIngredients(ItemStack stack) {
            return List.of(EntryStacks.of(stack));
        }
    }

    public static final class EMICallWrapper {
        public static List<Object> getEmiIngredients(Stream<ItemStack> stream, float xeiChance) {
            return List.of(EmiIngredient.of(stream.map(EmiStack::of).toList()).setChance(xeiChance));
        }
        public static List<Object> getEmiIngredients(List<Pair<TagKey<Item>, Integer>> list, float xeiChance) {
            return list.stream()
                    .map(pair -> EmiIngredient.of(pair.getFirst()).setAmount(pair.getSecond()).setChance(xeiChance))
                    .collect(Collectors.toList());
        }
        public static List<Object> getEmiIngredients(ItemStack stack, float xeiChance) {
            return List.of(EmiStack.of(stack).setChance(xeiChance));
        }

        public static Object getEmiIngredient(ItemStack stack, float xeiChance) {
            return EmiStack.of(stack).setChance(xeiChance);
        }

        public static boolean mouseClicked(Object ingredient, int button) {
            if (ingredient instanceof EmiStack emiStack) {
                return EmiScreenManager.stackInteraction(new EmiStackInteraction(emiStack), (bind) -> bind.matchesMouse(button));
            }
            return false;
        }

        public static boolean keyPressed(Object ingredient, int keyCode, int scanCode, int modifiers) {
            if (ingredient instanceof EmiStack emiStack) {
                return EmiScreenManager.stackInteraction(new EmiStackInteraction(emiStack), (bind) -> bind.matchesKey(keyCode, scanCode));
            }
            return false;
        }
    }
}
