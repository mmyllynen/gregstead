package com.gregtechceu.gtceu.common.entity;

import java.util.function.Supplier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.boat.Boat;
import net.minecraft.world.entity.vehicle.boat.ChestBoat;
import net.minecraft.world.item.Item;

/** Lazy item suppliers avoid item/entity registry initialization cycles. */
public final class NativeBoatFactories {
    private NativeBoatFactories() {}
    public static EntityType.EntityFactory<Boat> boat(Supplier<Item> item) {
        return (type, level) -> new Boat(type, level, item);
    }
    public static EntityType.EntityFactory<ChestBoat> chestBoat(Supplier<Item> item) {
        return (type, level) -> new ChestBoat(type, level, item);
    }
}
