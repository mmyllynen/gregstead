import fi.neogreg.uifixes.ButtonRails;
import fi.neogreg.uifixes.ButtonRails.Rect;

public class ButtonRailsContract {
    private static void check(boolean condition, String name) {
        if (!condition) throw new AssertionError(name);
    }
    public static void main(String[] args) {
        int scenarios = 0;
        for (int width : new int[] {320, 426, 640, 1280}) {
            for (int height : new int[] {240, 300, 480, 900}) {
                for (int panelHeight : new int[] {166, 180, 220}) {
                    var panel = new Rect((width - 176) / 2, (height - panelHeight) / 2, 176, panelHeight);
                    for (int machine = 0; machine <= 4; machine++) {
                        for (int player = 0; player <= 4; player++) {
                            var rails = ButtonRails.place(width, height, panel, panel.x(), panel.right(),
                                    panel.bottom() - 81, machine, player, 10, 10);
                            for (var rail : new Rect[] {rails.machine(), rails.player()}) {
                                check(rail.x() >= 0 && rail.right() <= width && rail.y() >= 0 && rail.bottom() <= height,
                                        "visible after resize");
                                if (rail.height() > 0) check(!rail.intersects(panel), "outside machine panel");
                            }
                            if (machine > 0 && player > 0) check(!rails.machine().intersects(rails.player()), "separate targets");
                            scenarios++;
                        }
                    }
                }
            }
        }
        var panel = new Rect(50, 20, 176, 166);
        var rails = ButtonRails.place(280, 240, panel, 50, 275, 105, 4, 4, 10, 10);
        check(rails.machine().right() < panel.x(), "left fallback when side tabs occupy the right");
        var shortPanel = new Rect(100, 20, 176, 100);
        rails = ButtonRails.place(500, 240, shortPanel, 100, 276, 40, 4, 4, 10, 10);
        check(!rails.machine().intersects(rails.player()), "short panels use two columns");
        check(rails.machine().x() != rails.player().x(), "columns are distinct");
        rails = ButtonRails.place(500, 240, shortPanel, 100, 276, 120, 4, 0, 10, 10);
        check(rails.player().height() == 0, "missing inventory has no player rail");
        // The old backpack hints left both mods in the same two title rows.
        // Native rectangles must participate in placement, not just IPN-vs-IPN checks.
        for (int viewport : new int[] {320, 426, 640}) {
            for (int panelWidth : new int[] {176, 230}) {
                for (int tabWidth : new int[] {24, 100, 180}) {
                    var backpack = new Rect((viewport - panelWidth) / 2, 12, panelWidth, 276);
                    int inventoryTop = 207;
                    var nativeControls = new Rect[] {
                        new Rect(backpack.right() - 48, 17, 24, 10),
                        new Rect(backpack.right() - 24, inventoryTop - 14, 24, 10),
                        new Rect(backpack.x() - 26, 12, 26, 76),
                        new Rect(backpack.right(), 12, tabWidth, 150)
                    };
                    rails = ButtonRails.place(viewport, 320, backpack, backpack.x() - 26,
                            backpack.right() + tabWidth, inventoryTop, 4, 4, 10, 10);
                    for (var rail : new Rect[] {rails.machine(), rails.player()}) {
                        for (var control : nativeControls) check(!rail.intersects(control), "backpack native/IPN separation");
                        check(!rail.intersects(backpack), "backpack content remains clear");
                    }
                }
            }
        }
        // A 10px control can lie wholly inside EMI's 18px cell without containing
        // any cell corner. Every intersecting cell must hit the padded exclusion.
        var control = new Rect(492, 128, 10, 10);
        var excluded = ButtonRails.emiExclusion(control);
        for (int x = control.x() - 35; x <= control.right() + 35; x++) {
            for (int y = control.y() - 35; y <= control.bottom() + 35; y++) {
                var cell = new Rect(x, y, 18, 18);
                if (!cell.intersects(control)) continue;
                boolean corner = false;
                for (int cx : new int[] {x, x + 17}) for (int cy : new int[] {y, y + 17}) {
                    corner |= cx >= excluded.x() && cx < excluded.right() && cy >= excluded.y() && cy < excluded.bottom();
                }
                check(corner, "EMI corner test excludes every cell overlapping a narrow control");
            }
        }
        System.out.println("rail layout passed: " + scenarios + " viewport/visibility scenarios and edge cases");
    }
}
