package com.gregtechceu.gtceu.api.item;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.DustProperty;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.item.armor.ArmorComponentItem;
import com.gregtechceu.gtceu.client.renderer.item.TagPrefixItemRenderer;
import com.gregtechceu.gtceu.common.data.GTDamageTypes;

import com.tterrag.registrate.util.RegistrateItemColors.Color;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import org.jetbrains.annotations.Nullable;

import java.util.List;

public class TagPrefixItem extends Item implements com.lowdragmc.lowdraglib.transfer.name.DynamicDescription, com.lowdragmc.lowdraglib.transfer.name.DynamicItemName,
        com.lowdragmc.lowdraglib.transfer.lifecycle.LegacyInventoryTick {

    public final TagPrefix tagPrefix;
    public final Material material;

    public TagPrefixItem(Properties properties, TagPrefix tagPrefix, Material material) {
        super(properties);
        this.tagPrefix = tagPrefix;
        this.material = material;
        if (GTCEu.isClientSide()) {
            TagPrefixItemRenderer.create(this, tagPrefix.materialIconType(), material.getMaterialIconSet());
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static Color tintColor(Material material) {
        return (itemStack, index) -> material.getLayerARGB(index);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context,
                                net.minecraft.world.item.component.TooltipDisplay display,
                                java.util.function.Consumer<Component> output, TooltipFlag flag) {
        com.lowdragmc.lowdraglib.transfer.tooltip.LegacyTooltips.emit(output,
                lines -> appendHoverText(stack, context, lines, flag));
    }

    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents,
                                TooltipFlag isAdvanced) {
        super.appendHoverText(stack, context, net.minecraft.world.item.component.TooltipDisplay.DEFAULT, tooltipComponents::add, isAdvanced);
        if (this.tagPrefix.tooltip() != null) {
            this.tagPrefix.tooltip().accept(material, tooltipComponents);
        }
    }

    @Override
    public String dynamicDescriptionId() {
        return tagPrefix.getUnlocalizedName(material);
    }

    public Component getDescription() {
        return tagPrefix.getLocalizedName(material);
    }

    @Override
    public Component dynamicItemName(ItemStack stack) {
        return getDescription();
    }

    @Override
    public void inventoryTick(ItemStack stack, net.minecraft.server.level.ServerLevel level, Entity owner,
                              @org.jetbrains.annotations.Nullable net.minecraft.world.entity.EquipmentSlot slot) {
        com.lowdragmc.lowdraglib.transfer.lifecycle.ItemTickBridge.dispatch(stack, level, owner, slot);
    }

    @Override
    public void legacyInventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        if (entity instanceof LivingEntity livingEntity) {
            if (livingEntity.tickCount % 20 == 0) {
                if (tagPrefix != TagPrefix.ingotHot || !material.hasProperty(PropertyKey.BLAST))
                    return;

                float heatDamage = ((material.getBlastTemperature() - 1750) / 1000.0F) + 2;
                ItemStack armor = livingEntity.getItemBySlot(EquipmentSlot.CHEST);
                if (!armor.isEmpty() && armor.getItem() instanceof ArmorComponentItem armorItem) {
                    heatDamage *= armorItem.getArmorLogic().getHeatResistance();
                }
                if (heatDamage > 0.0) {
                    livingEntity.hurt(level.damageSources().source(GTDamageTypes.HEAT), heatDamage);
                } else if (heatDamage < 0.0) {
                    livingEntity.hurt(livingEntity.damageSources().freeze(), -heatDamage);
                }
            }
        }
    }

    public int getItemBurnTime() {
        return materialBurnTime(tagPrefix, material);
    }

    public static int materialBurnTime(TagPrefix prefix, Material material) {
        DustProperty property = material.isNull() ? null : material.getProperty(PropertyKey.DUST);
        return property == null ? 0 : (int) (property.getBurnTime() * prefix.getMaterialAmount(material) / GTValues.M);
    }

    public static <T extends Item, P> com.tterrag.registrate.builders.ItemBuilder<T, P> materialFuel(
            com.tterrag.registrate.builders.ItemBuilder<T, P> builder, TagPrefix prefix, Material material) {
        int ticks = materialBurnTime(prefix, material);
        return ticks > 0 ? builder.burnTime(ticks) : builder;
    }

    // TODO BEACON PAYMENT
}
