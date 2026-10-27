package dev.emi.emi.recipe;

import java.util.List;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.DyeColor;
import net.minecraft.core.component.DataComponents;

public final class NativeFireworkColors {
    private NativeFireworkColors() {}
    public static List<Item> dyes() { return java.util.Arrays.stream(DyeColor.values()).map(Items.DYE::pick).toList(); }
    public static int color(Item dye) { return java.util.Objects.requireNonNull(dye.components().get(DataComponents.DYE)).getFireworkColor(); }
}
