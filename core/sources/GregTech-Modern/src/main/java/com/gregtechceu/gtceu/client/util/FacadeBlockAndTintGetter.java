package com.gregtechceu.gtceu.client.util;

import com.lowdragmc.lowdraglib.transfer.annotation.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.world.level.ColorResolver;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.FluidState;

import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class FacadeBlockAndTintGetter implements BlockAndTintGetter {

    public final BlockAndTintGetter parent;
    public final BlockPos pos;
    public final BlockState blockState;
    public final BlockEntity blockEntity;

    public FacadeBlockAndTintGetter(BlockAndTintGetter parent,
                                    BlockPos pos, BlockState blockState, @Nullable BlockEntity blockEntity) {
        this.parent = parent;
        this.pos = pos;
        this.blockState = blockState;
        this.blockEntity = blockEntity;
    }

    @Override
    public net.minecraft.world.level.CardinalLighting cardinalLighting() {
        return parent.cardinalLighting();
    }

    public float getShade(Direction direction, boolean shade) {
        return shade ? cardinalLighting().byFace(direction) : cardinalLighting().up();
    }

    @Override
    public LevelLightEngine getLightEngine() {
        return parent.getLightEngine();
    }

    @Override
    public int getBlockTint(BlockPos blockPos, ColorResolver colorResolver) {
        return parent.getBlockTint(blockPos, colorResolver);
    }

    @Override
    public @Nullable BlockEntity getBlockEntity(BlockPos pos) {
        return pos.equals(this.pos) ? blockEntity : parent.getBlockEntity(pos);
    }

    @Override
    public BlockState getBlockState(BlockPos pos) {
        return pos.equals(this.pos) ? blockState : parent.getBlockState(pos);
    }

    @Override
    public FluidState getFluidState(BlockPos pos) {
        return pos.equals(this.pos) ? blockState.getFluidState() : parent.getFluidState(pos);
    }

    @Override
    public int getHeight() {
        return parent.getHeight();
    }

    @Override
    public int getMinY() {
        return parent.getMinY();
    }
}
