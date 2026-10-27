package com.gregtechceu.gtceu.client;

import com.mojang.serialization.MapCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.numeric.RangeSelectItemModelProperty;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import java.util.*;

/** Native range-select property preserving item-specific GregTech value callbacks. */
public record NativeItemProperty(Identifier key) implements RangeSelectItemModelProperty {
    public static final MapCodec<NativeItemProperty> CODEC = Identifier.CODEC.fieldOf("key").xmap(NativeItemProperty::new, NativeItemProperty::key);
    private static final Map<Item, Map<Identifier, Value>> VALUES = new IdentityHashMap<>();
    @FunctionalInterface public interface Value {
        float get(ItemStack stack, ClientLevel level, ItemOwner owner, int seed);
    }
    public static void register(Item item, Identifier key, Value value) {
        VALUES.computeIfAbsent(item, ignored -> new HashMap<>()).put(key, Objects.requireNonNull(value));
    }
    @Override public float get(ItemStack stack, ClientLevel level, ItemOwner owner, int seed) {
        Value value = VALUES.getOrDefault(stack.getItem(), Map.of()).get(key);
        if (value == null) throw new IllegalStateException("Missing GregTech item property " + key + " for " + stack.getItem());
        return value.get(stack, level, owner, seed);
    }
    @Override public MapCodec<? extends RangeSelectItemModelProperty> type() { return CODEC; }
}
