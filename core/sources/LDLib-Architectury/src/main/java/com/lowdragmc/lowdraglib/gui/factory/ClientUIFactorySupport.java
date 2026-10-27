package com.lowdragmc.lowdraglib.gui.factory;

import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.modular.ModularUIGuiContainer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.RegistryFriendlyByteBuf;

/** Client-only screen construction kept outside the common scripted UI factory's verifier surface. */
final class ClientUIFactorySupport {
    private ClientUIFactorySupport() {}
    static net.minecraft.world.level.block.entity.BlockEntity readBlock(RegistryFriendlyByteBuf data) {
        var level = Minecraft.getInstance().level;
        return level == null ? null : level.getBlockEntity(data.readBlockPos());
    }
    static HeldItemUIFactory.HeldItemHolder readHeldItem(RegistryFriendlyByteBuf data) {
        var player = Minecraft.getInstance().player;
        return player == null ? null : new HeldItemUIFactory.HeldItemHolder(player, data.readEnum(net.minecraft.world.InteractionHand.class));
    }
    static <T> void open(UIFactory<T> factory, RegistryFriendlyByteBuf serializedHolder, int windowId) {
        T holder = factory.readHolderFromSyncData(serializedHolder);
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer entityPlayer = minecraft.player;
        ModularUI uiTemplate = factory.createUITemplate(holder, entityPlayer);
        if (uiTemplate == null) return;
        uiTemplate.initWidgets();
        ModularUIGuiContainer container = new ModularUIGuiContainer(uiTemplate, windowId);
        uiTemplate.mainGroup.readInitialData(serializedHolder);
        minecraft.gui.setScreen(container);
        minecraft.player.containerMenu = container.getMenu();
    }
}
