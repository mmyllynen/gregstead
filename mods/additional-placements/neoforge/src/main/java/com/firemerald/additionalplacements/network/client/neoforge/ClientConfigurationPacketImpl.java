package com.firemerald.additionalplacements.network.client.neoforge;

import com.firemerald.additionalplacements.network.client.ClientConfigurationPacket;
import com.firemerald.additionalplacements.network.server.ServerConfigurationPacket;
import com.firemerald.additionalplacements.network.server.neoforge.ServerConfigurationPacketImpl;
import net.minecraft.network.FriendlyByteBuf;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public interface ClientConfigurationPacketImpl extends ClientPacketImpl<FriendlyByteBuf>, ClientConfigurationPacket {
    @Override
    default void handleImpl(IPayloadContext context) {
        ServerConfigurationPacket reply = handleClient(context::enqueueWork, context::disconnect);
        if (reply instanceof ServerConfigurationPacketImpl packet) packet.reply(context);
    }
}
