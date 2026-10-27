package com.gregtechceu.gtceu.common.data;

import com.gregtechceu.gtceu.common.worldgen.feature.StoneBlobFeature;
import com.gregtechceu.gtceu.common.worldgen.feature.FluidSproutFeature;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class GTFeatureTypes {
    public static final DeferredRegister<MapCodec<? extends Feature>> TYPES =
            DeferredRegister.create(Registries.FEATURE_TYPE, "gtceu");
    static {
        TYPES.register("stone_blob", () -> StoneBlobFeature.CODEC);
        TYPES.register("fluid_sprout", () -> FluidSproutFeature.CODEC);
    }
    private GTFeatureTypes() {}
    public static void init(IEventBus bus) { TYPES.register(bus); }
}
