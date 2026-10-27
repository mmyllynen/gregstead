package com.firemerald.additionalplacements.common.neoforge;

import com.firemerald.additionalplacements.common.CommonModEvents;
import com.firemerald.additionalplacements.common.TagMismatchChecker;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.TagsUpdatedEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;

@EventBusSubscriber
public class CommonEventHandler {
    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        CommonModEvents.onItemTooltip(event.getItemStack(), event.getContext(), event.getFlags(), event.getToolTip());
    }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        CommonModEvents.onRegisterCommands(event.getDispatcher(), event.getBuildContext(), event.getCommandSelection());
    }

    @SubscribeEvent
    public static void onTagsUpdated(TagsUpdatedEvent event) {
        CommonModEvents.onTagsUpdated(event instanceof TagsUpdatedEvent.ClientPacketReceived);
    }

    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        CommonModEvents.onServerStarted(event.getServer());
    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (CommonModEvents.misMatchedTags && TagMismatchChecker.canGenerateTags(event.getEntity())) event.getEntity().sendSystemMessage(CommonModEvents.autoGenerateFailed ? TagMismatchChecker.FAILED : TagMismatchChecker.MESSAGE);
    }

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        CommonModEvents.onServerStopping(event.getServer());
    }
}