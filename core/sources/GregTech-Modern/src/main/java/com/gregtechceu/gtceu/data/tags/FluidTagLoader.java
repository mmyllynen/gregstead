package com.gregtechceu.gtceu.data.tags;

import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.data.recipe.CustomTags;

import net.minecraft.world.level.material.Fluid;

import com.tterrag.registrate.providers.RegistrateTagsProvider;

public class FluidTagLoader {

    public static void init(RegistrateTagsProvider.Impl<Fluid> provider) {
        tag(provider, CustomTags.LIGHTER_FLUIDS).add(GTMaterials.Butane.getFluid(), GTMaterials.Propane.getFluid());
        tag(provider, CustomTags.HPCA_COOLANTS).add(GTMaterials.PCBCoolant.getFluid());
    }
    private static NativeTagValues<Fluid> tag(RegistrateTagsProvider.Impl<Fluid> provider, net.minecraft.tags.TagKey<Fluid> key) {
        return new NativeTagValues<>(net.minecraft.core.registries.BuiltInRegistries.FLUID, provider.tag(key));
    }
}
