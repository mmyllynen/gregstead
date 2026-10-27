package com.lowdragmc.lowdraglib.transfer.equipment;

import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.EquipmentAssets;
import java.util.Map;
import java.util.Optional;

/** Native equipment assets for original GT gear, selected from the wearer's own chest equipment. */
public final class ArmorAppearance {
    private static final Map<String,String> FIXED = Map.ofEntries(
        Map.entry("nightvision_goggles","nightvision_goggles"),
        Map.entry("face_mask","bad_hazmat"), Map.entry("rubber_gloves","bad_hazmat"),
        Map.entry("hazmat_chestpiece","hazmat"), Map.entry("hazmat_leggings","hazmat"),
        Map.entry("hazmat_boots","hazmat"), Map.entry("hazmat_headpiece","hazmat"),
        Map.entry("liquid_fuel_jetpack","liquid_fuel_jetpack"), Map.entry("electric_jetpack","jetpack"),
        Map.entry("advanced_electric_jetpack","advanced_jetpack"));
    private ArmorAppearance() {}
    public static Optional<ResourceKey<EquipmentAsset>> asset(Identifier item, Identifier wearerChest) {
        if (!item.getNamespace().equals("gtceu")) return Optional.empty();
        String path=item.getPath(), theme=FIXED.get(path);
        if (path.startsWith("nanomuscle_") || path.equals("advanced_nanomuscle_chestplate")) {
            theme=path.startsWith("advanced_") || isChest(wearerChest,"advanced_nanomuscle_chestplate") ?
                    "advanced_nano_muscle_suite" : "nano_muscule_suite";
        } else if (path.startsWith("quarktech_") || path.equals("advanced_quarktech_chestplate")) {
            theme=path.startsWith("advanced_") || isChest(wearerChest,"advanced_quarktech_chestplate") ?
                    "advanced_quark_tech_suite" : "quark_tech_suite";
        }
        return theme==null ? Optional.empty() : Optional.of(ResourceKey.create(EquipmentAssets.ROOT_ID,
                Identifier.fromNamespaceAndPath("gtceu",theme)));
    }
    public static net.minecraft.world.item.equipment.Equippable withAsset(
            net.minecraft.world.item.equipment.Equippable source, ResourceKey<EquipmentAsset> asset) {
        return new net.minecraft.world.item.equipment.Equippable(source.slot(), source.equipSound(), Optional.of(asset),
                source.cameraOverlay(), source.allowedEntities(), source.dispensable(), source.swappable(),
                source.damageOnHurt(), source.equipOnInteract(), source.canBeSheared(), source.shearingSound());
    }
    private static boolean isChest(Identifier item,String path) {
        return item != null && item.getNamespace().equals("gtceu") && item.getPath().equals(path);
    }
}
