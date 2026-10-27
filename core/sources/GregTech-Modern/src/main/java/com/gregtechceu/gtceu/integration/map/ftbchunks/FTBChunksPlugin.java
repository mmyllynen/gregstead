package com.gregtechceu.gtceu.integration.map.ftbchunks;

import com.gregtechceu.gtceu.integration.map.GroupingMapRenderer;

import dev.ftb.mods.ftbchunks.api.client.event.AddMapIconEvent;
import dev.ftb.mods.ftbchunks.client.map.MapManager;

public class FTBChunksPlugin {

    private FTBChunksPlugin() {}

    public static void addEventListeners() {
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(
                (dev.ftb.mods.ftbchunks.api.neoforge.FTBChunksClientEvent.MapIcon event) -> {
                    if (!event.getEventData().mapType().isWorldIcon()) mapIconEventHandler(event.getEventData());
                });
    }

    private static void mapIconEventHandler(AddMapIconEvent.Data event) {
        if (GroupingMapRenderer.getInstance().doShowLayer("ore_veins")) {
            MapManager.getInstance().ifPresent(manager -> FTBChunksRenderer.oreElements
                    .row(event.dimension())
                    .values()
                    .forEach(event::add));
        }
        if (GroupingMapRenderer.getInstance().doShowLayer("bedrock_fluids")) {
            MapManager.getInstance().ifPresent(manager -> {
                FTBChunksRenderer.fluidElements
                        .row(event.dimension())
                        .values()
                        .forEach(event::add);
            });
        }
    }
}
