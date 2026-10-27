package com.gregtechceu.gtceu.client.model.machine;

import com.gregtechceu.gtceu.client.model.machine.multipart.NativeMachineParts;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import java.util.*;
import java.util.function.BiFunction;

/** Native base geometry selection; state is read from immutable meshing snapshots. */
public final class NativeMachineBaseModel<S> implements BlockStateModel {
    private final Map<S, BlockStateModel> variants;
    private final NativeMachineParts<S> multipart;
    private final S defaultState;
    private final BlockStateModel fallback;
    private final BiFunction<BlockAndTintGetter, BlockPos, S> stateLookup;
    public NativeMachineBaseModel(Map<S, BlockStateModel> variants, NativeMachineParts<S> multipart,
                                  S defaultState, BlockStateModel fallback,
                                  BiFunction<BlockAndTintGetter, BlockPos, S> stateLookup) {
        this.variants = Collections.unmodifiableMap(new IdentityHashMap<>(variants));
        this.multipart = multipart;
        this.defaultState = Objects.requireNonNull(defaultState);
        this.fallback = Objects.requireNonNull(fallback);
        this.stateLookup = Objects.requireNonNull(stateLookup);
    }
    private S state(BlockAndTintGetter level, BlockPos pos) {
        S selected = stateLookup.apply(level, pos);
        return selected == null ? defaultState : selected;
    }
    @Override public void collectParts(BlockAndTintGetter level, BlockPos pos, BlockState state,
                                       RandomSource random, List<BlockStateModelPart> output) {
        S selected = state(level, pos);
        if (multipart != null) multipart.collect(selected, level, pos, state, random, output);
        BlockStateModel variant = variants.get(selected);
        if (variant != null) variant.collectParts(level, pos, state, random, output);
        else if (multipart == null) fallback.collectParts(level, pos, state, random, output);
    }
    @Override public void collectParts(RandomSource random, List<BlockStateModelPart> output) {
        if (multipart != null) multipart.collect(defaultState, random, output);
        BlockStateModel variant = variants.get(defaultState);
        if (variant != null) variant.collectParts(random, output);
        else if (multipart == null) fallback.collectParts(random, output);
    }
    @Override public Material.Baked particleMaterial() { return multipart != null ? multipart.particle(defaultState) : variants.getOrDefault(defaultState, fallback).particleMaterial(); }
    @Override public Material.Baked particleMaterial(BlockAndTintGetter level, BlockPos pos, BlockState state) {
        S selected = state(level, pos);
        if (multipart != null) return multipart.particle(selected, level, pos, state);
        return variants.getOrDefault(selected, fallback).particleMaterial(level, pos, state);
    }
    @Override public int materialFlags() {
        int result = fallback.materialFlags() | (multipart == null ? 0 : multipart.materialFlags());
        for (BlockStateModel model : variants.values()) result |= model.materialFlags();
        return result;
    }
    @Override public int materialFlags(BlockAndTintGetter level, BlockPos pos, BlockState state) {
        S selected = state(level, pos);
        int result = multipart == null ? 0 : multipart.materialFlags(selected, level, pos, state);
        BlockStateModel variant = variants.get(selected);
        if (variant != null) result |= variant.materialFlags(level, pos, state);
        else if (multipart == null) result |= fallback.materialFlags(level, pos, state);
        return result;
    }
}
