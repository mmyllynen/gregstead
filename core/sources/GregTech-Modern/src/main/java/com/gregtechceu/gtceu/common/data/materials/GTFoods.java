package com.gregtechceu.gtceu.common.data.materials;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import com.lowdragmc.lowdraglib.transfer.food.FoodDefinition;

public class GTFoods {

    public final static FoodDefinition CHOCOLATE = new FoodDefinition.Builder()
            .effect(() -> new MobEffectInstance(MobEffects.SPEED, 200, 1), 0.1F)
            .alwaysEdible().nutrition(4).saturationModifier(0.3F).build();

    public final static FoodDefinition DRINK = new FoodDefinition.Builder()
            .effect(() -> new MobEffectInstance(MobEffects.HASTE, 800, 1), 0.9F)
            .alwaysEdible().nutrition(4).saturationModifier(0.3F).build();

    public static final FoodDefinition ANTIDOTE = new FoodDefinition.Builder()
            .alwaysEdible().fast().build();

    public static void init() {}
}
