package com.lowdragmc.lowdraglib.transfer.mixin;

import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.crafting.FluidIngredient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = FluidIngredient.class, remap = false)
public interface FluidIngredientCacheAccessor {
    @Accessor("fluids")
    void gttransfercompat$setFluids(List<Holder<Fluid>> fluids);
}
