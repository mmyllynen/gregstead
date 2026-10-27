package fi.neogreg.uifixes;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.Rect2i;
import org.anti_ad.mc.ipn.api.IPNButton;
import org.anti_ad.mc.ipnext.gui.inject.ContainerScreenEventHandler;
import org.anti_ad.mc.ipnext.gui.inject.SortingButtonCollectionWidget;
import org.anti_ad.mc.ipnext.gui.inject.base.SortButtonWidget;

/** IPN 2.3.8 keeps distinct hint objects for each logical button, including player sorts. */
public final class InventoryProfilesLayout {
    private static final IPNButton[] MACHINE = {
        IPNButton.SORT, IPNButton.SORT_COLUMNS, IPNButton.SORT_ROWS, IPNButton.MOVE_TO_PLAYER
    };
    private static final IPNButton[] PLAYER = {
        IPNButton.SORT_PLAYER, IPNButton.SORT_COLUMNS_PLAYER, IPNButton.SORT_ROWS_PLAYER,
        IPNButton.MOVE_TO_CONTAINER
    };

    private InventoryProfilesLayout() {}

    public static List<Rect2i> layout(AbstractContainerScreen<?> screen, List<Rect2i> existing) {
        var player = Minecraft.getInstance().player;
        if (player == null) return existing;
        var panel = ((ContainerLayoutAccess) screen).neogreg$panelBounds();
        var result = new ArrayList<>(existing);
        for (Object widget : ContainerScreenEventHandler.INSTANCE.getCurrentWidgets()) {
            if (!(widget instanceof SortingButtonCollectionWidget group) || group.getScreen() != screen) continue;
            var machine = buttons(group, MACHINE);
            var inventory = buttons(group, PLAYER);
            // Use vanilla's actual player inventory slots, excluding the hotbar.
            int playerTop = screen.getMenu().slots.stream()
                    .filter(slot -> slot.container == player.getInventory() && slot.isActive()
                            && slot.getContainerSlot() >= 9 && slot.getContainerSlot() < 36)
                    .mapToInt(slot -> panel.y() + slot.y).min()
                    .orElse(panel.bottom());
            int left = panel.x();
            int right = panel.right();
            for (var area : existing) {
                left = Math.min(left, area.getX());
                right = Math.max(right, area.getX() + area.getWidth());
            }
            int width = 10, height = 10;
            for (var button : machine) { width = Math.max(width, button.getWidth()); height = Math.max(height, button.getHeight()); }
            for (var button : inventory) { width = Math.max(width, button.getWidth()); height = Math.max(height, button.getHeight()); }
            var rails = ButtonRails.place(screen.width, screen.height,
                    panel,
                    left, right, playerTop, machine.size(), inventory.size(), width, height);
            place(machine, rails.machine(), height, result);
            place(inventory, rails.player(), height, result);
        }
        return result;
    }

    private static List<SortButtonWidget> buttons(SortingButtonCollectionWidget group, IPNButton[] kinds) {
        var result = new ArrayList<SortButtonWidget>();
        for (var kind : kinds) {
            var hint = group.getHints().hintFor(kind);
            for (Object candidate : group.getHintableList()) {
                if (candidate instanceof SortButtonWidget button && button.getHints() == hint && button.getVisible()) {
                    result.add(button);
                    break;
                }
            }
        }
        return result;
    }

    private static void place(List<SortButtonWidget> buttons, ButtonRails.Rect rail,
            int height, List<Rect2i> areas) {
        for (int i = 0; i < buttons.size(); i++) {
            var button = buttons.get(i);
            // Screen coordinates update the same widget bounds used by IPN's hit testing.
            button.setScreenX(rail.x());
            button.setScreenY(rail.y() + i * (height + 2));
        }
        if (!buttons.isEmpty()) areas.add(new Rect2i(rail.x() - 1, rail.y() - 1, rail.width() + 2, rail.height() + 2));
    }
}
