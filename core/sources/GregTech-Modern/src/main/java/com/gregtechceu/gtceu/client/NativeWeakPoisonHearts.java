package com.gregtechceu.gtceu.client;
import net.minecraft.client.gui.Hud;
import net.neoforged.neoforge.client.event.PlayerHeartTypeEvent;
/** Weak poison uses the native poison heart sprite while preserving vanilla types otherwise. */
public final class NativeWeakPoisonHearts {
    private NativeWeakPoisonHearts() {}
    public static void apply(PlayerHeartTypeEvent event,boolean poisoned) {
        if(poisoned)event.setType(Hud.HeartType.POISIONED);
    }
}
