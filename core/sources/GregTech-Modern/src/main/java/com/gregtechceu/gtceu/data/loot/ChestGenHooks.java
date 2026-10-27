package com.gregtechceu.gtceu.data.loot;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.core.mixins.LootPoolAccessor;
import com.gregtechceu.gtceu.utils.GTMath;
import com.gregtechceu.gtceu.utils.ItemStackHashStrategy;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.core.Holder;
import net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProvider;
import net.minecraft.world.level.storage.loot.providers.number.ints.ConstantValue;
import net.minecraft.world.level.storage.loot.entries.SingleEntryContainerBase;
import com.mojang.serialization.Codec;
import java.util.Optional;
import net.minecraft.world.level.storage.loot.providers.number.ints.UniformGenerator;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.LootTableLoadEvent;

import com.google.common.base.Preconditions;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

public final class ChestGenHooks {

    private static final Map<Identifier, List<GTLootEntryItem>> lootEntryItems = new Object2ObjectOpenHashMap<>();
    private static final Map<Identifier, Holder<ContextIntProvider>> rollValues = new Object2ObjectOpenHashMap<>();

    private static Map<Identifier, List<GTLootEntryItem>> dungeonEntryItems = new Object2ObjectOpenHashMap<>();
    private static Map<Identifier, Holder<ContextIntProvider>> dungeonRollValues = new Object2ObjectOpenHashMap<>();

    private ChestGenHooks() {}

    /** Replace only GT's reload-generated registrations; retain explicit addon registrations. */
    public static void beginDungeonReload() {
        dungeonEntryItems = new Object2ObjectOpenHashMap<>();
        dungeonRollValues = new Object2ObjectOpenHashMap<>();
    }

    public static void addDungeonItem(ResourceKey<LootTable> table, ItemStack stack, int min, int max, int weight) {
        addItemInto(dungeonEntryItems, table, stack, min, max, weight);
    }

    public static void addDungeonRolls(ResourceKey<LootTable> table, int min, int max) {
        dungeonRollValues.put(table.identifier(), Holder.direct(new UniformGenerator(Holder.direct(new ConstantValue(min)), Holder.direct(new ConstantValue(max)))));
    }

    public static void init() {
        NeoForge.EVENT_BUS.register(ChestGenHooks.class);
        RandomWeightLootFunction.init();
        GTLootEntryItem.init();
    }

    @SubscribeEvent
    public static void onWorldLoad(@NotNull LootTableLoadEvent event) {
        LootPool mainPool = event.getTable().getPool("main");
        // Native multi-pool JSON receives pool0/pool1 names, not main.
        if (mainPool == null) mainPool = event.getTable().getPool("pool0");
        if (mainPool == null) return;

        Identifier name = event.getName();
        List<GTLootEntryItem> entryItems = new ArrayList<>(lootEntryItems.getOrDefault(name, List.of()));
        entryItems.addAll(dungeonEntryItems.getOrDefault(name, List.of()));
        if (!entryItems.isEmpty()) {
            for (GTLootEntryItem entry : entryItems) {
                if (ConfigHolder.INSTANCE.dev.debug) {
                    GTCEu.LOGGER.info("adding {} to lootTable {}", entry, name);
                }

                try {
                    List<LootPoolEntryContainer> entries = new ArrayList<>(((LootPoolAccessor) mainPool).getEntries());
                    entries.add(entry);
                    ((LootPoolAccessor) mainPool).setEntries(entries);
                } catch (RuntimeException e) {
                    GTCEu.LOGGER.error("Couldn't add {} to lootTable {}: {}", entry, name, e.getMessage());
                }
            }
        }

        Holder<ContextIntProvider> rolls = mainPool.getRolls();
        var addonRolls = rollValues.get(name);
        var dungeonRolls = dungeonRollValues.get(name);
        if (addonRolls != null) rolls = net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProviders.add(rolls, addonRolls);
        if (dungeonRolls != null) rolls = net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProviders.add(rolls, dungeonRolls);
        if (addonRolls != null || dungeonRolls != null) ((LootPoolAccessor) mainPool).setRolls(rolls);
    }

    public static void addItem(@NotNull ResourceKey<LootTable> lootTable, @NotNull ItemStack stack, int minAmount,
                               int maxAmount, int weight) {
        addItemInto(lootEntryItems, lootTable, stack, minAmount, maxAmount, weight);
    }

    private static void addItemInto(Map<Identifier, List<GTLootEntryItem>> destination, ResourceKey<LootTable> lootTable,
                                   ItemStack stack, int minAmount, int maxAmount, int weight) {
        RandomWeightLootFunction lootFunction = new RandomWeightLootFunction(stack, minAmount, maxAmount);
        String modid = Objects.requireNonNull(BuiltInRegistries.ITEM.getKey(stack.getItem())).getNamespace();
        String entryName = createEntryName(stack, modid, weight, lootFunction);
        GTLootEntryItem itemEntry = new GTLootEntryItem(stack, weight, lootFunction, entryName);
        destination.computeIfAbsent(lootTable.identifier(), $ -> new ArrayList<>()).add(itemEntry);
    }

