package com.firemerald.additionalplacements.block.interfaces.neoforge;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BucketPickup;
import net.minecraft.world.level.block.LiquidBlockContainer;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Optional;

public interface INeoForgeAdditionalPlacementLiquidBlock<T extends Block & BucketPickup & LiquidBlockContainer> extends INeoForgeAdditionalPlacementBlock<T>, BucketPickup, LiquidBlockContainer {
    @Override
    default Optional<SoundEvent> getPickupSound(BlockState blockState) {
        return this.additionalplacements$getOtherBlock().getPickupSound(this.getModelState(blockState));
    }
}
