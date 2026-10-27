package com.gregtechceu.gtceu.api.data.chemical.material;

import com.gregtechceu.gtceu.api.data.chemical.material.stack.ItemMaterialInfo;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.MaterialEntry;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.MaterialStack;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.common.data.GTMaterialItems;
import com.gregtechceu.gtceu.data.recipe.misc.RecyclingRecipes;
import com.gregtechceu.gtceu.data.recipe.misc.StoneMachineRecipes;
import com.gregtechceu.gtceu.data.recipe.misc.WoodMachineRecipes;
import com.gregtechceu.gtceu.data.tags.TagsHandler;
import com.gregtechceu.gtceu.utils.ItemStackHashStrategy;
import com.gregtechceu.gtceu.utils.memoization.MemoizedBlockSupplier;

import net.minecraft.core.registries.Registries;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.registries.DeferredHolder;

import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenCustomHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Supplier;

public class ItemMaterialData {

    /** Used for custom material data for items that do not fall into the normal "prefix, material" pair */
    public static final Map<Item, ItemMaterialInfo> ITEM_MATERIAL_INFO = new Object2ObjectOpenHashMap<>();
    private static Map<Item, ItemMaterialInfo> registeredMaterialInfo;
    /** Mapping of an item to a "prefix, material" pair */
    public static final List<Pair<Supplier<? extends Item>, MaterialEntry>> ITEM_MATERIAL_ENTRY = new ArrayList<>();
    public static final Map<Item, MaterialEntry> ITEM_MATERIAL_ENTRY_COLLECTED = new Object2ObjectOpenHashMap<>();
    /** Mapping of a tag to a "prefix, material" pair */
    public static final Map<TagKey<Item>, MaterialEntry> TAG_MATERIAL_ENTRY = new Object2ObjectLinkedOpenHashMap<>();
    /** Mapping of a fluid to a material */
    public static final Map<Fluid, Material> FLUID_MATERIAL = new Object2ObjectOpenHashMap<>();
    /** Mapping of all items that represent a "prefix, material" pair */
    public static final Map<MaterialEntry, List<Supplier<? extends Item>>> MATERIAL_ENTRY_ITEM_MAP = new Object2ObjectOpenHashMap<>();
    public static final Map<MaterialEntry, List<Supplier<? extends Block>>> MATERIAL_ENTRY_BLOCK_MAP = new Object2ObjectOpenHashMap<>();
    /** Mapping of stone type blockState to "prefix, material" */
    public static final Map<Supplier<BlockState>, TagPrefix> ORES_INVERSE = new Object2ReferenceOpenHashMap<>();

    public static final Map<ItemStack, List<ItemStack>> UNRESOLVED_ITEM_MATERIAL_INFO = new Object2ObjectOpenCustomHashMap<>(
            ItemStackHashStrategy.comparingAllButCount());

    public static void registerMaterialInfo(ItemLike item, ItemMaterialInfo materialInfo) {
        ITEM_MATERIAL_INFO.put(item.asItem(), materialInfo);
    }

    public static ItemMaterialInfo getMaterialInfo(ItemLike item) {
        return ITEM_MATERIAL_INFO.get(item.asItem());
    }

    public static void clearMaterialInfo(ItemLike item) {
        ITEM_MATERIAL_INFO.remove(item.asItem());
    }

    /**
     * Register Material Entry for an item
     *
     * @param supplier      a supplier to the item
     * @param materialEntry the entry to register
     */
    public static void registerMaterialEntry(@NotNull Supplier<? extends ItemLike> supplier,
                                             @NotNull MaterialEntry materialEntry) {
        registerItemEntry(supplier, materialEntry);
        ITEM_MATERIAL_ENTRY.add(Pair.of(() -> supplier.get().asItem(), materialEntry));
        var blockSupplier = convertToBlock(supplier);
        if (blockSupplier != null) {
            registerBlockEntry(blockSupplier, materialEntry);
        }
    }

