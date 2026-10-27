package dev.emi.emi.input;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.input.InputQuirks;
import org.lwjgl.sdl.SDLKeyboard;

public class EmiInput {
    public static final int CONTROL_MASK = 1;
    public static final int ALT_MASK = 2;
    public static final int SHIFT_MASK = 4;

    public static boolean isControlDown() {
        return (SDLKeyboard.SDL_GetModState() & InputQuirks.EDIT_SHORTCUT_KEY_MODIFIER) != 0;
    }
    public static boolean isAltDown() {
        return (SDLKeyboard.SDL_GetModState() & InputConstants.MOD_ALT) != 0;
    }
    public static boolean isShiftDown() {
        return (SDLKeyboard.SDL_GetModState() & InputConstants.MOD_SHIFT) != 0;
    }
    public static int maskFromCode(int scancode) { return maskFromCode(scancode, InputQuirks.ON_OSX); }
    public static int maskFromCode(int scancode, boolean mac) {
        if (mac && (scancode == InputConstants.KEY_LGUI || scancode == InputConstants.KEY_RGUI)) return CONTROL_MASK;
        if (scancode == InputConstants.KEY_LCONTROL || scancode == InputConstants.KEY_RCONTROL) return CONTROL_MASK;
        if (scancode == InputConstants.KEY_LALT || scancode == InputConstants.KEY_RALT) return ALT_MASK;
        if (scancode == InputConstants.KEY_LSHIFT || scancode == InputConstants.KEY_RSHIFT) return SHIFT_MASK;
        return 0;
    }
    public static int modifiers(int sdlModifiers, boolean mac) {
        int result = 0;
        if ((sdlModifiers & (mac ? InputConstants.MOD_SUPER : InputConstants.MOD_CONTROL)) != 0) result |= CONTROL_MASK;
        if ((sdlModifiers & InputConstants.MOD_ALT) != 0) result |= ALT_MASK;
        if ((sdlModifiers & InputConstants.MOD_SHIFT) != 0) result |= SHIFT_MASK;
        return result;
    }
    public static void focus(net.minecraft.client.gui.components.EditBox widget, boolean focused) {
        var screen = net.minecraft.client.Minecraft.getInstance().gui.screen();
        focus(screen, widget, focused);
    }
    public static void focus(net.minecraft.client.gui.screens.Screen screen, net.minecraft.client.gui.components.EditBox widget, boolean focused) {
        if (screen != null) {
            var current = screen.getFocused();
            if ((!focused && current == widget) || (focused && current != widget)) screen.setFocused(null);
        }
        widget.setFocused(focused);
    }
    public static int getCurrentModifiers() {
        return modifiers(SDLKeyboard.SDL_GetModState(), InputQuirks.ON_OSX);
    }
}
