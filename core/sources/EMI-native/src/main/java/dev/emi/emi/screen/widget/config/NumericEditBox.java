package dev.emi.emi.screen.widget.config;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringUtil;

/** Integer input validation before the native EditBox changes its value or calls its responder. */
public final class NumericEditBox extends EditBox {
    private int highlight;
    private int limit = 32;

    public NumericEditBox(Font font, int x, int y, int width, int height, Component narration) {
        super(font, x, y, width, height, narration);
    }

    private static boolean valid(String value) {
        return value.matches("^-?[0-9]*$");
    }

    @Override
    public void setValue(String value) {
        if (valid(value)) super.setValue(value);
    }

    @Override
    public void setHighlightPos(int position) {
        super.setHighlightPos(position);
        highlight = Math.clamp(position, 0, getValue().length());
    }

    @Override
    public void setMaxLength(int length) {
        super.setMaxLength(length);
        limit = length;
        highlight = Math.min(highlight, getValue().length());
    }

    @Override
    public void insertText(String input) {
        String value = getValue();
        int start = Math.min(getCursorPosition(), highlight);
        int end = Math.max(getCursorPosition(), highlight);
        int available = limit - value.length() + end - start;
        if (available <= 0) return;
        String text = StringUtil.filterText(input);
        if (text.length() > available) {
            if (Character.isHighSurrogate(text.charAt(available - 1))) available--;
            text = text.substring(0, available);
        }
        if (valid(new StringBuilder(value).replace(start, end, text).toString())) {
            super.insertText(input);
        }
    }
}