    public static void addRolls(ResourceKey<LootTable> lootTable, int minAdd, int maxAdd) {
        rollValues.put(lootTable.identifier(), Holder.direct(new UniformGenerator(Holder.direct(new ConstantValue(minAdd)),
                Holder.direct(new ConstantValue(maxAdd)))));
    }

    private static final ItemStackHashStrategy HASH_STRATEGY = ItemStackHashStrategy.comparingAllButCount();

    private static @NotNull String createEntryName(@NotNull ItemStack stack, @NotNull String modid, int weight,
                                                   @NotNull RandomWeightLootFunction function) {
        int hashCode = GTMath.hashInts(HASH_STRATEGY.hashCode(stack), modid.hashCode(), weight, function.getMinAmount(),
                function.getMaxAmount());
        return String.format("#%s:loot_%s", modid, hashCode);
    }

    private static class GTLootEntryItem extends SingleEntryContainerBase {

        private static final MapCodec<GTLootEntryItem> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                ItemStack.CODEC.fieldOf("stack").forGetter(entry -> entry.stack),
                Codec.INT.fieldOf("weight").forGetter(entry -> entry.entryWeight),
                ExtraCodecs.NON_NEGATIVE_INT.fieldOf("min").forGetter(entry -> entry.function.minAmount),
                ExtraCodecs.NON_NEGATIVE_INT.fieldOf("max").forGetter(entry -> entry.function.maxAmount)
        ).apply(instance, GTLootEntryItem::decode));
        private static final MapCodec<GTLootEntryItem> TYPE = GTRegistries.register(
                BuiltInRegistries.LOOT_POOL_ENTRY_TYPE, GTCEu.id("stack_loot_entry"), CODEC);
        private final int entryWeight;
        private final RandomWeightLootFunction function;
        static void init() {}
        private static GTLootEntryItem decode(ItemStack stack,int weight,int min,int max) {
            var function = new RandomWeightLootFunction(stack,min,max);
            var modid = Objects.requireNonNull(BuiltInRegistries.ITEM.getKey(stack.getItem())).getNamespace();
            return new GTLootEntryItem(stack,weight,function,createEntryName(stack,modid,weight,function));
        }
        @Override public MapCodec<GTLootEntryItem> codec() { return TYPE; }

        private final ItemStack stack;
        private final String entryName;

        public GTLootEntryItem(@NotNull ItemStack stack, int weight, LootItemFunction lootFunction,
                               @NotNull String entryName) {
            super(weight, 1, Optional.empty(), Optional.of(Holder.direct(lootFunction)));
            this.entryWeight = weight;
            this.function = (RandomWeightLootFunction) lootFunction;
            this.stack = stack;
            this.entryName = entryName;
        }

        public void createItemStack(Consumer<ItemStack> stackConsumer, @NotNull LootContext lootContext) {
            stackConsumer.accept(this.stack.copy());
        }

        @Override
        public @NotNull String toString() {
            return "GTLootEntryItem{name=" + entryName + ", stack=" + stack.toString() + '}';
        }
    }

    public static class RandomWeightLootFunction extends LootItemConditionalFunction implements LootItemFunction {

        public static final MapCodec<RandomWeightLootFunction> CODEC = RecordCodecBuilder.mapCodec(instance -> instance
                .group(
                        ItemStack.CODEC.fieldOf("stack").forGetter(val -> val.stack),
                        ExtraCodecs.NON_NEGATIVE_INT.fieldOf("min").forGetter(val -> val.minAmount),
                        ExtraCodecs.NON_NEGATIVE_INT.fieldOf("max").forGetter(val -> val.maxAmount))
                .apply(instance, RandomWeightLootFunction::new));

        public static final MapCodec<RandomWeightLootFunction> TYPE = GTRegistries.register(
                BuiltInRegistries.LOOT_FUNCTION_TYPE, GTCEu.id("random_weight"), CODEC);

        private final ItemStack stack;
        @Getter
        private final int minAmount;
        @Getter
        private final int maxAmount;

        public RandomWeightLootFunction(@NotNull ItemStack stack, int minAmount, int maxAmount) {
            super(Optional.empty());
            Preconditions.checkArgument(minAmount <= maxAmount, "minAmount must be <= maxAmount");
            this.stack = stack;
            this.minAmount = minAmount;
            this.maxAmount = maxAmount;
        }

        public static void init() {}

        @Override
        public @NotNull MapCodec<RandomWeightLootFunction> codec() {
            return TYPE;
        }

        @Override
        protected @NotNull ItemStack run(@NotNull ItemStack itemStack, @NotNull LootContext context) {
            return com.lowdragmc.lowdraglib.transfer.item.LootStackRanges.apply(
                    stack, itemStack, minAmount, maxAmount, context.getRandom());
        }
    }
}
