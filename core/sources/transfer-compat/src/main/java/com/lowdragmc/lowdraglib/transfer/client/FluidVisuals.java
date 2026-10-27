package com.lowdragmc.lowdraglib.transfer.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.FluidModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;

/** Queries the current baked fluid model, including stack-sensitive native tint. */
public final class FluidVisuals {
    private FluidVisuals() {}
    public enum Texture { STILL, FLOWING, OVERLAY }
    public static FluidModel model(net.minecraft.world.level.material.Fluid fluid) {
        return Minecraft.getInstance().getModelManager().getFluidStateModelSet().get(fluid.defaultFluidState());
    }
    public static FluidModel model(FluidStack stack) { return model(stack.getFluid()); }
    public static int color(net.minecraft.world.level.material.Fluid fluid) {
        var tint = model(fluid).fluidTintSource();
        return tint == null ? -1 : tint.color(fluid.defaultFluidState());
    }
    public static TextureAtlasSprite sprite(net.minecraft.world.level.material.Fluid fluid, Texture texture) {
        var model = model(fluid);
        return switch (texture) {
            case STILL -> model.stillMaterial().sprite();
            case FLOWING -> model.flowingMaterial().sprite();
            case OVERLAY -> model.overlayMaterial() == null ? model.stillMaterial().sprite() : model.overlayMaterial().sprite();
        };
    }
    public static net.minecraft.resources.Identifier stillTexture(net.minecraft.world.level.material.Fluid fluid) {
        return sprite(fluid, Texture.STILL).contents().name();
    }
    public static int colorInWorld(net.minecraft.world.level.material.FluidState fluid,
            net.minecraft.world.level.block.state.BlockState block,
            net.minecraft.client.renderer.block.BlockAndTintGetter level, net.minecraft.core.BlockPos pos) {
        var tint = Minecraft.getInstance().getModelManager().getFluidStateModelSet().get(fluid).fluidTintSource();
        return tint == null ? -1 : tint.colorInWorld(fluid, block, level, pos);
    }
    public static int color(FluidStack stack) {
        var tint = model(stack).fluidTintSource();
        return tint == null ? -1 : tint.colorAsStack(stack);
    }
    public static @Nullable TextureAtlasSprite stillSprite(FluidStack stack) {
        return stack.isEmpty() ? null : model(stack).stillMaterial().sprite();
    }
}
