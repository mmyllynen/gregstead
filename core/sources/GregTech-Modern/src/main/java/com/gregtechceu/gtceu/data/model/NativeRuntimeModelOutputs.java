package com.gregtechceu.gtceu.data.model;

import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import net.minecraft.client.data.models.ItemModelOutput;
import net.minecraft.client.data.models.blockstates.BlockModelDefinitionGenerator;
import net.minecraft.client.data.models.model.ModelInstance;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelDispatcher;
import net.minecraft.client.renderer.item.ClientItem;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import java.util.*;
import java.util.function.BiConsumer;

/** Collects native datagen outputs for a dynamic resource-pack generation cycle. */
public final class NativeRuntimeModelOutputs implements ItemModelOutput {
    private final Map<Identifier, ModelInstance> models = new LinkedHashMap<>();
    private final Map<Identifier, BlockStateModelDispatcher> states = new LinkedHashMap<>();
    private final Map<Identifier, ClientItem> items = new LinkedHashMap<>();
    private final Map<Identifier, Identifier> copies = new LinkedHashMap<>();
    public void model(Identifier id, ModelInstance instance) { models.put(id, instance); }
    public void state(BlockModelDefinitionGenerator generator) {
        states.put(BuiltInRegistries.BLOCK.getKey(generator.block()), generator.create());
    }
    @Override public void accept(Item item, ItemModel.Unbaked model, ClientItem.Properties properties) {
        register(item, new ClientItem(model, properties));
    }
    @Override public void register(Item item, ClientItem clientItem) {
        register(BuiltInRegistries.ITEM.getKey(item), clientItem);
    }
    @Override public void register(Identifier id, ClientItem clientItem) { items.put(id, clientItem); }
    @Override public void copy(Item donor, Item acceptor) {
        copies.put(BuiltInRegistries.ITEM.getKey(acceptor), BuiltInRegistries.ITEM.getKey(donor));
    }
    private ClientItem resolve(Identifier id, Set<Identifier> visiting) {
        ClientItem direct = items.get(id);
        if (direct != null) return direct;
        if (!visiting.add(id)) throw new IllegalStateException("Cyclic client item copy: " + id);
        Identifier donor = copies.get(id);
        if (donor == null) throw new IllegalStateException("Missing client item copy donor: " + id);
        return resolve(donor, visiting);
    }
    public void flush(BiConsumer<Identifier, JsonElement> output) {
        // Serialize everything before publishing so failed codecs/copies retain the pending cycle.
        Map<Identifier, JsonElement> files = new LinkedHashMap<>();
        models.forEach((id, model) -> files.put(id.withPrefix("models/").withSuffix(".json"), model.get()));
        states.forEach((id, state) -> files.put(id.withPrefix("blockstates/").withSuffix(".json"),
                BlockStateModelDispatcher.CODEC.encodeStart(JsonOps.INSTANCE, state).getOrThrow()));
        Map<Identifier, ClientItem> allItems = new LinkedHashMap<>(items);
        copies.keySet().forEach(id -> allItems.putIfAbsent(id, resolve(id, new HashSet<>())));
        allItems.forEach((id, item) -> files.put(id.withPrefix("items/").withSuffix(".json"),
                ClientItem.CODEC.encodeStart(JsonOps.INSTANCE, item).getOrThrow()));
        files.forEach(output);
        models.clear(); states.clear(); items.clear(); copies.clear();
    }
}
