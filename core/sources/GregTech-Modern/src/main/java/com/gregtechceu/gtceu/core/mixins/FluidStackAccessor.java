package com.gregtechceu.gtceu.core.mixins;

import net.minecraft.core.Holder;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** The public typeHolder hides zero-amount resources; AE config needs their retained raw fluid identity. */
@Mixin(value = FluidStack.class, remap = false)
public interface FluidStackAccessor {
    @Accessor("fluid")
    Holder<Fluid> getRawFluidHolder();
}
