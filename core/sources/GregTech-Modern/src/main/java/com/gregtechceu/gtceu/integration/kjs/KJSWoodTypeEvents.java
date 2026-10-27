package com.gregtechceu.gtceu.integration.kjs;

import com.gregtechceu.gtceu.data.recipe.WoodTypeEntry;
import com.gregtechceu.gtceu.integration.kjs.events.RegisterWoodsEventJS;

import java.util.ArrayList;
import java.util.List;

/** Loaded only when KubeJS is present, so vanilla wood recipes do not link its event hierarchy. */
public final class KJSWoodTypeEvents {
    private KJSWoodTypeEvents() {}

    public static List<WoodTypeEntry> register() {
        var event = new RegisterWoodsEventJS();
        GTCEuStartupEvents.REGISTER_WOODS.post(event);
        return new ArrayList<>(event.woods);
    }
}
