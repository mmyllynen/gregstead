package com.lowdragmc.lowdraglib.transfer.food;

import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.component.Consumables;
import net.minecraft.world.item.component.UseRemainder;
import net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

/** Food nutrition plus native consumable effects, retaining lazy effect suppliers and legacy durations. */
public record FoodDefinition(FoodProperties food, List<Effect> effects, float seconds) {
    public FoodDefinition { effects = List.copyOf(effects); }
    public record Effect(Supplier<MobEffectInstance> supplier, float probability) {
        public Effect { Objects.requireNonNull(supplier); if (probability < 0 || probability > 1) throw new IllegalArgumentException("Invalid effect probability"); }
        public MobEffectInstance effect() { return new MobEffectInstance(supplier.get()); }
    }
    public static FoodDefinition plain(FoodProperties food) { return new FoodDefinition(food, List.of(), 1.6F); }
    public Consumable consumable(boolean drink) {
        Consumable.Builder builder = (drink ? Consumables.defaultDrink() : Consumables.defaultFood()).consumeSeconds(seconds);
        for (Effect effect : effects) builder.onConsume(new ApplyStatusEffectsConsumeEffect(effect.effect(), effect.probability()));
        return builder.build();
    }
    public Item.Properties properties(Item.Properties properties, boolean drink) {
        return properties.food(food, consumable(drink));
    }
    public void install(DataComponentMap.Builder components, boolean drink, Supplier<ItemStack> remainder) {
        components.set(DataComponents.FOOD, food);
        components.set(DataComponents.CONSUMABLE, consumable(drink));
        if (remainder != null) {
            ItemStack stack = remainder.get();
            if (!stack.isEmpty()) components.set(DataComponents.USE_REMAINDER,
                    new UseRemainder(ItemStackTemplate.fromNonEmptyStack(stack)));
        }
    }
    public static final class Builder {
        private final FoodProperties.Builder food = new FoodProperties.Builder();
        private final List<Effect> effects = new ArrayList<>();
        private float seconds = 1.6F;
        public Builder nutrition(int amount) { food.nutrition(amount); return this; }
        public Builder saturationModifier(float amount) { food.saturationModifier(amount); return this; }
        public Builder alwaysEdible() { food.alwaysEdible(); return this; }
        public Builder fast() { seconds = 0.8F; return this; }
        public Builder effect(Supplier<MobEffectInstance> effect, float chance) { effects.add(new Effect(effect, chance)); return this; }
        public FoodDefinition build() { return new FoodDefinition(food.build(), effects, seconds); }
    }
}
