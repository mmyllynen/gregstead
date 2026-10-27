package com.gregtechceu.gtceu.common.data;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.common.worldgen.modifier.RubberTreeChancePlacement;

import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

public class GTFeatures {

    public static final DeferredRegister<com.mojang.serialization.MapCodec<? extends net.minecraft.world.level.levelgen.placement.PlacementModifier>> PLACEMENT_MODIFIERS =
            DeferredRegister.create(Registries.PLACEMENT_MODIFIER_TYPE, GTCEu.MOD_ID);

    static {
        PLACEMENT_MODIFIERS.register("rubber_tree_chance", () -> RubberTreeChancePlacement.CODEC);
    }

    public static void register(IEventBus modEventBus) {
        GTPlacementModifierTypes.init(modEventBus);
        PLACEMENT_MODIFIERS.register(modEventBus);
        GTFeatureTypes.init(modEventBus);
    }
}
