package com.gregtechceu.gtceu.integration.map.ftbchunks;

import dev.ftb.mods.ftbchunks.api.FTBChunksAPI;
import dev.ftb.mods.ftblibrary.config.value.BooleanValue;
import dev.ftb.mods.ftblibrary.config.ConfigUtil;
import dev.ftb.mods.ftblibrary.config.value.Config;

import java.util.HashMap;
import java.util.Map;

public class FTBChunksOptions {

    private static final Map<String, BooleanValue> layerOptions = new HashMap<>();

    private static final Config CONFIG = Config.create("gtceu");
    private static final Config LAYERS = CONFIG.addGroup("journeymap.options.layers");
    private static final BooleanValue ORE_LAYER = LAYERS.addBoolean("ore_veins", false);
    private static final BooleanValue FLUID_LAYER = LAYERS.addBoolean("bedrock_fluids", false);
    private static final BooleanValue HIDE_DEPLETED = LAYERS.addBoolean("hide_depleted", false);

    static {
        layerOptions.put(ORE_LAYER.getKey(), ORE_LAYER);
        layerOptions.put(FLUID_LAYER.getKey(), FLUID_LAYER);
        layerOptions.put(HIDE_DEPLETED.getKey(), HIDE_DEPLETED);

        loadConfig();
    }

    private FTBChunksOptions() {}

    public static boolean showLayer(String name) {
        return layerOptions.get(name).get();
    }

    public static void toggleLayer(String name, boolean active) {
        layerOptions.get(name).set(active);
        saveConfig();
        FTBChunksAPI.clientApi().getWaypointManager()
                .ifPresent(manager -> manager.getAllWaypoints().forEach(waypoint -> {
                    if (waypoint.getName().equals(name)) {
                        waypoint.setHidden(!active);
                    }
                }));
    }

    public static boolean hideDepleted() {
        return HIDE_DEPLETED.get();
    }

    public static void loadConfig() {
        try { NativeFtbLayerConfig.load(CONFIG, ConfigUtil.LOCAL_DIR.resolve("gtceu"), layerOptions); }
        catch (java.io.IOException failure) { throw new java.io.UncheckedIOException("Cannot load GT map layer config", failure); }
    }

    public static void saveConfig() {
        try { NativeFtbLayerConfig.save(CONFIG, ConfigUtil.LOCAL_DIR.resolve("gtceu")); }
        catch (java.io.IOException failure) { throw new java.io.UncheckedIOException("Cannot save GT map layer config", failure); }
    }
}
