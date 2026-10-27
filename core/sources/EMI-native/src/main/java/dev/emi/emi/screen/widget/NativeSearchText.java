package dev.emi.emi.screen.widget;

import java.util.List;
import com.mojang.datafixers.util.Pair;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;

/** Formats the visible EditBox substring using absolute search-token style boundaries. */
public final class NativeSearchText {
    private NativeSearchText() {}

    public static FormattedCharSequence format(String visible, int offset, List<Pair<Integer, Style>> spans) {
        MutableComponent result = Component.empty();
        int consumed = 0;
        for (var span : spans) {
            int end = Math.clamp(span.getFirst() - offset, 0, visible.length());
            if (end > consumed) {
                result.append(Component.literal(visible.substring(consumed, end)).setStyle(span.getSecond()));
                consumed = end;
            }
            if (consumed == visible.length()) break;
        }
        if (consumed < visible.length()) result.append(Component.literal(visible.substring(consumed)));
        return result.getVisualOrderText();
    }
}
