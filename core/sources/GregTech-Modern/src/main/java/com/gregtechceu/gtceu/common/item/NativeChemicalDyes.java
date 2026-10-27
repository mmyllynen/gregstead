package com.gregtechceu.gtceu.common.item;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.*;
/** Chemical dyes retain the native sheep/sign dye behavior through the DYE component. */
public final class NativeChemicalDyes {
    private NativeChemicalDyes() {}
    public static DyeItem create(DyeColor color,Item.Properties properties) {
        return new DyeItem(properties.component(DataComponents.DYE,color));
    }
}
