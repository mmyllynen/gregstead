package com.lowdragmc.lowdraglib.utils.virtuallevel;

import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.neoforge.model.data.ModelData;

/** Render view of the supplied level; client worlds retain their native tint/model-data behavior. */
public record NativeLevelRenderView(Level world) implements BlockAndTintGetter {
    public static BlockAndTintGetter of(Level world) {
        return world instanceof BlockAndTintGetter view ? view : new NativeLevelRenderView(world);
    }
    public CardinalLighting cardinalLighting() { return world.dimensionType().cardinalLightType().get(); }
    public int getBlockTint(BlockPos pos, ColorResolver resolver) { return NativeLevelTint.color(world, pos, resolver); }
    public LevelLightEngine getLightEngine() { return world.getLightEngine(); }
    public BlockEntity getBlockEntity(BlockPos pos) { return world.getBlockEntity(pos); }
    public BlockState getBlockState(BlockPos pos) { return world.getBlockState(pos); }
    public FluidState getFluidState(BlockPos pos) { return world.getFluidState(pos); }
    public int getHeight() { return world.getHeight(); }
    public int getMinY() { return world.getMinY(); }
    public ModelData getModelData(BlockPos pos) { return world.getModelData(pos); }
    public int getBrightness(LightLayer layer, BlockPos pos) { return world.getBrightness(layer, pos); }
    public int getRawBrightness(BlockPos pos, int amount) { return world.getRawBrightness(pos, amount); }
    public boolean canSeeSky(BlockPos pos) { return world.canSeeSky(pos); }
}
