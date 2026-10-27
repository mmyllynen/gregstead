package com.gregtechceu.gtceu.common.item.armor;

import net.minecraft.world.item.equipment.ArmorType;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.ArmorProperty;
import com.gregtechceu.gtceu.client.renderer.item.ArmorItemRenderer;

import com.tterrag.registrate.util.RegistrateItemColors.Color;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import org.jetbrains.annotations.Nullable;

public class GTArmorItem extends Item implements com.lowdragmc.lowdraglib.transfer.name.DynamicDescription, com.lowdragmc.lowdraglib.transfer.name.DynamicItemName {

    private final ArmorType type;
    public final Material material;
    public final ArmorProperty armorProperty;

    public GTArmorItem(ArmorType type, Properties properties, Material material, ArmorProperty armorProperty) {
        super(properties.humanoidArmor(armorProperty.getArmorMaterial().value(), type));
        this.type = type;
        this.material = material;
        this.armorProperty = armorProperty;
        if (GTCEu.isClientSide()) {
            ArmorItemRenderer.create(this, type);
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static Color tintColor(Material material) {
        return (itemStack, index) -> material.getLayerARGB(index);
    }

    @Override
    public String dynamicDescriptionId() {
        String matSpecificKey = String.format("item.%s.%s_%s",
                material.getModid(), material.getName(), type.getName());
        if (Language.getInstance().has(matSpecificKey)) {
            return matSpecificKey;
        }
        return "item.gtceu.armor." + type.getName();
    }

    public Component getDescription() {
        return Component.translatable(getDescriptionId(), material.getLocalizedName());
    }

    @Override
    public Component dynamicItemName(ItemStack stack) {
        return this.getDescription();
    }

    public @Nullable Identifier getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot,
                                                      com.lowdragmc.lowdraglib.transfer.equipment.ArmorLayer layer, boolean innerModel) {
        return armorProperty.getCustomTextureGetter().getCustomTexture(stack, entity, slot, layer);
    }
}
