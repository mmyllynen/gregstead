package com.lowdragmc.lowdraglib.gui.factory;

import com.lowdragmc.lowdraglib.core.mixins.accessor.ServerPlayerAccessor;
import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.modular.ModularUIContainer;
import com.lowdragmc.lowdraglib.gui.modular.ModularUIGuiContainer;
import com.lowdragmc.lowdraglib.networking.s2c.SPacketUIOpen;
import com.lowdragmc.lowdraglib.utils.ByteBufUtil;
import dev.latvian.mods.rhino.util.HideFromJS;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerContainerEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.HashMap;
import java.util.Map;

public abstract class UIFactory<T> {
    public final Identifier uiFactoryId;
    public static final Map<Identifier, UIFactory<?>> FACTORIES = new HashMap<>();

    public UIFactory(Identifier uiFactoryId){
        this.uiFactoryId = uiFactoryId;
    }
    
    public static void register(UIFactory<?> factory) {
        FACTORIES.put(factory.uiFactoryId, factory);
    }

    @HideFromJS
    public final boolean openUI(T holder, ServerPlayer player) {
        ModularUI uiTemplate = createUITemplate(holder, player);
        if (uiTemplate == null) return false;
        uiTemplate.initWidgets();

        if (player.containerMenu != player.inventoryMenu) {
            player.closeContainer();
        }
        ((ServerPlayerAccessor)player).callNextContainerCounter();
        int currentWindowId = ((ServerPlayerAccessor)player).getContainerCounter();

        ModularUIContainer container = new ModularUIContainer(uiTemplate, currentWindowId);

        //accumulate all initial updates of widgets in open packet
        var data = ByteBufUtil.writeCustomData(buf -> {
            writeHolderToSyncData(buf, holder);
            uiTemplate.mainGroup.writeInitialData(buf);
        }, player.level().getServer().registryAccess());

        PacketDistributor.sendToPlayer(player, new SPacketUIOpen(uiFactoryId, data, currentWindowId));

        ((ServerPlayerAccessor)player).callInitMenu(container);
        player.containerMenu = container;

        NeoForge.EVENT_BUS.post(new PlayerContainerEvent.Open(player, container));
        return true;
    }

    @OnlyIn(Dist.CLIENT)
    public final void initClientUI(RegistryFriendlyByteBuf serializedHolder, int windowId) {
        ClientUIFactorySupport.open(this, serializedHolder, windowId);
    }

    protected abstract ModularUI createUITemplate(T holder, Player entityPlayer);

    @OnlyIn(Dist.CLIENT)
    protected abstract T readHolderFromSyncData(RegistryFriendlyByteBuf syncData);

    protected abstract void writeHolderToSyncData(RegistryFriendlyByteBuf syncData, T holder);

}
