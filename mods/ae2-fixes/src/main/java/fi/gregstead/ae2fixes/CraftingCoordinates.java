package fi.gregstead.ae2fixes;

/** Converts a full-grid position to the recipe's trimmed input, rejecting cells outside it. */
public final class CraftingCoordinates {
    private CraftingCoordinates() {}
    public static int remainderIndex(int x, int y, int left, int top, int width, int height) {
        int localX = x - left, localY = y - top;
        return localX >= 0 && localX < width && localY >= 0 && localY < height
            ? localX + localY * width : -1;
    }
}
