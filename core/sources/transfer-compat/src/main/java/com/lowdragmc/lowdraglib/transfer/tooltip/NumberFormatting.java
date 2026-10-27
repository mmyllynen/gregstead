package com.lowdragmc.lowdraglib.transfer.tooltip;

import java.text.NumberFormat;
import java.util.Locale;

/** Original locale-neutral grouping used by GT numeric tooltips. */
public final class NumberFormatting {
    private static final NumberFormat FORMAT = NumberFormat.getInstance(Locale.ROOT);
    private NumberFormatting() {}
    public static String formatNumbers(int value) { return FORMAT.format(value); }
    public static String formatNumbers(long value) { return FORMAT.format(value); }
    public static String formatNumbers(double value) { return FORMAT.format(value); }
    public static String formatNumbers(Object value) { return FORMAT.format(value); }
}
