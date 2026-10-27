package com.gregtechceu.gtceu.core.mixins;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.storage.loot.LootTable;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(BlockBehaviour.class)
public interface BlockBehaviourAccessor {

    @org.spongepowered.asm.mixin.Mutable
    @Accessor("drops")
    void setDrops(java.util.Optional<ResourceKey<LootTable>> location);
}
