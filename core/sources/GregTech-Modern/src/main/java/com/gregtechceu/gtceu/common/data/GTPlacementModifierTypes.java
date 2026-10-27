package com.gregtechceu.gtceu.common.data;

import com.gregtechceu.gtceu.api.data.worldgen.modifier.BiomePlacement;
import com.gregtechceu.gtceu.api.data.worldgen.modifier.DimensionFilter;
import com.gregtechceu.gtceu.api.data.worldgen.modifier.FrequencyModifier;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class GTPlacementModifierTypes {
    public static final DeferredRegister<MapCodec<? extends PlacementModifier>> TYPES =
            DeferredRegister.create(Registries.PLACEMENT_MODIFIER_TYPE, "gtceu");
    static {
        TYPES.register("frequency", () -> FrequencyModifier.CODEC);
        TYPES.register("dimension", () -> DimensionFilter.CODEC);
        TYPES.register("biome_placement", () -> BiomePlacement.CODEC);
    }
    private GTPlacementModifierTypes() {}
    public static void init(IEventBus bus) { TYPES.register(bus); }
}
