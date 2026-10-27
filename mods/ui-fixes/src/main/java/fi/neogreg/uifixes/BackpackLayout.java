package fi.neogreg.uifixes;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.renderer.Rect2i;
import net.neoforged.fml.ModList;
import net.p3pp3rf1y.sophisticatedbackpacks.client.gui.BackpackScreen;
import net.p3pp3rf1y.sophisticatedcore.client.gui.controls.WidgetBase;

/** Keep Sophisticated's native controls in place; move only IPN's existing widgets. */
public final class BackpackLayout {
    private BackpackLayout() {}

    public static List<Rect2i> obstacles(BackpackScreen screen) {
        var areas = new ArrayList<Rect2i>();
        // Includes both native transfer buttons and both native sorting buttons.
        // Conservative bounds also protect hidden controls while their tabs animate.
        for (var child : screen.children()) {
            if (child instanceof WidgetBase widget) {
                add(areas, widget.getX(), widget.getY(), widget.getWidth(), widget.getHeight());
            } else if (child instanceof AbstractWidget widget && widget.visible) {
                add(areas, widget.getX(), widget.getY(), widget.getWidth(), widget.getHeight());
            }
        }
        screen.getSortButtonsRectangle().ifPresent(areas::add);
        screen.getUpgradeSlotsRectangle().ifPresent(areas::add);
        var tabs = screen.getUpgradeSettingsControl();
        if (tabs != null) areas.addAll(tabs.getTabRectangles());
        return areas;
    }

    public static List<Rect2i> layout(BackpackScreen screen) {
        var areas = obstacles(screen);
        return ModList.get().isLoaded("inventoryprofilesnext")
                ? InventoryProfilesLayout.layout(screen, areas) : areas;
    }

    private static void add(List<Rect2i> areas, int x, int y, int width, int height) {
        if (width > 0 && height > 0) areas.add(new Rect2i(x, y, width, height));
    }
}
