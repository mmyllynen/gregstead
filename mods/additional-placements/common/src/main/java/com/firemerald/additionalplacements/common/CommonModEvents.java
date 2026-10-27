package com.firemerald.additionalplacements.common;

import com.firemerald.additionalplacements.AdditionalPlacementsMod;
import com.firemerald.additionalplacements.block.interfaces.IPlacementBlock;
import com.firemerald.additionalplacements.commands.CommandExportTags;
import com.firemerald.additionalplacements.commands.CommandGenerateStairsDebugger;
import com.firemerald.additionalplacements.config.APConfigs;
import com.firemerald.additionalplacements.generation.Registration;
import com.google.common.base.Suppliers;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.WeatheringCopper;
import net.neoforged.fml.config.ModConfig;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

public class CommonModEvents {
    public static boolean misMatchedTags = false, autoGenerateFailed = false;
    protected static boolean reloadedFromChecker = false;

    public static void onItemTooltip(ItemStack stack, Item.TooltipContext context, TooltipFlag tooltipType, List<Component> lines) {
        if (stack.getItem() instanceof BlockItem) {
            Block block = ((BlockItem) stack.getItem()).getBlock();
            if (block instanceof IPlacementBlock<?> verticalBlock) {
                if (verticalBlock.additionalplacements$hasAdditionalStates()) verticalBlock.additionalplacements$appendHoverTextImpl(stack, context, lines, tooltipType);
            }
        }
    }

    public static void onRegisterCommands(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext registryAccess, Commands.CommandSelection environment) {
        CommandExportTags.register(dispatcher);
        CommandGenerateStairsDebugger.register(dispatcher, registryAccess);
    }

    public static void onTagsUpdated(boolean client) {
        if (!client) {
            Registration.forEach(type -> type.onTagsUpdated(false));
            boolean fromAutoGenerate;
            if (reloadedFromChecker) {
                reloadedFromChecker = false;
                fromAutoGenerate = true;
            } else fromAutoGenerate = false;
            if (currentServer != null) possiblyCheckTags(fromAutoGenerate);
        }
        else Registration.forEach(type -> type.onTagsUpdated(true));
    }

    private static void possiblyCheckTags(boolean fromAutoGenerate) {
        misMatchedTags = false;
        autoGenerateFailed = false;
        if (APConfigs.common().checkTags.get() && APConfigs.server().checkTags.get()) {
            TagMismatchChecker.startChecker(currentServer, !fromAutoGenerate && APConfigs.common().autoRebuildTags.get() && APConfigs.server().autoRebuildTags.get(), fromAutoGenerate); //TODO halt on datapack reload
        }
    }

    private static MinecraftServer currentServer = null;

    public static void onServerStarted(MinecraftServer server) {
        currentServer = server;
        possiblyCheckTags(false);
    }

    public static void onServerStopping(MinecraftServer server) {
        currentServer = null;
        TagMismatchChecker.stopChecker();
        reloadedFromChecker = false;
    }


    public static void modifyWaxables() {
        // NeoForge 26.3 uses data maps, expanded in PlacementDataMaps before its inverse caches are rebuilt.
    }

    public static void onConfigLoaded(ModConfig config) {
        APConfigs.onConfigLoaded(config.getSpec());
    }

    public static void onConfigReloaded(ModConfig config) {
        APConfigs.onConfigReloaded(config.getSpec());
    }
}
