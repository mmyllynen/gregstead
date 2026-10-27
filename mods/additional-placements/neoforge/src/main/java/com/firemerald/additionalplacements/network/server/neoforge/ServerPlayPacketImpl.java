package com.firemerald.additionalplacements.network.server.neoforge;

import com.firemerald.additionalplacements.network.client.ClientPlayPacket;
import com.firemerald.additionalplacements.network.client.neoforge.ClientPlayPacketImpl;
import com.firemerald.additionalplacements.network.server.ServerPlayPacket;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public interface ServerPlayPacketImpl extends ServerPacketImpl<RegistryFriendlyByteBuf>, ServerPlayPacket {
    @Override
    default void handleImpl(IPayloadContext context) {
        ClientPlayPacket reply = handleServer((ServerPlayer) context.player(), context::enqueueWork);
        if (reply instanceof ClientPlayPacketImpl packet) packet.reply(context);
    }
}
