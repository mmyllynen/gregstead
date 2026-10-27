package fi.neogreg.uifixes;

import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.widget.Bounds;
import net.neoforged.fml.ModList;
import net.p3pp3rf1y.sophisticatedbackpacks.client.gui.BackpackScreen;

/** Optional EMI entrypoint: existing providers remain registered alongside this one. */
@EmiEntrypoint
public final class BackpackEmiPlugin implements EmiPlugin {
    @Override
    public void register(EmiRegistry registry) {
        if (!ModList.get().isLoaded("sophisticatedbackpacks") || !ModList.get().isLoaded("inventoryprofilesnext")) return;
        registry.addExclusionArea(BackpackScreen.class, (screen, consumer) -> {
            for (var area : BackpackLayout.layout(screen)) {
                var reserved = ButtonRails.emiExclusion(new ButtonRails.Rect(
                        area.getX(), area.getY(), area.getWidth(), area.getHeight()));
                consumer.accept(new Bounds(reserved.x(), reserved.y(), reserved.width(), reserved.height()));
            }
        });
    }
}
