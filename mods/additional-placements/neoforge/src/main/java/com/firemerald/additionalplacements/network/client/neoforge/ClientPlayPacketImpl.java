package com.firemerald.additionalplacements.network.client.neoforge;

import com.firemerald.additionalplacements.network.client.ClientPlayPacket;
import com.firemerald.additionalplacements.network.server.ServerPlayPacket;
import com.firemerald.additionalplacements.network.server.neoforge.ServerPlayPacketImpl;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public interface ClientPlayPacketImpl extends ClientPacketImpl<RegistryFriendlyByteBuf>, ClientPlayPacket {
    @Override
    default void handleImpl(IPayloadContext context) {
        ServerPlayPacket reply = handleClient(context::enqueueWork);
        if (reply instanceof ServerPlayPacketImpl packet) packet.reply(context);
    }
}
