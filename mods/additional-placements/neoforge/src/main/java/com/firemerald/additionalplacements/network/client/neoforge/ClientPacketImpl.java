package com.firemerald.additionalplacements.network.client.neoforge;

import com.firemerald.additionalplacements.network.neoforge.APPacketImpl;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.PacketFlow;

public interface ClientPacketImpl<T extends FriendlyByteBuf> extends APPacketImpl<T> {
    @Override
    default PacketFlow getFlow() {
        return PacketFlow.CLIENTBOUND;
    }
}
