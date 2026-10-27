package com.gregtechceu.gtceu.common.network.packets;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.electric.CentralMonitorMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.electric.monitor.MonitorGroup;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import com.lowdragmc.lowdraglib.transfer.item.IItemHandlerModifiable;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import org.jetbrains.annotations.NotNull;


public class SCPacketMonitorGroupNBTChange implements CustomPacketPayload {

    public static final Identifier ID = GTCEu.id("spacket_monitor_group_nbt_change");
    public static final Type<SCPacketMonitorGroupNBTChange> TYPE = new Type<>(ID);
    public static final StreamCodec<RegistryFriendlyByteBuf, SCPacketMonitorGroupNBTChange> CODEC = StreamCodec
            .ofMember(SCPacketMonitorGroupNBTChange::encode, SCPacketMonitorGroupNBTChange::new);

    private final ItemStack stack;
    private final int monitorGroupId;
    private final BlockPos pos;

    public SCPacketMonitorGroupNBTChange(ItemStack stack, MonitorGroup group, CentralMonitorMachine machine) {
        this.stack = stack;
        this.monitorGroupId = machine.getMonitorGroups().indexOf(group);
        this.pos = machine.getBlockPos();
    }

    public SCPacketMonitorGroupNBTChange(RegistryFriendlyByteBuf buf) {
        this.stack = ItemStack.STREAM_CODEC.decode(buf);
        this.monitorGroupId = buf.readVarInt();
        this.pos = buf.readBlockPos();
    }

    public void encode(RegistryFriendlyByteBuf buffer) {
        ItemStack.STREAM_CODEC.encode(buffer, stack);
        // buffer.writeItemStack(stack, false);
        buffer.writeVarInt(monitorGroupId);
        buffer.writeBlockPos(pos);
    }

    public void execute(IPayloadContext context) {
        Level level = context.player().level();

        MetaMachine machine = MetaMachine.getMachine(level, pos);
        if (machine instanceof CentralMonitorMachine centralMonitor) {
            var groups = centralMonitor.getMonitorGroups();
            if (monitorGroupId < 0 || monitorGroupId >= groups.size()) return;
            IItemHandlerModifiable itemHandler = groups.get(monitorGroupId).getItemStackHandler();
            if (itemHandler.getSlots() == 0) return;
            if (ItemStack.isSameItem(itemHandler.getStackInSlot(0), stack)) {
                itemHandler.setStackInSlot(0, stack);
            }
        }
    }

    @Override
    public @NotNull Type<SCPacketMonitorGroupNBTChange> type() {
        return TYPE;
    }
}