    /**
     * @see #registerMaterialEntry(Supplier, MaterialEntry)
     */
    public static void registerMaterialEntries(@NotNull Collection<Supplier<? extends ItemLike>> items,
                                               @NotNull TagPrefix tagPrefix, @NotNull Material material) {
        if (!items.isEmpty()) {
            MaterialEntry entry = new MaterialEntry(tagPrefix, material);
            for (var supplier : items) {
                registerMaterialEntry(supplier, entry);
            }
        }
    }

    /**
     * @see #registerMaterialEntry(Supplier, MaterialEntry)
     */
    public static void registerMaterialEntry(@NotNull Supplier<? extends ItemLike> item,
                                             @NotNull TagPrefix tagPrefix, @NotNull Material material) {
        registerMaterialEntry(item, new MaterialEntry(tagPrefix, material));
    }

    /**
     * @see #registerMaterialEntry(Supplier, MaterialEntry)
     */
    public static void registerMaterialEntry(@NotNull ItemLike item,
                                             @NotNull TagPrefix tagPrefix, @NotNull Material material) {
        registerMaterialEntry(() -> item, new MaterialEntry(tagPrefix, material));
    }

    private static void registerItemEntry(@NotNull Supplier<? extends ItemLike> supplier,
                                          @NotNull MaterialEntry materialEntry) {
        MATERIAL_ENTRY_ITEM_MAP.computeIfAbsent(materialEntry, k -> new ArrayList<>())
                .add(() -> supplier.get().asItem());
        if (TagPrefix.ORES.containsKey(materialEntry.tagPrefix()) &&
                !ORES_INVERSE.containsValue(materialEntry.tagPrefix())) {
            ORES_INVERSE.put(TagPrefix.ORES.get(materialEntry.tagPrefix()).stoneType(), materialEntry.tagPrefix());
        }
        if (!materialEntry.isEmpty()) {
            for (TagKey<Item> tag : materialEntry.tagPrefix().getAllItemTags(materialEntry.material())) {
                TAG_MATERIAL_ENTRY.putIfAbsent(tag, materialEntry);
            }
        }
    }

    private static void registerBlockEntry(@NotNull Supplier<? extends Block> supplier,
                                           @NotNull MaterialEntry materialEntry) {
        MATERIAL_ENTRY_BLOCK_MAP.computeIfAbsent(materialEntry, k -> new ArrayList<>())
                .add(supplier);
    }

    @SuppressWarnings("unchecked")
    public static @Nullable Supplier<? extends Block> convertToBlock(@NotNull Supplier<? extends ItemLike> supplier) {
        if (supplier instanceof DeferredHolder<?, ?> registryObject) {
            var key = registryObject.getKey();
            if (key.isFor(Registries.BLOCK)) {
                return (Supplier<? extends Block>) registryObject;
            }
        } else if (supplier instanceof MemoizedBlockSupplier<?> blockSupplier) {
            return blockSupplier;
        }
        return null;
    }

    public static void reinitializeMaterialData() {
        // Recipe resolution mutates ItemMaterialInfo. Keep the registered defaults
        // separate so repeated reloads cannot accumulate derived component amounts.
        if (registeredMaterialInfo == null) {
            registeredMaterialInfo = new HashMap<>();
            ITEM_MATERIAL_INFO.forEach((item, info) -> registeredMaterialInfo.put(item,
                    new ItemMaterialInfo(new ArrayList<>(info.getMaterials()))));
        }
        ITEM_MATERIAL_INFO.clear();
        registeredMaterialInfo.forEach((item, info) -> ITEM_MATERIAL_INFO.put(item,
                new ItemMaterialInfo(new ArrayList<>(info.getMaterials()))));
        UNRESOLVED_ITEM_MATERIAL_INFO.clear();
        // Clear old data
        MATERIAL_ENTRY_ITEM_MAP.clear();
        MATERIAL_ENTRY_BLOCK_MAP.clear();
        ITEM_MATERIAL_ENTRY.clear();
        FLUID_MATERIAL.clear();

        // Load new data
        TagsHandler.initExtraUnificationEntries();
        for (TagPrefix prefix : GTRegistries.TAG_PREFIXES) {
            prefix.getIgnored().forEach((mat, items) -> registerMaterialEntries(items, prefix, mat));
        }
        GTMaterialItems.toUnify
                .forEach((materialEntry, supplier) -> registerMaterialEntry(supplier, materialEntry));
        WoodMachineRecipes.registerMaterialInfo();
        StoneMachineRecipes.registerMaterialInfo();
    }

