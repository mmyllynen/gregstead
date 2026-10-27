package com.tterrag.registrate.providers.loot;

import com.tterrag.registrate.AbstractRegistrate;
import net.minecraft.core.HolderGetter;
import net.minecraft.data.loot.LootTableSubProvider;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.loot.EntityLootSubProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import javax.annotation.Generated;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class RegistrateEntityLootTables extends EntityLootSubProvider implements RegistrateLootTables {

    private final AbstractRegistrate<?> parent;
    private final Consumer<RegistrateEntityLootTables> callback;

    private final HolderGetter<Item> itemLookup;
    private final HolderGetter<Block> blockLookup;
    private final HolderGetter<EntityType<?>> entityLookup;

    public RegistrateEntityLootTables(LootTableSubProvider.Context p_346214_, AbstractRegistrate<?> parent, Consumer<RegistrateEntityLootTables> callback) {
        super(FeatureFlags.REGISTRY.allFlags(), p_346214_);
        this.parent = parent;
        this.callback = callback;
        itemLookup = output.lookup(Registries.ITEM);
        blockLookup = output.lookup(Registries.BLOCK);
        entityLookup = output.lookup(Registries.ENTITY_TYPE);
    }

    @Override
    public void generate() {
        callback.accept(this);
    }

    @Override
    protected Stream<EntityType<?>> getKnownEntityTypes() {
        return parent.getAll(Registries.ENTITY_TYPE).stream().map(Supplier::get);
    }

    public LootTableSubProvider.Context getRegistries() {
        return this.output;
    }

    public HolderGetter<Item> itemLookup() {
        return itemLookup;
    }

    public HolderGetter<Block> blockLookup() {
        return blockLookup;
    }

    public HolderGetter<EntityType<?>> entityLookup() {
        return entityLookup;
    }

    // @formatter:off
    // GENERATED START - DO NOT EDIT BELOW THIS LINE

    /** Generated override to expose protected method: {@link EntityLootSubProvider#killedByFrog} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateEntityLootTables", date = "Sat, 20 Jun 2026 04:20:42 GMT")
    public LootItemCondition.Builder killedByFrog() { return super.killedByFrog(); }

    /** Generated override to expose protected method: {@link EntityLootSubProvider#add} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateEntityLootTables", date = "Sat, 20 Jun 2026 04:20:42 GMT")
    public void add(EntityType<?> type, LootTable.Builder builder) { super.add(type, builder); }

    /** Generated override to expose protected method: {@link EntityLootSubProvider#add} */
    @Override
    @Generated(value = "com.tterrag.registrate.test.meta.UpdateEntityLootTables", date = "Sat, 20 Jun 2026 04:20:42 GMT")
    public void add(EntityType<?> type, ResourceKey<LootTable> lootTable, LootTable.Builder builder) { super.add(type, lootTable, builder); }

    // GENERATED END
}
