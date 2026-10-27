package com.lowdragmc.lowdraglib.transfer.tooltip;
import net.minecraft.network.chat.Component;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
public final class LegacyTooltips {
    private LegacyTooltips() {}
    public static void emit(Consumer<Component> output,Consumer<List<Component>> legacy) {
        var lines=new ArrayList<Component>();legacy.accept(lines);lines.forEach(output);
    }
}