    private record DeferredMaterialInfo(ItemStack output, List<ItemStack> inputs) {}

    @ApiStatus.Internal
    public static void resolveItemMaterialInfos(RecipeOutput provider) {
        Map<ItemStack, DeferredMaterialInfo> pending = new Object2ObjectOpenCustomHashMap<>(
                ItemStackHashStrategy.comparingAllButCount());
        UNRESOLVED_ITEM_MATERIAL_INFO.forEach((output, inputs) -> pending.put(output,
                new DeferredMaterialInfo(output.copy(), List.copyOf(inputs))));
        Map<ItemStack, ItemMaterialInfo> resolved = new Object2ObjectOpenCustomHashMap<>(
                ItemStackHashStrategy.comparingAllButCount());
        Map<ItemStack, Boolean> resolving = new Object2ObjectOpenCustomHashMap<>(
                ItemStackHashStrategy.comparingAllButCount());
        Map<Item, ItemMaterialInfo> known = new IdentityHashMap<>(ITEM_MATERIAL_INFO);
        List<DeferredMaterialInfo> ordered = new ArrayList<>(pending.values());
        ordered.sort(Comparator.comparing(info ->
                net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(info.output().getItem()).toString()));
        // Resolve the complete graph before changing the public tables or emitting recipes.
        for (var info : ordered) resolveMaterialInfo(info.output(), pending, known, resolved, resolving);
        for (var info : ordered) {
            var materials = resolved.get(info.output());
            if (materials != null) ITEM_MATERIAL_INFO.put(info.output().getItem(), materials);
        }
        UNRESOLVED_ITEM_MATERIAL_INFO.clear();
        for (var info : ordered) {
            var materials = resolved.get(info.output());
            if (materials != null && !materials.getMaterials().isEmpty()) {
                RecyclingRecipes.registerRecyclingRecipes(provider, info.output().copyWithCount(1),
                        materials.getMaterials(), false, null);
            }
        }
    }

    private static ItemMaterialInfo resolveMaterialInfo(ItemStack output,
                                                        Map<ItemStack, DeferredMaterialInfo> pending,
                                                        Map<Item, ItemMaterialInfo> known,
                                                        Map<ItemStack, ItemMaterialInfo> resolved,
                                                        Map<ItemStack, Boolean> resolving) {
        if (resolved.containsKey(output)) return resolved.get(output);
        var dependency = pending.get(output);
        if (dependency == null) return known.get(output.getItem());
        if (resolving.put(output, true) != null) {
            throw new IllegalStateException("Cyclic recycling material dependency: " + output);
        }
        int outputCount = dependency.output().getCount();
        if (outputCount <= 0) throw new IllegalStateException("Invalid recycling output count: " + outputCount);
        List<MaterialStack> materials = new ArrayList<>();
        var partial = known.get(output.getItem());
        if (partial != null) materials.addAll(partial.getMaterials());
        for (var input : dependency.inputs()) {
            var inputInfo = resolveMaterialInfo(input, pending, known, resolved, resolving);
            if (inputInfo == null) continue;
            for (var material : inputInfo.getMaterials()) {
                long amount = java.math.BigInteger.valueOf(material.amount())
                        .multiply(java.math.BigInteger.valueOf(input.getCount()))
                        .divide(java.math.BigInteger.valueOf(outputCount)).longValueExact();
                materials.add(new MaterialStack(material.material(), amount));
            }
        }
        var info = materials.isEmpty() ? null : new ItemMaterialInfo(materials);
        resolved.put(dependency.output(), info);
        resolving.remove(output);
        return info;
    }
}
