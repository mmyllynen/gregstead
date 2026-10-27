package dev.latvian.mods.kubejs.util;

import net.minecraft.core.*;
import net.minecraft.core.component.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.*;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.server.packs.resources.ResourceManager;
import java.util.*;
import java.util.function.Supplier;

/** Rules belong to the packs in one reload; no process-wide item mutation ledger. */
public final class NativeItemComponentRules {
    public interface Source { NativeItemComponentRules componentRules(); }
    private record Rule(TagKey<Item> tag, Set<ResourceKey<Item>> items, DataComponentPatch patch) {
        boolean matches(HolderLookup.Provider context, Item item) {
            var registry = context.lookupOrThrow(Registries.ITEM);
            var key = net.minecraft.core.registries.BuiltInRegistries.ITEM.getResourceKey(item).orElseThrow();
            return tag == null ? items.contains(key) : registry.get(tag).map(values -> values.contains(registry.getOrThrow(key))).orElse(false);
        }
    }
    private static final ScopedValue<List<Rule>> CURRENT = ScopedValue.newInstance();
    private final String stage;
    private final List<Rule> rules = new ArrayList<>();
    private int sequence;
    public NativeItemComponentRules(String stage) { this.stage = stage; }
    public ResourceKey<net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProvider> nextProvider() {
        return ResourceKey.create(Registries.CONTEXT_INT_PROVIDER, Identifier.fromNamespaceAndPath("kubejs", "item_rules/" + stage + "/" + sequence++));
    }
    public void add(HolderSet<Item> selector, DataComponentPatch patch) {
        var tag = selector.unwrapKey().orElse(null);
        var keys = tag == null ? selector.stream().map(holder -> holder.unwrapKey().orElseThrow()).collect(java.util.stream.Collectors.toUnmodifiableSet()) : Set.<ResourceKey<Item>>of();
        rules.add(new Rule(tag, keys, patch));
    }
    public void reset() { rules.clear(); sequence = 0; }
    public static <T> Supplier<T> scope(ResourceManager resources, Supplier<T> work) {
        var snapshot = resources.listPacks().filter(pack -> pack instanceof Source)
                .flatMap(pack -> ((Source) pack).componentRules().rules.stream()).toList();
        return () -> ScopedValue.where(CURRENT, snapshot).call(work::get);
    }
    public static void register(net.neoforged.neoforge.event.ModifyDefaultComponentsEvent event) {
        event.modifyMatching((item, components) -> CURRENT.isBound(), NativeItemComponentRules::apply);
    }
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static void apply(DataComponentMap.Builder builder, HolderLookup.Provider context, Item item) {
        if (!CURRENT.isBound()) return;
        for (var rule : CURRENT.get()) if (rule.matches(context, item)) {
            for (var type : rule.patch.keySet()) builder.set((DataComponentType) type, rule.patch.getPatch(type));
        }
    }
}
