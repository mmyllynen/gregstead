package com.lowdragmc.lowdraglib.gui.modular;

import net.neoforged.neoforge.client.network.ClientPacketDistributor;

import com.lowdragmc.lowdraglib.Platform;
import com.lowdragmc.lowdraglib.core.mixins.accessor.AbstractContainerMenuAccessor;
import com.lowdragmc.lowdraglib.gui.util.PerTickIntCounter;
import com.lowdragmc.lowdraglib.gui.widget.SlotWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.networking.c2s.CPacketUIClientAction;
import com.lowdragmc.lowdraglib.networking.s2c.SPacketUIWidgetUpdate;
import com.lowdragmc.lowdraglib.utils.ByteBufUtil;
import io.netty.buffer.Unpooled;
import lombok.Getter;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

import javax.annotation.Nonnull;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class ModularUIContainer extends AbstractContainerMenu implements WidgetUIAccess {
    public static final MenuType<ModularUIContainer> MENUTYPE = new MenuType<>((i, inventory) -> new ModularUIContainer(i), FeatureFlags.DEFAULT_FLAGS);

    @Getter
    private final ModularUI modularUI;

    public ModularUIContainer(ModularUI modularUI, int windowID) {
        super(MENUTYPE, windowID);
        this.modularUI = modularUI;
        this.modularUI.setModularUIContainer(this);
        modularUI.mainGroup.setUiAccess(this);
    }

    private ModularUIContainer(int windowID) {
        super(null, windowID);
        this.modularUI = null;
    }

    //WARNING! WIDGET CHANGES SHOULD BE *STRICTLY* SYNCHRONIZED BETWEEN SERVER AND CLIENT,
    //OTHERWISE ID MISMATCH CAN HAPPEN BETWEEN ASSIGNED SLOTS!
    @Nonnull
    public Slot addSlot(@Nonnull Slot slotHandle) {
        var emptySlotIndex = slots.stream()
                .filter(it -> it instanceof EmptySlotPlaceholder)
                .mapToInt(slot -> slot.index).findFirst();
        if (emptySlotIndex.isPresent()) {
            slotHandle.index = emptySlotIndex.getAsInt();
            this.slots.set(slotHandle.index, slotHandle);
            ((AbstractContainerMenuAccessor)this).getLastSlots().set(slotHandle.index, ItemStack.EMPTY);
            setRemoteSlot(slotHandle.index, ItemStack.EMPTY);
        } else {
            super.addSlot(slotHandle);
        }
        return slotHandle;
    }

    //WARNING! WIDGET CHANGES SHOULD BE *STRICTLY* SYNCHRONIZED BETWEEN SERVER AND CLIENT,
    //OTHERWISE ID MISMATCH CAN HAPPEN BETWEEN ASSIGNED SLOTS!
    public void removeSlot(Slot slotHandle) {
        //replace removed slot with empty placeholder to avoid list index shift
        EmptySlotPlaceholder emptySlotPlaceholder = new EmptySlotPlaceholder();
        emptySlotPlaceholder.index = slotHandle.index;
        this.slots.set(slotHandle.index, emptySlotPlaceholder);
        ((AbstractContainerMenuAccessor)this).getLastSlots().set(slotHandle.index, ItemStack.EMPTY);
        setRemoteSlot(slotHandle.index, ItemStack.EMPTY);
    }

    @Override
    public void removed(@Nonnull Player playerIn) {
        super.removed(playerIn);
        modularUI.triggerCloseListeners();
    }

    @Override
    public void addSlotListener(@Nonnull ContainerListener pListener) {
        super.addSlotListener(pListener);
        modularUI.mainGroup.detectAndSendChanges();
    }

    @Override
    public void broadcastChanges() {
        super.broadcastChanges();
        if (modularUI.holder.isInvalid() && modularUI.entityPlayer instanceof ServerPlayer serverPlayer) {
            serverPlayer.closeContainer();
        }
        modularUI.mainGroup.detectAndSendChanges();
        modularUI.addTick();
    }

    @Override
    public void clicked(int slotId, int dragType, @Nonnull ContainerInput clickTypeIn, @Nonnull Player player) {
        if (slotId >= 0 && slotId < slots.size()) {
            Slot slot = getSlot(slotId);
            ItemStack result = modularUI.getSlotMap().get(slot).slotClick(dragType, clickTypeIn, player);
            if (result == null) {
//                return super.clicked(slotId, dragType, clickTypeIn, player);
                super.clicked(slotId, dragType, clickTypeIn, player);
            }
//            return result;
        }
        if (slotId == -999) {
            super.clicked(slotId, dragType, clickTypeIn, player);
        }
//        return ItemStack.EMPTY;
    }

    private final PerTickIntCounter transferredPerTick = new PerTickIntCounter(0);

    private List<SlotWidget> getShiftClickSlots(ItemStack itemStack, boolean fromContainer) {
        return modularUI.getSlotMap().values().stream()
                .filter(it -> it.canMergeSlot(itemStack))
                .filter(it -> it.isPlayerContainer == fromContainer)
                .sorted(Comparator.comparing(s -> (fromContainer ? -1 : 1) * s.getHandler().index))
                .collect(Collectors.toList());
    }

    @Override
    public boolean attemptMergeStack(ItemStack itemStack, boolean fromContainer, boolean simulate) {
        List<Slot> slots = getShiftClickSlots(itemStack, fromContainer).stream()
                .map(SlotWidget::getHandler)
                .collect(Collectors.toList());
        return mergeItemStack(itemStack, slots, simulate);
    }

    public static boolean mergeItemStack(ItemStack itemStack, List<Slot> slots, boolean simulate) {
        if (itemStack.isEmpty()) return false;
        boolean merged = false;
        for (boolean empty : new boolean[]{false, true}) {
            for (Slot slot : slots) {
                if (!slot.mayPlace(itemStack)) continue;
                ItemStack before = slot.getItem().copy();
                if (before.isEmpty() != empty) continue;
                if (!empty && !ItemStack.isSameItemSameComponents(itemStack, before)) continue;
                int maximum = Math.max(0, Math.min(itemStack.getMaxStackSize(), slot.getMaxStackSize(itemStack)));
                int amount = Math.min(itemStack.getCount(), Math.max(0, maximum - before.getCount()));
                if (amount <= 0) continue;
                if (!simulate) {
                    ItemStack combined = empty ? itemStack.copyWithCount(amount) : before.copyWithCount(before.getCount() + amount);
                    slot.set(combined);
                    // Slot getters may return detached copies; only committed storage counts as inserted.
                    ItemStack after = slot.getItem();
                    if (!ItemStack.isSameItemSameComponents(after, itemStack)) {
                        slot.set(before);
                        continue;
                    }
                    amount = Math.min(amount, Math.max(0, after.getCount() - before.getCount()));
                    if (amount == 0) continue;
                }
                itemStack.shrink(amount);
                merged = true;
                if (itemStack.isEmpty()) return true;
            }
        }
        return merged;
    }

    @Nonnull
    @Override
    public ItemStack quickMoveStack(@Nonnull Player player, int index) {
        if (index < 0 || index >= slots.size()) return ItemStack.EMPTY;
        Slot slot = slots.get(index);
        var widget = modularUI.getSlotMap().get(slot);
        if (widget == null || !slot.mayPickup(player) || !slot.hasItem()) return ItemStack.EMPTY;
        ItemStack original = slot.getItem().copy();
        ItemStack simulated = original.copy();
        boolean fromContainer = !widget.isPlayerContainer;
        if (!attemptMergeStack(simulated, fromContainer, true)) return ItemStack.EMPTY;
        if (!simulated.isEmpty() && !widget.canMergeSlot(simulated)) return ItemStack.EMPTY;
        int count = original.getCount() - simulated.getCount();
        if (count <= 0 || count + transferredPerTick.get(player.level()) > original.getMaxStackSize()) return ItemStack.EMPTY;
        ItemStack extracted = slot.remove(count);
        if (extracted.isEmpty()) return ItemStack.EMPTY;
        ItemStack taken = extracted.copy();
        try {
            attemptMergeStack(extracted, fromContainer, false);
        } finally {
            // Capacity can change between simulation and commit. Keep uninserted items in their source.
            if (!extracted.isEmpty()) {
                ItemStack remaining = slot.getItem().copy();
                if (remaining.isEmpty()) remaining = extracted.copy();
                else {
                    if (!ItemStack.isSameItemSameComponents(remaining, extracted))
                        throw new IllegalStateException("Source slot changed during quick move");
                    remaining.grow(extracted.getCount());
                }
                slot.set(remaining);
            } else slot.setChanged();
        }
        int moved = taken.getCount() - extracted.getCount();
        if (moved <= 0) return ItemStack.EMPTY;
        transferredPerTick.increment(player.level(), moved);
        return taken.copyWithCount(moved);
    }

    @Override
    public boolean canTakeItemForPickAll(@Nonnull ItemStack stack, @Nonnull Slot slotIn) {
        return modularUI.getSlotMap().get(slotIn).canMergeSlot(stack);
    }

    @Override
    public boolean stillValid(@Nonnull Player playerIn) {
        return true;
    }

    @Override
    public void writeClientAction(Widget widget, int updateId, Consumer<RegistryFriendlyByteBuf> payloadWriter) {
        if (modularUI.entityPlayer instanceof AbstractClientPlayer) {
            ClientPacketDistributor.sendToServer(new CPacketUIClientAction(containerId, ByteBufUtil.writeCustomData(buf -> {
                buf.writeVarInt(updateId);
                payloadWriter.accept(buf);
            }, Platform.getFrozenRegistry())));
        }
    }

    @Override
    public void writeUpdateInfo(Widget widget, int updateId, Consumer<RegistryFriendlyByteBuf> payloadWriter) {
        if (modularUI.entityPlayer instanceof ServerPlayer serverPlayer) {
            PacketDistributor.sendToPlayer(serverPlayer, new SPacketUIWidgetUpdate(containerId, ByteBufUtil.writeCustomData(buf -> {
                buf.writeVarInt(updateId);
                payloadWriter.accept(buf);
            }, serverPlayer.registryAccess())));

        }
    }

    public void handleClientAction(CPacketUIClientAction packet) {
        if (packet.windowId == containerId) {
            ByteBufUtil.readCustomData(packet.updateData, buf -> {
                int updateId = buf.readVarInt();
                modularUI.mainGroup.handleClientAction(updateId, buf);
            }, Platform.getFrozenRegistry());
        }
    }

    private static class EmptySlotPlaceholder extends Slot {

        private static final Container EMPTY_INVENTORY = new SimpleContainer(0);

        public EmptySlotPlaceholder() {
            super(EMPTY_INVENTORY, 0, -100000, -100000);
        }

        @Nonnull
        @Override
        public ItemStack getItem() {
            return ItemStack.EMPTY;
        }
        
        
        @Override
        public void set(@Nonnull ItemStack stack) {
        }

        @Override
        public boolean mayPlace(@Nonnull ItemStack stack) {
            return false;
        }

        @Override
        public boolean mayPickup(@Nonnull Player playerIn) {
            return false;
        }

        @Override
        public boolean isActive() {
            return false;
        }
    }
}
