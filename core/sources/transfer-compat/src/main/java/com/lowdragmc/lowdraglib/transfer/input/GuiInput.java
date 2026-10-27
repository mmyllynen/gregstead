package com.lowdragmc.lowdraglib.transfer.input;

/** Maps native SDL buttons to the established widget/server-action numbering. */
public final class GuiInput {
    public static final int LEFT = 0, RIGHT = 1, MIDDLE = 2;
    private GuiInput() {}
    public static int widgetButton(int nativeButton) {
        return switch (nativeButton) {
            case 1 -> LEFT;
            case 3 -> RIGHT;
            case 2 -> MIDDLE;
            default -> nativeButton >= 4 ? nativeButton - 1 : -1;
        };
    }
    public static int nativeButton(int widgetButton) {
        return switch (widgetButton) {
            case LEFT -> 1;
            case RIGHT -> 3;
            case MIDDLE -> 2;
            default -> widgetButton >= 3 ? widgetButton + 1 : -1;
        };
    }
}
