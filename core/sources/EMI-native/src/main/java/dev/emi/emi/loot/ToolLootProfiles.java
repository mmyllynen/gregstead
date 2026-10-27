package dev.emi.emi.loot;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

public final class ToolLootProfiles {
    private ToolLootProfiles() {}
    private static volatile Map<Identifier,List<ItemStack>> profiles=Map.of();
    private static final java.util.concurrent.CopyOnWriteArrayList<Runnable> listeners=new java.util.concurrent.CopyOnWriteArrayList<>();
    public static void onReceive(Runnable listener) { listeners.add(listener); }
    public static void receive(ToolLootPayload payload) { profiles=payload.profiles();listeners.forEach(Runnable::run); }
    public static void clear() { profiles=Map.of(); }
    public static Optional<List<ItemStack>> get(Identifier id) {
        return Optional.ofNullable(profiles.get(id)).map(stacks->stacks.stream().map(ItemStack::copy).toList());
    }
}
