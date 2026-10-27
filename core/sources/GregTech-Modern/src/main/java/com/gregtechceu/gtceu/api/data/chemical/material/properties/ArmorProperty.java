package com.gregtechceu.gtceu.api.data.chemical.material.properties;

import net.minecraft.world.item.equipment.ArmorType;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.registry.registrate.GTRegistrate;
import com.gregtechceu.gtceu.utils.memoization.GTMemoizer;

import net.minecraft.util.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import com.google.common.base.Preconditions;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

import java.util.*;
import java.util.function.Supplier;

// TODO document
public class ArmorProperty implements IMaterialProperty {

    private static final EnumMap<ArmorType, Integer> HEALTH_FUNCTION_FOR_TYPE = Util.make(
            new EnumMap<>(ArmorType.class), (map) -> {
                map.put(ArmorType.BOOTS, 13);
                map.put(ArmorType.LEGGINGS, 15);
                map.put(ArmorType.CHESTPLATE, 16);
                map.put(ArmorType.HELMET, 11);
            });

    @Getter
    @Setter
    @Range(from = 0, to = Integer.MAX_VALUE)
    private int durabilityMultiplier;
    @Setter
    private Map<ArmorType, Integer> protectionValues;
    @Setter
    private int enchantability;
    @Setter
    private Holder<SoundEvent> sound;
    @Setter
    private float toughness;
    @Setter
    private float knockbackResistance;

    @Getter
    @Setter
    private Supplier<@Nullable Ingredient> repairIngredient;
    private boolean noRepair;

    @Setter
    private Identifier textureName = GTCEu.id("metal");
    @Getter
    @Setter
    private CustomTextureGetter customTextureGetter = (stack, entity, slot, overlay) -> null;

    @Getter
    @Setter
    private boolean dyeable;

    @Getter
    @Setter
    private List<com.lowdragmc.lowdraglib.transfer.equipment.ArmorLayer> layers = null;

    @Getter
    private Holder<ArmorMaterial> armorMaterial;
    private Material material;

    public ArmorProperty(int durabilityMultiplier, int[] protectionValues) {
        this.durabilityMultiplier = durabilityMultiplier;
        this.protectionValues = Util.make(new EnumMap<>(ArmorType.class), map -> {
            for (int i = 0; i < 4; i++) {
                map.put(ArmorType.values()[i], protectionValues[i]);
            }
        });
        this.sound = SoundEvents.ARMOR_EQUIP_IRON;
        this.toughness = 0;
        this.knockbackResistance = 0;
    }

    @Override
    public void verifyProperty(MaterialProperties properties) {
        if (this.material == null) {
            this.material = properties.getMaterial();
        }

        if (this.repairIngredient == null && noRepair) {
            this.repairIngredient = () -> null;
        } else if (this.repairIngredient == null) {
            this.repairIngredient = GTMemoizer
                    .memoize(() -> com.lowdragmc.lowdraglib.transfer.recipe.IngredientStacks.of(ChemicalHelper.getTag(TagPrefix.plate, material)));
        }

        if (this.layers == null) {
            this.layers = List.of(new com.lowdragmc.lowdraglib.transfer.equipment.ArmorLayer(this.textureName, "", this.dyeable));
        }
        if (this.armorMaterial == null) {
            this.armorMaterial = Holder.direct(new ArmorMaterial(durabilityMultiplier,
                    protectionValues, enchantability, sound, toughness, knockbackResistance,
                    ChemicalHelper.getTag(TagPrefix.plate, material),
                    net.minecraft.resources.ResourceKey.create(net.minecraft.world.item.equipment.EquipmentAssets.ROOT_ID,
                            this.textureName)));

        }
    }

    @SuppressWarnings("unused") // API, need to treat all of these as used
    public static class Builder {

        private final ArmorProperty armorProperty;

