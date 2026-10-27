package com.gregtechceu.gtceu.common.data;

import com.gregtechceu.gtceu.common.entity.*;

import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.vehicle.boat.Boat;
import net.minecraft.world.entity.vehicle.boat.ChestBoat;

import com.tterrag.registrate.util.entry.EntityEntry;

import static com.gregtechceu.gtceu.common.registry.GTRegistration.REGISTRATE;

public class GTEntityTypes {

    public static final EntityEntry<DynamiteEntity> DYNAMITE = REGISTRATE
            .<DynamiteEntity>entity("dynamite", DynamiteEntity::new, MobCategory.MISC)
            .lang("Dynamite")
            .properties(builder -> builder.sized(0.25F, 0.25F).fireImmune().clientTrackingRange(4).updateInterval(10))
            .tag(EntityTypeTags.IMPACT_PROJECTILES)
            .register();

    public static final EntityEntry<PowderbarrelEntity> POWDERBARREL = REGISTRATE
            .<PowderbarrelEntity>entity("powderbarrel", PowderbarrelEntity::new, MobCategory.MISC)
            .lang("Powderbarrel")
            .properties(builder -> builder.sized(0.98F, 0.98F).fireImmune().clientTrackingRange(10).updateInterval(10))
            .register();

    public static final EntityEntry<IndustrialTNTEntity> INDUSTRIAL_TNT = REGISTRATE
            .<IndustrialTNTEntity>entity("industrial_tnt", IndustrialTNTEntity::new, MobCategory.MISC)
            .lang("Industrial TNT")
            .properties(builder -> builder.sized(0.98F, 0.98F).fireImmune().clientTrackingRange(10).updateInterval(10))
            .register();

    public static final EntityEntry<Boat> RUBBER_BOAT = REGISTRATE
            .<Boat>entity("rubber_boat", NativeBoatFactories.boat(() -> GTItems.RUBBER_BOAT.get()), MobCategory.MISC)
            .lang("Rubber Boat")
            .properties(builder -> builder.noLootTable().sized(1.375F, 0.5625F).eyeHeight(0.5625F).clientTrackingRange(10))
            .tag(EntityTypeTags.BOAT)
            .register();

    public static final EntityEntry<Boat> TREATED_WOOD_BOAT = REGISTRATE
            .<Boat>entity("treated_wood_boat", NativeBoatFactories.boat(() -> GTItems.TREATED_WOOD_BOAT.get()), MobCategory.MISC)
            .lang("Treated Wood Boat")
            .properties(builder -> builder.noLootTable().sized(1.375F, 0.5625F).eyeHeight(0.5625F).clientTrackingRange(10))
            .tag(EntityTypeTags.BOAT)
            .register();

    public static final EntityEntry<ChestBoat> RUBBER_CHEST_BOAT = REGISTRATE
            .<ChestBoat>entity("rubber_chest_boat", NativeBoatFactories.chestBoat(() -> GTItems.RUBBER_CHEST_BOAT.get()), MobCategory.MISC)
            .lang("Rubber Boat with Chest")
            .properties(builder -> builder.noLootTable().sized(1.375F, 0.5625F).eyeHeight(0.5625F).clientTrackingRange(10))
            .tag(EntityTypeTags.BOAT)
            .register();

    public static final EntityEntry<ChestBoat> TREATED_WOOD_CHEST_BOAT = REGISTRATE
            .<ChestBoat>entity("treated_wood_chest_boat", NativeBoatFactories.chestBoat(() -> GTItems.TREATED_WOOD_CHEST_BOAT.get()), MobCategory.MISC)
            .lang("Treated Wood Boat with Chest")
            .properties(builder -> builder.noLootTable().sized(1.375F, 0.5625F).eyeHeight(0.5625F).clientTrackingRange(10))
            .tag(EntityTypeTags.BOAT)
            .register();

    public static void init() {}
}
