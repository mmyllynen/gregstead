package com.firemerald.additionalplacements.network.neoforge;

import com.firemerald.additionalplacements.AdditionalPlacementsMod;
import com.firemerald.additionalplacements.network.APPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.PacketFlow;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public interface APPacketImpl<T extends FriendlyByteBuf> extends APPacket<T> {
    PacketFlow getFlow();

    void handleImpl(IPayloadContext context);

    default void handle(IPayloadContext context) {
        if (context.flow() == getFlow()) handleImpl(context);
        else AdditionalPlacementsMod.LOGGER.error("Tried to handle {} with invalid flow {}", getClass(), context.flow());
    }

    default void reply(IPayloadContext context) {
        context.reply(this);
    }
}
