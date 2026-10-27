package com.gregtechceu.gtceu.common.fluid.potion;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.alchemy.PotionContents;
import net.neoforged.neoforge.fluids.FluidStack;

public final class NativePotionFluidNames {
    private NativePotionFluidNames() {}
    public static String description(FluidStack stack, String base) {
        var contents = stack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
        return base + ".effect." + contents.potion().map(holder -> holder.value().name()).orElse("empty");
    }
}
