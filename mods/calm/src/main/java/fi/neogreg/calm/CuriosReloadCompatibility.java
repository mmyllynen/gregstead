package fi.neogreg.calm;

import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import top.theillusivec4.curios.common.data.CuriosSlotResources;

/** Curios 17 beta.3 replaces its server slot resources on reload but only populates them at startup. */
final class CuriosReloadCompatibility {
    private CuriosReloadCompatibility() {}

    static void register() {
        // Run after all reload listeners, before Curios syncs slots or rebuilds equipped inventories.
        // A null player denotes a completed server reload, including when no players are online.
        NeoForge.EVENT_BUS.addListener(EventPriority.HIGHEST, CuriosReloadCompatibility::beforeSync);
    }

    private static void beforeSync(OnDatapackSyncEvent event) {
        if (event.getPlayer() == null) {
            CuriosSlotResources.SERVER.populateData();
        }
    }
}
