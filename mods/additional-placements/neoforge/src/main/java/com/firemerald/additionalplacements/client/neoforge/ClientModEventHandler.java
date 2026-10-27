package com.firemerald.additionalplacements.client.neoforge;

import com.firemerald.additionalplacements.client.APClientData;
import com.firemerald.additionalplacements.client.models.DynamicModelsDefinition;
import com.firemerald.additionalplacements.client.models.neoforge.DynamicModelsDefinitionImpl;
import com.firemerald.additionalplacements.client.resources.APDynamicResources;
import com.firemerald.additionalplacements.datagen.AdditionalPlacementsModelProvider;
import com.firemerald.additionalplacements.util.PlatformUtils;
import net.minecraft.data.DataProvider;
import net.minecraft.server.packs.PackType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterBlockStateModels;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.neoforge.event.AddPackFindersEvent;

@EventBusSubscriber(value = Dist.CLIENT)
public class ClientModEventHandler {
    static {
        PlatformUtils.checkIsClient();
    }

    @SubscribeEvent
    public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(APClientData.AP_PLACEMENT_KEY);
    }

    @SubscribeEvent
    public static void onGatherClientData(GatherDataEvent.Client event) {
        event.getGenerator().addProvider(true, (DataProvider.Factory<AdditionalPlacementsModelProvider>) AdditionalPlacementsModelProvider::new);
    }

    @SubscribeEvent
    public static void registerBlockStateModels(RegisterBlockStateModels event) {
        event.registerDefinition(DynamicModelsDefinition.ID, DynamicModelsDefinitionImpl.CODEC);
    }

    @SubscribeEvent
    public static void onAddPackFinders(AddPackFindersEvent event) {
        if (event.getPackType() == PackType.CLIENT_RESOURCES) event.addRepositorySource(addPack -> addPack.accept(APDynamicResources.PACK));
    }
}