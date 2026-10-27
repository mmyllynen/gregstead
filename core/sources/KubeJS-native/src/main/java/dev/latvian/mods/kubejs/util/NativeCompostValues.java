package dev.latvian.mods.kubejs.util;

import net.minecraft.core.*;
import net.minecraft.advancements.predicates.StatePropertiesPredicate;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.storage.loot.predicates.MatchBlock;
import net.minecraft.world.level.storage.loot.providers.number.DispatcherProvider;
import net.minecraft.world.level.storage.loot.providers.number.ints.*;
import java.util.List;

/** Exact script chance; positive chance retains the original guaranteed first layer. */
public final class NativeCompostValues {
    private NativeCompostValues() {}
    public static ContextIntProvider provider(float chance, HolderGetter<Block> blocks) {
        if (!(chance > 0)) return ContextIntProviders.binomial(1, chance).value();
        var empty = MatchBlock.blockMatches(blocks, Blocks.COMPOSTER,
                StatePropertiesPredicate.Builder.properties().hasProperty(ComposterBlock.LEVEL, 0)).build();
        return new NumberDispatcher(List.of(new DispatcherProvider.Case<>(Holder.direct(empty),
                ContextIntProviders.exactly(1))), ContextIntProviders.binomial(1, chance));
    }
}
