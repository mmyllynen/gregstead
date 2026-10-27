package com.firemerald.additionalplacements.network.neoforge;

import com.firemerald.additionalplacements.AdditionalPlacementsMod;
import com.firemerald.additionalplacements.network.APStreamCodec;
import com.firemerald.additionalplacements.network.client.*;
import com.firemerald.additionalplacements.network.client.neoforge.*;
import com.firemerald.additionalplacements.network.server.*;

import com.firemerald.additionalplacements.network.server.neoforge.*;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import java.util.function.Function;

public class APNetworkImpl {
    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(AdditionalPlacementsMod.MOD_ID);
        playToServer(registrar, SetPlacementTogglePacketImpl.TYPE, SetPlacementTogglePacketImpl::new);
        configurationToClient(registrar, CheckDataClientPacketImpl.TYPE, CheckDataClientPacketImpl::new);
        configurationToServer(registrar, CheckDataServerPacketImpl.TYPE, CheckDataServerPacketImpl::new);
        configurationToClient(registrar, ConfigurationCheckFailedPacketImpl.TYPE, ConfigurationCheckFailedPacketImpl::new);
    }

    public static <T extends ServerPlayPacketImpl> void playToServer(PayloadRegistrar registrar, CustomPacketPayload.Type<T> type, Function<RegistryFriendlyByteBuf, T> constructor) {
        registrar.playToServer(type, new APStreamCodec<>(constructor), APPacketImpl::handle);
    }

    public static <T extends ClientPlayPacketImpl> void playToClient(PayloadRegistrar registrar, CustomPacketPayload.Type<T> type, Function<RegistryFriendlyByteBuf, T> constructor) {
        registrar.playToClient(type, new APStreamCodec<>(constructor), APPacketImpl::handle);
    }

    public static <T extends ServerConfigurationPacketImpl> void configurationToServer(PayloadRegistrar registrar, CustomPacketPayload.Type<T> type, Function<FriendlyByteBuf, T> constructor) {
        registrar.configurationToServer(type, new APStreamCodec<>(constructor), APPacketImpl::handle);
    }

    public static <T extends ClientConfigurationPacketImpl> void configurationToClient(PayloadRegistrar registrar, CustomPacketPayload.Type<T> type, Function<FriendlyByteBuf, T> constructor) {
        registrar.configurationToClient(type, new APStreamCodec<>(constructor), APPacketImpl::handle);
    }

    public static void sendToServer(ServerPacket<?> packet) {
        if (packet instanceof ServerPacketImpl<?> serverPacket) ClientPacketDistributor.sendToServer(serverPacket);
    }

    public static void sendToClient(ClientPacket<?> packet, ServerPlayer player) {
        if (packet instanceof ClientPacketImpl<?> clientPacket) PacketDistributor.sendToPlayer(player, clientPacket);
    }
}