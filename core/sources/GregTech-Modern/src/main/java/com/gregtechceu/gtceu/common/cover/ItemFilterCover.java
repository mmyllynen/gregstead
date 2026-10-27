package com.gregtechceu.gtceu.common.cover;

import com.gregtechceu.gtceu.api.capability.ICoverable;
import com.gregtechceu.gtceu.api.cover.CoverBehavior;
import com.gregtechceu.gtceu.api.cover.CoverDefinition;
import com.gregtechceu.gtceu.api.cover.IUICover;
import com.gregtechceu.gtceu.api.cover.filter.ItemFilter;
import com.gregtechceu.gtceu.api.cover.filter.SmartItemFilter;
import com.gregtechceu.gtceu.api.gui.widget.EnumSelectorWidget;
import com.gregtechceu.gtceu.api.machine.MachineCoverContainer;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;
import com.gregtechceu.gtceu.api.sync_system.annotations.SyncToClient;
import com.gregtechceu.gtceu.api.transfer.item.ItemHandlerDelegate;
import com.gregtechceu.gtceu.common.cover.data.FilterMode;
import com.gregtechceu.gtceu.common.cover.data.ManualIOMode;

import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import com.lowdragmc.lowdraglib.transfer.item.IItemHandlerModifiable;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ItemFilterCover extends CoverBehavior implements IUICover {

    protected ItemFilter itemFilter;
    @SaveField
    @SyncToClient
    @Getter
    protected FilterMode filterMode = FilterMode.FILTER_INSERT;
    private FilteredItemHandlerWrapper itemFilterWrapper;
    @SaveField
    @Setter
    @Getter
    protected ManualIOMode allowFlow = ManualIOMode.DISABLED;

    public ItemFilterCover(CoverDefinition definition, ICoverable coverHolder, Direction attachedSide) {
        super(definition, coverHolder, attachedSide);
    }

    public ItemFilter getItemFilter() {
        if (itemFilter == null) {
            itemFilter = ItemFilter.loadFilter(attachItem);
            if (itemFilter instanceof SmartItemFilter smart && coverHolder instanceof MachineCoverContainer mcc) {
                var machine = MetaMachine.getMachine(mcc.getLevel(), mcc.getBlockPos());
                if (machine != null) smart.setModeFromMachine(machine.getDefinition().getName());
            }
        }
        return itemFilter;
    }

    public void setFilterMode(FilterMode filterMode) {
        this.filterMode = filterMode;
        syncDataHolder.markClientSyncFieldDirty("filterMode");
    }

    @Override
    public boolean canAttach() {
        return super.canAttach() && coverHolder.getItemHandlerCap(attachedSide, false) != null;
    }

    @Override
    public @Nullable IItemHandlerModifiable getItemHandlerCap(IItemHandlerModifiable defaultValue) {
        if (defaultValue == null) {
            return null;
        }
        if (itemFilterWrapper == null || itemFilterWrapper.delegate != defaultValue) {
            this.itemFilterWrapper = new FilteredItemHandlerWrapper(defaultValue);
        }
        return itemFilterWrapper;
    }

    @Override
    public void onAttached(ItemStack itemStack, @Nullable ServerPlayer player) {
        super.onAttached(itemStack, player);
    }

    @Override
    public Widget createUIWidget() {
        final var group = new WidgetGroup(0, 0, 178, 85);
        group.addWidget(new LabelWidget(60, 5, com.lowdragmc.lowdraglib.transfer.name.ItemNames.descriptionId(attachItem)));
        group.addWidget(new EnumSelectorWidget<>(35, 25, 18, 18,
                FilterMode.VALUES, filterMode, this::setFilterMode));
        group.addWidget(new EnumSelectorWidget<>(35, 45, 18, 18, ManualIOMode.VALUES, allowFlow, this::setAllowFlow));
        group.addWidget(getItemFilter().openConfigurator(62, 25));
        return group;
    }

    private class FilteredItemHandlerWrapper extends ItemHandlerDelegate {

        @Override
        public net.neoforged.neoforge.transfer.ResourceHandler<net.neoforged.neoforge.transfer.item.ItemResource> nativeView() {
            return new com.lowdragmc.lowdraglib.transfer.item.DirectionalItemView(
                    () -> com.lowdragmc.lowdraglib.transfer.item.NativeItemViews.require(delegate), stack -> true,
                    stack -> com.lowdragmc.lowdraglib.transfer.NativeManualTransferPolicy.allows(filterMode == FilterMode.FILTER_EXTRACT,
                            allowFlow == ManualIOMode.DISABLED, allowFlow == ManualIOMode.UNFILTERED,
                            () -> getItemFilter().test(stack)),
                    stack -> com.lowdragmc.lowdraglib.transfer.NativeManualTransferPolicy.allows(filterMode == FilterMode.FILTER_INSERT,
                            allowFlow == ManualIOMode.DISABLED, allowFlow == ManualIOMode.UNFILTERED,
                            () -> getItemFilter().test(stack)));
        }

        public FilteredItemHandlerWrapper(IItemHandlerModifiable delegate) {
            super(delegate);
        }

        @Override
        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            if (filterMode == FilterMode.FILTER_EXTRACT) {
                if (allowFlow == ManualIOMode.DISABLED) {
                    return stack;
                }
                if (allowFlow == ManualIOMode.UNFILTERED) {
                    return super.insertItem(slot, stack, simulate);
                }
            }
            if (!getItemFilter().test(stack)) {
                return stack;
            }
            return super.insertItem(slot, stack, simulate);
        }

        @Override
        public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
            if (filterMode == FilterMode.FILTER_INSERT) {
                if (allowFlow == ManualIOMode.DISABLED) {
                    return ItemStack.EMPTY;
                }
                if (allowFlow == ManualIOMode.UNFILTERED) {
                    return super.extractItem(slot, amount, simulate);
                }
            }
            ItemStack result = super.extractItem(slot, amount, true);
            if (result.isEmpty() || !getItemFilter().test(result)) {
                return ItemStack.EMPTY;
            }
            return simulate ? result : super.extractItem(slot, amount, false);
        }
    }

    @Override
    public CompoundTag copyConfig(CompoundTag tag) {
        tag.putInt("manualIO", getAllowFlow().ordinal());
        tag.putInt("filterMode", getFilterMode().ordinal());
        tag.put("filter", com.gregtechceu.gtceu.api.sync_system.data_transformers.NativeSyncValueCodecs.item(attachItem, coverHolder.getLevel().registryAccess()));
        return super.copyConfig(tag);
    }

    @Override
    public void pasteConfig(ServerPlayer player, CompoundTag tag) {
        setAllowFlow(ManualIOMode.values()[tag.getIntOr("manualIO", 0)]);
        setFilterMode(FilterMode.values()[tag.getIntOr("filterMode", 0)]);
        itemFilter = ItemFilter.loadFilter(com.gregtechceu.gtceu.api.sync_system.data_transformers.NativeSyncValueCodecs.item(tag.getCompoundOrEmpty("filter"), coverHolder.getLevel().registryAccess()));
        super.pasteConfig(player, tag);
    }
}
