package com.tterrag.registrate.util;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import java.util.concurrent.ConcurrentHashMap;

/** Item registration color policies consumed by the native item-layer bridge. */
public final class RegistrateItemColors {
    @FunctionalInterface public interface Color {int getColor(ItemStack stack,int layer);}
    private static final ConcurrentHashMap<Item,Color> COLORS=new ConcurrentHashMap<>();
    private RegistrateItemColors() {}
    public static void register(Item item,Color color){COLORS.put(item,color);}
    public static Color get(Item item){return COLORS.get(item);}
}
