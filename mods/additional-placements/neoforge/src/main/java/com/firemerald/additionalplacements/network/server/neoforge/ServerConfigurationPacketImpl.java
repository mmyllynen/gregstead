package com.firemerald.additionalplacements.network.server.neoforge;

import com.firemerald.additionalplacements.network.client.ClientConfigurationPacket;
import com.firemerald.additionalplacements.network.client.neoforge.ClientConfigurationPacketImpl;
import com.firemerald.additionalplacements.network.server.ServerConfigurationPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public interface ServerConfigurationPacketImpl extends ServerPacketImpl<FriendlyByteBuf>, ServerConfigurationPacket {
    @Override
    default void handleImpl(IPayloadContext context) {
        ClientConfigurationPacket reply = handleServer(context::enqueueWork, context::disconnect, context::finishCurrentTask);
        if (reply instanceof ClientConfigurationPacketImpl packet) packet.reply(context);
    }
}
