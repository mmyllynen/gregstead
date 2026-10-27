package com.tterrag.registrate.util;

import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.advancements.predicates.StatePropertiesPredicate;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ComposterBlock;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootPredicates;
import net.minecraft.world.level.storage.loot.predicates.MatchBlock;
import net.minecraft.world.level.storage.loot.providers.number.DispatcherProvider;
import net.minecraft.world.level.storage.loot.providers.number.ints.*;

/** Native context values retaining the pre-component item builder semantics. */
public final class NativeItemIntProviders {
    private NativeItemIntProviders() {}

    public static ContextIntProvider cooking(int ticks, HolderGetter<LootItemCondition> predicates,
            HolderGetter<ContextIntProvider> providers) {
        var divisor = Holder.<ContextIntProvider>direct(new ConditionalValue(
                predicates.getOrThrow(LootPredicates.FAST_FURNACE),
                providers.getOrThrow(ContextIntProviders.COOKING_FAST_BURN_TIME_REDUCTION_FACTOR),
                providers.getOrThrow(ContextIntProviders.COOKING_NORMAL_BURN_TIME_REDUCTION_FACTOR)));
        return ContextIntProviders.div(ContextIntProviders.exactly(ticks), divisor).value();
    }

    public static ContextIntProvider compostable(float chance, HolderGetter<Block> blocks) {
        if (!(chance > 0)) return ContextIntProviders.binomial(1, chance).value();
        var empty = MatchBlock.blockMatches(blocks, Blocks.COMPOSTER,
                StatePropertiesPredicate.Builder.properties().hasProperty(ComposterBlock.LEVEL, 0)).build();
        return new NumberDispatcher(List.of(new DispatcherProvider.Case<>(Holder.direct(empty),
                ContextIntProviders.exactly(1))), ContextIntProviders.binomial(1, chance));
    }
}
