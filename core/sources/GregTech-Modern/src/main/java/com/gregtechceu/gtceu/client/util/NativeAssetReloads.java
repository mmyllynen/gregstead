package com.gregtechceu.gtceu.client.util;

import net.minecraft.resources.Identifier;
import net.minecraft.util.Unit;
import net.neoforged.neoforge.client.event.AddClientReloadListenersEvent;
import net.neoforged.neoforge.client.resources.VanillaClientListeners;

/** Runs listener cleanup before model preparation can register callbacks for the new generation. */
public final class NativeAssetReloads {
    public static final Identifier LISTENER=Identifier.parse("gtceu:asset_listener_cleanup");
    private NativeAssetReloads() {}
    public static void register(AddClientReloadListenersEvent event,Runnable cleanup) {
        event.addListener(LISTENER,(state,taskExecutor,barrier,reloadExecutor)-> {
            NativeAtlasListeners.beginReload();cleanup.run();
            return barrier.wait(Unit.INSTANCE).thenAcceptAsync(ignored->{},reloadExecutor);
        });
        event.addDependency(LISTENER,VanillaClientListeners.MODELS);
    }
}
