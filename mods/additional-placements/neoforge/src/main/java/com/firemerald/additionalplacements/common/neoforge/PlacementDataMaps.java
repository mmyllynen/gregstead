package com.firemerald.additionalplacements.common.neoforge;

import com.firemerald.additionalplacements.block.AdditionalPlacementBlock;
import com.firemerald.additionalplacements.block.interfaces.IPlacementBlock;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.BaseMappedRegistry;
import net.neoforged.neoforge.registries.datamaps.DataMapType;
import net.neoforged.neoforge.registries.datamaps.DataMapsUpdatedEvent;
import net.neoforged.neoforge.registries.datamaps.builtin.*;

/** Expand loaded vanilla/modded copper mappings before NeoForge builds its inverse and tool-transform caches. */
@EventBusSubscriber(modid = "additionalplacements")
public final class PlacementDataMaps {
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void updated(DataMapsUpdatedEvent event) {
        event.ifRegistry(Registries.BLOCK, registry -> {
            expand(registry, NeoForgeDataMaps.OXIDIZABLES, Oxidizable::nextOxidationStage,
                (old, block) -> new Oxidizable(block, old.generateBlockTransform()));
            expand(registry, NeoForgeDataMaps.WAXABLES, Waxable::waxed,
                (old, block) -> new Waxable(block, old.generateBlockTransform()));
        });
    }
    private static <T> void expand(Registry<Block> registry, DataMapType<Block,T> type,
                                  Function<T,Block> target, BiFunction<T,Block,T> replace) {
        Map<ResourceKey<Block>,T> expanded = new HashMap<>(registry.getDataMap(type));
        for (var entry : registry.getDataMap(type).entrySet()) {
            Block from = registry.getValueOrThrow(entry.getKey()), to = target.apply(entry.getValue());
            if (!(from instanceof AdditionalPlacementBlock<?>) && from instanceof IPlacementBlock<?> a
                    && to instanceof IPlacementBlock<?> b && a.additionalplacements$hasAdditionalStates() && b.additionalplacements$hasAdditionalStates()) {
                Block variant = a.additionalplacements$getOtherBlock();
                expanded.putIfAbsent(variant.builtInRegistryHolder().key(),replace.apply(entry.getValue(),b.additionalplacements$getOtherBlock()));
            }
        }
        ((BaseMappedRegistry<Block>) registry).getDataMaps().put(type, Map.copyOf(expanded));
    }
}
