package com.firemerald.additionalplacements.common.neoforge;

import java.util.ArrayList;
import java.util.List;

import com.firemerald.additionalplacements.common.CommonModEvents;
import com.firemerald.additionalplacements.compat.LoadedMods;
import com.firemerald.additionalplacements.neoforge.AdditionalPlacementsNeoForge;
import com.firemerald.additionalplacements.generation.neoforge.RegistrationImpl;
import com.firemerald.additionalplacements.network.neoforge.APNetworkImpl;
import com.firemerald.additionalplacements.network.neoforge.CheckDataConfigurationTaskImpl;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.network.event.RegisterConfigurationTasksEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.registries.NewRegistryEvent;
import net.neoforged.neoforge.registries.RegisterEvent;
import org.apache.commons.lang3.tuple.Pair;

import com.firemerald.additionalplacements.config.APConfigs;
import com.firemerald.additionalplacements.generation.Registration;

import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;

@EventBusSubscriber
public class CommonModEventHandler {
    private static boolean init = false;

    @SubscribeEvent
    public static void onNewRegistry(NewRegistryEvent event) { //best hook I could find for loading a config after all mods have been processed but before registries are built
        if (!init) {
            RegistrationImpl.registerTypes();
            ModLoadingContext ctx = ModLoadingContext.get();
            APConfigs.init(ctx.getActiveContainer()::registerConfig);
            init = true;
        }
    }

    @SubscribeEvent
    public static void onModConfigsLoaded(ModConfigEvent.Loading event) {
        CommonModEvents.onConfigLoaded(event.getConfig());
    }

    @SubscribeEvent
    public static void onModConfigsReloaded(ModConfigEvent.Reloading event) {
        CommonModEvents.onConfigReloaded(event.getConfig());
    }

    @SubscribeEvent
    public static void onBlockRegistry(RegisterEvent event) {
        if (event.getRegistry() == BuiltInRegistries.BLOCK) {
            List<Pair<ResourceKey<Block>, Block>> created = new ArrayList<>();
            BuiltInRegistries.BLOCK.entrySet().forEach(entry -> {
                Identifier name = entry.getKey().identifier();
                Block block = entry.getValue();
                Registration.tryApply(block, name, (key, obj) -> created.add(Pair.of(key, obj)));
            });
            created.forEach(pair -> Registry.register(BuiltInRegistries.BLOCK, pair.getLeft(), pair.getRight()));
            AdditionalPlacementsNeoForge.dynamicRegistration = true;
        }
    }

    @SubscribeEvent
    public static void onFMLCommonSetup(FMLCommonSetupEvent event) {
        LoadedMods.populate();
        CommonModEvents.modifyWaxables();
    }

    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        APNetworkImpl.register(event);
    }

    @SubscribeEvent
    public static void onGatherLoginConfigurationTasks(RegisterConfigurationTasksEvent event) {
        event.register(new CheckDataConfigurationTaskImpl());
    }
}