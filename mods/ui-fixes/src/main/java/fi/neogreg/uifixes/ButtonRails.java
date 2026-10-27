package fi.neogreg.uifixes;

/** GUI-pixel geometry, independent of Minecraft and IPN. */
public final class ButtonRails {
    public record Rect(int x, int y, int width, int height) {
        public int right() { return x + width; }
        public int bottom() { return y + height; }
        public boolean intersects(Rect other) {
            return x < other.right() && right() > other.x && y < other.bottom() && bottom() > other.y;
        }
    }
    public record Rails(Rect machine, Rect player) {}

    private ButtonRails() {}

    /** EMI 1.1.24 tests cell corners, so a narrow control needs a full 18px cell margin. */
    public static Rect emiExclusion(Rect control) {
        int margin = 18;
        return new Rect(control.x - margin, control.y - margin,
                control.width + margin * 2, control.height + margin * 2);
    }

    public static Rails place(int viewportWidth, int viewportHeight, Rect panel,
            int obstacleLeft, int obstacleRight, int playerTop,
            int machineCount, int playerCount, int buttonWidth, int buttonHeight) {
        int gap = 2;
        int mh = machineCount == 0 ? 0 : machineCount * (buttonHeight + gap) - gap;
        int ph = playerCount == 0 ? 0 : playerCount * (buttonHeight + gap) - gap;
        int my = clamp(panel.y + 4, 0, Math.max(0, viewportHeight - mh));
        int py = clamp(playerTop, 0, Math.max(0, viewportHeight - ph));
        // Short/custom machine panels need two columns instead of overlapping groups.
        boolean twoColumns = mh > 0 && ph > 0 && my < py + ph + gap && my + mh + gap > py;
        int width = twoColumns ? buttonWidth * 2 + gap : buttonWidth;
        int right = Math.max(panel.right(), obstacleRight) + 4;
        int left = Math.min(panel.x, obstacleLeft) - 4 - width;
        int x = right + width <= viewportWidth ? right : left;
        x = clamp(x, 0, Math.max(0, viewportWidth - width));
        return new Rails(new Rect(x, my, buttonWidth, mh),
                new Rect(x + (twoColumns ? buttonWidth + gap : 0), py, buttonWidth, ph));
    }

    private static int clamp(int value, int min, int max) { return Math.max(min, Math.min(max, value)); }
}
