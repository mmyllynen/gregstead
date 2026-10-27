package dev.latvian.mods.kubejs.util;

import net.minecraft.advancements.predicates.StatePropertiesPredicate;
import net.minecraft.core.HolderGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.MatchBlock;

/** Native block identity and property predicate for the original generated loot conditions. */
public final class NativeLootConditions {
    private NativeLootConditions() {}
    public static LootItemCondition.Builder blockState(HolderGetter<Block> registry, Block block,
                                                       StatePropertiesPredicate.Builder properties) {
        return MatchBlock.blockMatches(registry, block, properties);
    }
}