        /**
         * Create Armor for this Material.
         *
         * @param durabilityMultiplier The durability value of this Armor. Leather is 5, Iron is 15, Diamond is 33.
         * @param protectionValues     The protection values of each armor piece in the set.<br>
         *                             Ordered as Helmet, Chestplate, Leggings, Boots.
         * @throws IllegalArgumentException If the protectionValues array parameter does not have exactly 4 entries.
         *
         * @see net.minecraft.world.item.ArmorMaterials
         */
        public static ArmorProperty.Builder of(int durabilityMultiplier, int[] protectionValues) {
            Preconditions.checkArgument(protectionValues != null && protectionValues.length == 4,
                    "protectionValues must have 4 entries!");
            return new ArmorProperty.Builder(durabilityMultiplier, protectionValues);
        }

        private Builder(int durabilityMultiplier, int[] protectionValues) {
            armorProperty = new ArmorProperty(durabilityMultiplier, protectionValues);
        }

        /**
         * Set armors made from this Material as unbreakable, bypassing all durability.
         */
        public ArmorProperty.Builder unbreakable() {
            armorProperty.durabilityMultiplier = 0;
            return this;
        }

        /**
         * Set the base enchantability of a tool made from this Material. Iron is 14, Diamond is 10, Stone is 5.
         */
        public ArmorProperty.Builder enchantability(int enchantability) {
            armorProperty.enchantability = enchantability;
            return this;
        }

        /**
         * Set the protection value for a specific piece of armor made from this Material.
         */
        public ArmorProperty.Builder protectionValue(ArmorType type, int value) {
            armorProperty.protectionValues.put(type, value);
            return this;
        }

        /**
         * Set the protection values for all pieces of armor made from this Material.
         *
         * @throws IllegalArgumentException If the provided map does not have a value for all 4 armor pieces.
         */
        public ArmorProperty.Builder protectionValues(Map<ArmorType, Integer> protectionValues) {
            Preconditions.checkArgument(protectionValues != null && protectionValues.size() == 4,
                    "protectionValues must have 4 entries!");
            armorProperty.protectionValues = protectionValues;
            return this;
        }

        public ArmorProperty.Builder repairIngredient(@Nullable Supplier<Ingredient> repairIngredient) {
            if (repairIngredient == null) {
                armorProperty.repairIngredient = () -> null;
                armorProperty.noRepair = true;
            } else {
                armorProperty.repairIngredient = GTMemoizer.memoize(repairIngredient);
            }
            return this;
        }

        /**
         * Set the toughness granted for wearing armors made of this Material.
         * Diamond is 2, Netherite is 3, other armors are 0.
         *
         * @see net.minecraft.world.item.ArmorMaterials
         * @see <a href="https://minecraft.wiki/w/Armor#Armor_toughness">Armor Toughness - Minecraft Wiki</a>
         */
        public ArmorProperty.Builder toughness(float toughness) {
            armorProperty.toughness = toughness;
            return this;
        }

        /**
         * Set the knockback resistance granted for wearing armor made of this Material.<br>
         * Netherite is 0.1 (10%), other armors are 0.
         *
         * @see net.minecraft.world.item.ArmorMaterials
         */
        public ArmorProperty.Builder knockbackResistance(float knockbackResistance) {
            armorProperty.knockbackResistance = knockbackResistance;
            return this;
        }

        /**
         * Set whether armor made of this Material can be dyed, similar to Leather armor.
         */
        public ArmorProperty.Builder dyeable(boolean dyeable) {
            armorProperty.dyeable = dyeable;
            return this;
        }

        public ArmorProperty.Builder layers(com.lowdragmc.lowdraglib.transfer.equipment.ArmorLayer... layers) {
            return layers(Arrays.asList(layers));
        }

        public ArmorProperty.Builder layers(List<com.lowdragmc.lowdraglib.transfer.equipment.ArmorLayer> layers) {
            armorProperty.layers = layers;
            return this;
        }

        /**
         * Set a custom worn armor texture for armor made of this Material.
         */
        public ArmorProperty.Builder customTexture(ArmorProperty.CustomTextureGetter textureGetter) {
            armorProperty.customTextureGetter = textureGetter;
            return this;
        }

        public ArmorProperty build() {
            return armorProperty;
        }
    }

    @FunctionalInterface
    public interface CustomTextureGetter {

        Identifier getCustomTexture(ItemStack stack, Entity entity, EquipmentSlot slot,
                                          com.lowdragmc.lowdraglib.transfer.equipment.ArmorLayer layer);
    }
}
