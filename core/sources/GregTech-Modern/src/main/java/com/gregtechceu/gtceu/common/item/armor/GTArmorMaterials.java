package com.gregtechceu.gtceu.common.item.armor;

import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.TagKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.EquipmentAssets;
import java.util.Map;

/** Armor materials are component definitions in 26.3, not entries in a removed vanilla registry. */
public final class GTArmorMaterials {
    private static final TagKey<Item> NO_REPAIR = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("gtceu", "armor_no_repair"));
    public static final Holder<ArmorMaterial> GOGGLES = material(0,0,0,0,0,0.0F);
    public static final Holder<ArmorMaterial> JETPACK = material(0,0,0,0,0,0.0F);
    public static final Holder<ArmorMaterial> ARMOR = material(0,0,0,0,0,5.0F);
    public static final Holder<ArmorMaterial> BAD_PPE_EQUIPMENT = material(1,3,2,1,10,0.0F);
    public static final Holder<ArmorMaterial> GOOD_PPE_EQUIPMENT = material(2,6,5,2,10,0.0F);
    private static Holder<ArmorMaterial> material(int head, int chest, int legs, int feet, int enchantment, float toughness) {
        return Holder.direct(new ArmorMaterial(1,
                Map.of(ArmorType.HELMET,head, ArmorType.CHESTPLATE,chest, ArmorType.LEGGINGS,legs, ArmorType.BOOTS,feet, ArmorType.BODY,0),
                enchantment, SoundEvents.ARMOR_EQUIP_GENERIC, toughness, 0.0F, NO_REPAIR,
                ResourceKey.create(EquipmentAssets.ROOT_ID, Identifier.fromNamespaceAndPath("gtceu", "armor"))));
    }
    private GTArmorMaterials() {}
}
