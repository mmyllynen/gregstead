package com.gregtechceu.gtceu.api.sync_system.data_transformers.gtceu;

import com.gregtechceu.gtceu.api.sync_system.data_transformers.ValueTransformer;
import com.gregtechceu.gtceu.api.transfer.item.CustomItemStackHandler;
import com.gregtechceu.gtceu.common.machine.multiblock.electric.monitor.MonitorGroup;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.*;

import org.jetbrains.annotations.Nullable;

public class MonitorGroupTransformer implements ValueTransformer<MonitorGroup> {

    @Override
    public CompoundTag serializeNBT(MonitorGroup value, ValueTransformer.TransformerContext<MonitorGroup> context) {
        CompoundTag tag = new CompoundTag();
        tag.putString("name", value.getName());
        ListTag list = new ListTag();
        value.getMonitorPositions().forEach(pos -> list.add(com.lowdragmc.lowdraglib.transfer.persistence.NbtCompatibility.blockPosTag(pos)));
        if (value.getTargetRaw() != null) {
            tag.put("targetPos", com.lowdragmc.lowdraglib.transfer.persistence.NbtCompatibility.blockPosTag(value.getTargetRaw()));
            if (value.getTargetCoverSide() != null) {
                tag.putString("targetSide", value.getTargetCoverSide().getSerializedName());
            }
        }
        tag.put("positions", list);
        tag.putInt("dataSlot", value.getDataSlot());
        tag.put("items", value.getItemStackHandler().serializeNBT(context.lookup()));
        tag.put("placeholderSlots", value.getPlaceholderSlotsHandler().serializeNBT(context.lookup()));
        return tag;
    }

    @Override
    public @Nullable MonitorGroup deserializeNBT(Tag tag, ValueTransformer.TransformerContext<MonitorGroup> context) {
        var compoundTag = ValueTransformer.assertTagType(CompoundTag.class, tag, context);
        CustomItemStackHandler handler = new CustomItemStackHandler(),
                placeholderSlotsHandler = new CustomItemStackHandler();
        handler.deserializeNBT(context.lookup(), compoundTag.getCompoundOrEmpty("items"));
        placeholderSlotsHandler.deserializeNBT(context.lookup(), compoundTag.getCompoundOrEmpty("placeholderSlots"));
        var group = new MonitorGroup(compoundTag.getStringOr("name", ""), handler, placeholderSlotsHandler);
        ListTag list = com.lowdragmc.lowdraglib.transfer.persistence.NbtCompatibility.getList(compoundTag, "positions", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            int[] aint = list.getIntArray(i).orElseGet(() -> new int[0]);
            if (aint.length != 3) continue;
            group.add(new BlockPos(aint[0], aint[1], aint[2]));
        }
        if (com.lowdragmc.lowdraglib.transfer.persistence.NbtCompatibility.contains(compoundTag, "targetPos", Tag.TAG_COMPOUND)) {
            group.setTarget(com.lowdragmc.lowdraglib.transfer.persistence.NbtCompatibility.blockPosFromTag(compoundTag, "targetPos").orElse(BlockPos.ZERO));
            if (com.lowdragmc.lowdraglib.transfer.persistence.NbtCompatibility.contains(compoundTag, "targetSide", Tag.TAG_STRING)) {
                group.setTargetCoverSide(Direction.byName(compoundTag.getStringOr("targetSide", "")));
            }
            if (com.lowdragmc.lowdraglib.transfer.persistence.NbtCompatibility.contains(compoundTag, "dataSlot", Tag.TAG_INT)) {
                group.setDataSlot(compoundTag.getIntOr("dataSlot", 0));
            }
        }
        return group;
    }
}
