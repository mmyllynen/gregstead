package com.gregtechceu.gtceu.api.gui.widget;

import com.gregtechceu.gtceu.GTCEu;

import com.lowdragmc.lowdraglib.editor.annotation.ConfigSetter;
import com.lowdragmc.lowdraglib.editor.annotation.Configurable;
import com.lowdragmc.lowdraglib.registry.annotation.LDLRegister;
import com.lowdragmc.lowdraglib.editor.annotation.NumberRange;
import com.lowdragmc.lowdraglib.editor.configurator.IConfigurableWidget;
import com.lowdragmc.lowdraglib.gui.ingredient.IGhostIngredientTarget;
import com.lowdragmc.lowdraglib.gui.ingredient.Target;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import com.lowdragmc.lowdraglib.transfer.item.IItemHandlerModifiable;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.InputConstants;
import dev.emi.emi.api.stack.EmiStack;
import mezz.jei.api.ingredients.ITypedIngredient;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

@LDLRegister(name = "gtm_phantom_item_slot", group = "widget.gtm_container", priority = 50, registry = "ldlib:widget")
public class PhantomSlotWidget extends SlotWidget implements IGhostIngredientTarget, IConfigurableWidget {

    private boolean clearSlotOnRightClick;

    @Configurable
    @NumberRange(range = { 0, 64 })
    private int maxStackSize = 64;

    private Predicate<ItemStack> validator = stack -> true;

    public PhantomSlotWidget() {
        super();
    }

    public PhantomSlotWidget(IItemHandlerModifiable itemHandler, int slotIndex, int xPosition, int yPosition) {
        super(itemHandler, slotIndex, xPosition, yPosition, true, true);
    }

    public PhantomSlotWidget(IItemHandlerModifiable itemHandler, int slotIndex, int xPosition, int yPosition,
                             Predicate<ItemStack> validator) {
        super(itemHandler, slotIndex, xPosition, yPosition, true, true);
        this.validator = validator;
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

    public PhantomSlotWidget setMaxStackSize(int stackSize) {
        maxStackSize = stackSize;
        return this;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (slotReference != null && isMouseOverElement(mouseX, mouseY) && gui != null) {
            if (isClientSideWidget && !gui.getModularUIContainer().getCarried().isEmpty()) {
                slotReference.set(gui.getModularUIContainer().getCarried());
            } else if (button == 1 && clearSlotOnRightClick && !slotReference.getItem().isEmpty()) {
                slotReference.set(ItemStack.EMPTY);
                writeClientAction(2, buf -> {});
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

    @Nullable
    private static Object convertIngredient(Object ingredient) {
        if (GTCEu.Mods.isEMILoaded() && ingredient instanceof EmiStack emiStack) {
            Item item = emiStack.getKeyOfType(Item.class);
            if (item != null) {
                ingredient = new ItemStack(item, (int) emiStack.getAmount());
                ((ItemStack) ingredient).applyComponents(emiStack.getComponentChanges());
            }
        } else if (GTCEu.Mods.isJEILoaded() && ingredient instanceof ITypedIngredient<?> jeiStack) {
            ingredient = jeiStack.getItemStack().orElse(null);
        }
        return ingredient;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public List<Target> getPhantomTargets(Object ingredient) {
        ingredient = convertIngredient(ingredient);
        if (!(ingredient instanceof ItemStack)) {
            return Collections.emptyList();
        }

        Rect2i rectangle = toRectangleBox();
        return Lists.newArrayList(new Target() {

            @NotNull
            @Override
            public Rect2i getArea() {
                return rectangle;
            }

            @Override
            public void accept(@NotNull Object ingredient) {
                ingredient = convertIngredient(ingredient);
                if (slotReference != null && ingredient instanceof ItemStack stack) {

                    boolean shiftDown = InputConstants.isKeyDown(InputConstants.KEY_LSHIFT);
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
                stackHeld, maxStackSize, validator, this::areItemsEqual);
    }

    public boolean areItemsEqual(ItemStack itemStack1, ItemStack itemStack2) {
        return ItemStack.matches(itemStack1, itemStack2);
    }
}
