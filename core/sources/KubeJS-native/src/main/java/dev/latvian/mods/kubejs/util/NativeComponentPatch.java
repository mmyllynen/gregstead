package dev.latvian.mods.kubejs.util;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;
import java.util.*;
/** Retains explicit removed values while traversing the native patch keys. */
public final class NativeComponentPatch {
    private NativeComponentPatch() {}
    public static List<Map.Entry<DataComponentType<?>, Optional<Object>>> entries(DataComponentPatch patch) {
        var entries = new ArrayList<Map.Entry<DataComponentType<?>, Optional<Object>>>();
        for (var type : patch.keySet()) entries.add(Map.entry(type, Optional.ofNullable(patch.getPatch(type))));
        return List.copyOf(entries);
    }
}
