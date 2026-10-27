package com.lowdragmc.lowdraglib.transfer.fluid;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.fluids.FluidStack;

/** Keeps the original default client-extension color on a dedicated server. */
public final class FluidColors {
    private FluidColors() {}
    public static int color(net.minecraft.world.level.material.Fluid fluid) {
        return FMLEnvironment.getDist() == Dist.CLIENT
                ? com.lowdragmc.lowdraglib.transfer.client.FluidVisuals.color(fluid) : -1;
    }
    public static int color(FluidStack stack) {
        return FMLEnvironment.getDist() == Dist.CLIENT
                ? com.lowdragmc.lowdraglib.transfer.client.FluidVisuals.color(stack) : -1;
    }
}
