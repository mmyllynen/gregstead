package com.lowdragmc.lowdraglib.transfer.equipment;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
/** NeoForge's empty prototype attribute list selects the item's dynamic attribute callback. */
public final class ComponentArmorProperties {
    private ComponentArmorProperties() {}
    public static Item.Properties configure(Item.Properties properties,ArmorMaterial material,ArmorType type) {
        properties.attributes(ItemAttributeModifiers.EMPTY).durability(Integer.MAX_VALUE)
                .component(net.minecraft.core.component.DataComponents.EQUIPPABLE,
                        net.minecraft.world.item.equipment.Equippable.builder(type.getSlot())
                                .setEquipSound(material.equipSound()).setAsset(material.assetId()).build())
                .repairable(material.repairIngredient());
        if (material.enchantmentValue() > 0) properties.enchantable(material.enchantmentValue());
        else properties.component(net.minecraft.core.component.DataComponents.ENCHANTABLE, null);
        return properties;
    }
}
