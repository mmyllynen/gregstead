package com.gregtechceu.gtceu.client.fluid;

import com.gregtechceu.gtceu.api.registry.registrate.GTClientFluidTypeExtensions;
import net.minecraft.client.renderer.block.FluidModel;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.neoforge.client.event.RegisterFluidModelsEvent;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.client.fluid.FluidTintSource;
import net.neoforged.neoforge.fluids.FluidStack;

/** Bakes current GT fluid metadata on every native fluid-model reload. */
public final class GTFluidModels {
    private GTFluidModels() {}
    public static void registerMaterials(RegisterFluidModelsEvent event) {
        for (var fluid : BuiltInRegistries.FLUID) {
            if (IClientFluidTypeExtensions.of(fluid) instanceof GTClientFluidTypeExtensions metadata) {
                event.register(material(metadata), fluid);
            }
        }
    }
    public static FluidModel.Unbaked material(GTClientFluidTypeExtensions metadata) {
        var tint = new FluidTintSource() {
            @Override public int color(FluidState state) { return metadata.getTintColor(); }
        };
        return new FluidModel.Unbaked(texture(metadata.getStillTexture()),
                texture(metadata.getFlowingTexture()), null, tint);
    }
    private static Material texture(Identifier id) {
        return new Material(id == null ? Identifier.withDefaultNamespace("missingno") : id);
    }
    public static FluidModel.Unbaked potion(Identifier texture) {
        var tint = new FluidTintSource() {
            @Override public int color(FluidState state) { return -1; }
            @Override public int colorAsStack(FluidStack stack) {
                return stack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY).getColor() | 0xff000000;
            }
        };
        return new FluidModel.Unbaked(new Material(texture),new Material(texture),null,tint);
    }
}
