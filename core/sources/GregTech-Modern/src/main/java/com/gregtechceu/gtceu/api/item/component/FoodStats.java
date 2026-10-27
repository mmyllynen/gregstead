package com.gregtechceu.gtceu.api.item.component;

import com.gregtechceu.gtceu.utils.GTUtil;
import com.lowdragmc.lowdraglib.transfer.food.FoodDefinition;
import net.minecraft.core.component.DataComponentMap;

import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.level.Level;

import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Supplier;

/**
 * Simple {@link IEdibleItem} implementation.
 */
public class FoodStats implements IEdibleItem, IInteractionItem, IAddInformation {

    protected final FoodDefinition definition;

    protected final boolean isDrink;
    @Nullable
    protected final Supplier<ItemStack> containerItem;
    @Nullable
    private Supplier<net.minecraft.world.item.ItemStackTemplate> nativeContainerItem;

    public FoodStats(FoodDefinition properties, boolean isDrink, Item container) {
        this(properties, isDrink, (Supplier<ItemStack>) null);
        this.nativeContainerItem = () -> new net.minecraft.world.item.ItemStackTemplate(container);
    }

    public FoodStats(FoodDefinition properties, boolean isDrink, boolean hasPotionEffects,
                     @Nullable Supplier<ItemStack> containerItem) {
        this.definition = properties;
        this.isDrink = isDrink;
        this.containerItem = containerItem;
    }

    public FoodStats(FoodDefinition properties, boolean isDrink, @Nullable Supplier<ItemStack> containerItem) {
        this.definition = properties;
        this.isDrink = isDrink;
        this.containerItem = containerItem;
    }

    public FoodStats(FoodDefinition properties) {
        this.definition = properties;
        this.isDrink = false;
        this.containerItem = null;
    }

    @Override
    public FoodProperties getFoodProperties(ItemStack stack, @Nullable LivingEntity entity) {
        return definition.food();
    }

    @Override
    public SoundEvent getEatingSound() {
        return isDrink ? getDrinkingSound() : IEdibleItem.super.getEatingSound();
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack stack) {
        return isDrink ? ItemUseAnimation.DRINK : ItemUseAnimation.EAT;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltipComponents,
                                TooltipFlag isAdvanced) {
        GTUtil.addPotionTooltip(definition.effects(), tooltipComponents);
    }

    public FoodStats(FoodProperties properties, boolean drink, boolean hasPotionEffects, Supplier<ItemStack> remainder) {
        this(FoodDefinition.plain(properties), drink, hasPotionEffects, remainder);
    }
    public FoodStats(FoodProperties properties) { this(FoodDefinition.plain(properties)); }
    public FoodStats(FoodProperties properties, boolean drink, Supplier<ItemStack> remainder) {
        this(FoodDefinition.plain(properties), drink, remainder);
    }

    public void installComponents(DataComponentMap.Builder components) {
        definition.install(components, isDrink, containerItem);
        if (nativeContainerItem != null) {
            components.set(net.minecraft.core.component.DataComponents.USE_REMAINDER,
                    new net.minecraft.world.item.component.UseRemainder(nativeContainerItem.get()));
        }
        var consumable=components.build().get(net.minecraft.core.component.DataComponents.CONSUMABLE);
        components.set(net.minecraft.core.component.DataComponents.CONSUMABLE,
                com.lowdragmc.lowdraglib.transfer.food.ConsumableSounds.withSound(consumable,getEatingSound()));
    }

    @Override
    public ItemStack finishUsingItem(ItemStack food, Level level, LivingEntity livingEntity) {
        // ComponentItem performs the native consumption once, then dispatches post-use hooks.
        // UseRemainder is applied by ItemStack, including inventory overflow and creative mode.
        return food;
    }
}
