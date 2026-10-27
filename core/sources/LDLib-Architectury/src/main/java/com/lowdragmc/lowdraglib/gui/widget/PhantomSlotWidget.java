package com.lowdragmc.lowdraglib.gui.widget;

import com.google.common.collect.Lists;
import com.lowdragmc.lowdraglib.LDLib;
import com.lowdragmc.lowdraglib.editor.annotation.ConfigSetter;
import com.lowdragmc.lowdraglib.editor.annotation.Configurable;
import com.lowdragmc.lowdraglib.registry.annotation.LDLRegister;
import com.lowdragmc.lowdraglib.editor.annotation.NumberRange;
import com.lowdragmc.lowdraglib.editor.configurator.IConfigurableWidget;
import com.lowdragmc.lowdraglib.gui.ingredient.IGhostIngredientTarget;
import com.lowdragmc.lowdraglib.gui.ingredient.Target;
import com.mojang.blaze3d.platform.InputConstants;
import dev.emi.emi.api.stack.EmiStack;
import lombok.Getter;
import lombok.Setter;
import mezz.jei.api.ingredients.ITypedIngredient;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import com.lowdragmc.lowdraglib.transfer.item.IItemHandlerModifiable;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;

@LDLRegister(name = "phantom_item_slot", group = "widget.container", registry = "ldlib:widget")
public class PhantomSlotWidget extends SlotWidget implements IGhostIngredientTarget, IConfigurableWidget {

    @Getter
    private boolean clearSlotOnRightClick;

    @Configurable
    @NumberRange(range = {0, 64})
    @Setter
    @Getter
    private int maxStackSize = 64;

    public PhantomSlotWidget() {
        super();
    }

    public PhantomSlotWidget(IItemHandlerModifiable itemHandler, int slotIndex, int xPosition, int yPosition) {
        super(itemHandler, slotIndex, xPosition, yPosition, true, true);
    }

    public PhantomSlotWidget setClearSlotOnRightClick(boolean clearSlotOnRightClick) {
        this.clearSlotOnRightClick = clearSlotOnRightClick;
        return this;
    }

    @ConfigSetter(field = "canTakeItems")
    public PhantomSlotWidget setCanTakeItems(boolean v) {
        // you cant modify it
        return this;
    }

    @ConfigSetter(field = "canPutItems")
    public PhantomSlotWidget setCanPutItems(boolean v) {
        // you cant modify it
        return this;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (slotReference != null && isMouseOverElement(mouseX, mouseY) && gui != null) {
            if (isClientSideWidget && !gui.getModularUIContainer().getCarried().isEmpty()) {
                slotReference.set(gui.getModularUIContainer().getCarried());
            } else if (button == 1 && clearSlotOnRightClick && !slotReference.getItem().isEmpty()) {
                slotReference.set(ItemStack.EMPTY);
                writeClientAction(2, buf -> {
                });
            } else {
                HOVER_SLOT = slotReference;
                gui.getModularUIGui().superMouseClicked(mouseX, mouseY, button);
                HOVER_SLOT = null;
            }
            return true;
        }
        return false;
    }

    @Override
    public ItemStack slotClick(int dragType, ContainerInput clickTypeIn, Player player) {
        if (slotReference != null && gui != null) {
            ItemStack stackHeld = gui.getModularUIContainer().getCarried();
            return slotClickPhantom(slotReference, dragType, clickTypeIn, stackHeld);
        }
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canMergeSlot(ItemStack stack) {
        return false;
    }

    @Override
    public boolean canTakeStack(Player player) {
        return false;
    }

    @Override
    public boolean canPutStack(ItemStack stack) {
        return false;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public List<Target> getPhantomTargets(Object ingredient) {
        if (LDLib.isEmiLoaded() && ingredient instanceof EmiStack itemEmiStack) {
            Item item = itemEmiStack.getKeyOfType(Item.class);
            ingredient = item == null ? null : new ItemStack(item, (int)itemEmiStack.getAmount());
            if (ingredient instanceof ItemStack itemStack) {
                itemStack.applyComponents(itemEmiStack.getComponentChanges());
            }
        }
        if (LDLib.isJeiLoaded() && ingredient instanceof ITypedIngredient<?> itemJeiStack) {
            ingredient = itemJeiStack.getItemStack().orElse(ItemStack.EMPTY);
        }
        if (!(ingredient instanceof ItemStack)) {
            return Collections.emptyList();
        }
        Rect2i rectangle = toRectangleBox();
        return Lists.newArrayList(new Target() {
            @Nonnull
            @Override
            public Rect2i getArea() {
                return rectangle;
            }

            @Override
            public void accept(@Nonnull Object ingredient) {
                if (LDLib.isEmiLoaded() && ingredient instanceof EmiStack itemEmiStack) {
                    Item item = itemEmiStack.getKeyOfType(Item.class);
                    ingredient = item == null ? null : new ItemStack(item, (int)itemEmiStack.getAmount());
                    if (ingredient instanceof ItemStack itemStack) {
                        itemStack.applyComponents(itemEmiStack.getComponentChanges());
                    }
                }
                if (LDLib.isJeiLoaded() && ingredient instanceof ITypedIngredient<?> itemJeiStack) {
                    ItemStack itemStack = itemJeiStack.getItemStack().orElse(ItemStack.EMPTY);
                    if (!itemStack.isEmpty()) {
                        ingredient = itemStack;
                    }
                }
                if (slotReference != null && ingredient instanceof ItemStack stack) {

                    boolean shiftDown = InputConstants.isKeyDown(InputConstants.KEY_LSHIFT) || InputConstants.isKeyDown(InputConstants.KEY_RSHIFT);
                    ContainerInput clickType = shiftDown ? ContainerInput.QUICK_MOVE : ContainerInput.PICKUP;
                    slotClickPhantom(slotReference, 0, clickType, stack);
                    writeClientAction(1, buffer -> {
                        ItemStack.OPTIONAL_STREAM_CODEC.encode(buffer, stack);
                        buffer.writeVarInt(0);
                        buffer.writeBoolean(shiftDown);
                    });
                }
            }
        });
    }

    @Override
    public void handleClientAction(int id, RegistryFriendlyByteBuf buffer) {
        if (slotReference != null && id == 1) {
            ItemStack stackHeld = ItemStack.OPTIONAL_STREAM_CODEC.decode(buffer);
            int mouseButton = buffer.readVarInt();
            boolean shiftKeyDown = buffer.readBoolean();
            ContainerInput clickType = shiftKeyDown ? ContainerInput.QUICK_MOVE : ContainerInput.PICKUP;
            slotClickPhantom(slotReference, mouseButton, clickType, stackHeld);
        } else if (slotReference != null && id == 2) {
            slotReference.set(ItemStack.EMPTY);
        }
    }

    public ItemStack slotClickPhantom(Slot slot, int mouseButton, ContainerInput clickTypeIn, ItemStack stackHeld) {
        return com.lowdragmc.lowdraglib.transfer.menu.PhantomSlotOperations.click(slot, mouseButton, clickTypeIn,
                stackHeld, maxStackSize, stack -> true, this::areItemsEqual);
    }

    public boolean areItemsEqual(ItemStack itemStack1, ItemStack itemStack2) {
        return ItemStack.matches(itemStack1, itemStack2);
    }
}
