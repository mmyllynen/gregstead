package com.gregtechceu.gtceu.client.model.machine.multipart;

import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import java.util.*;
import java.util.function.Predicate;

/** Native machine multipart selection without retaining live block entities in mesh data. */
public final class NativeMachineParts<S> {
    public record Selector<S>(Predicate<S> condition, BlockStateModel model) {
        public Selector { Objects.requireNonNull(condition); Objects.requireNonNull(model); }
    }
    public static final class Selection<S> {
        private final List<Predicate<S>> predicates;
        private final Map<S, BitSet> cache = Collections.synchronizedMap(new IdentityHashMap<>());
        public Selection(List<Predicate<S>> predicates) { this.predicates = List.copyOf(predicates); }
        public BitSet selected(S state) {
            synchronized (cache) {
                var result = cache.computeIfAbsent(state, value -> {
                    var bits = new BitSet();
                    for (int i = 0; i < predicates.size(); i++) if (predicates.get(i).test(value)) bits.set(i);
                    return bits;
                });
                return (BitSet) result.clone();
            }
        }
    }
    private final List<Selector<S>> selectors;
    private final Selection<S> selections;
    public NativeMachineParts(List<Selector<S>> selectors) {
        if (selectors.isEmpty()) throw new IllegalArgumentException("Machine multipart requires a selector");
        this.selectors = List.copyOf(selectors);
        this.selections = new Selection<>(selectors.stream().map(Selector::condition).toList());
    }
    public BitSet selected(S state) { return selections.selected(state); }
    public void collect(S machineState, BlockAndTintGetter level, BlockPos pos, BlockState blockState,
                        RandomSource random, List<BlockStateModelPart> output) {
        BitSet selected = selected(machineState);
        long seed = random.nextLong();
        for (int i = selected.nextSetBit(0); i >= 0; i = selected.nextSetBit(i + 1))
            selectors.get(i).model().collectParts(level, pos, blockState, RandomSource.create(seed), output);
    }
    public void collect(S machineState, RandomSource random, List<BlockStateModelPart> output) {
        BitSet selected = selected(machineState);
        long seed = random.nextLong();
        for (int i = selected.nextSetBit(0); i >= 0; i = selected.nextSetBit(i + 1))
            selectors.get(i).model().collectParts(RandomSource.create(seed), output);
    }
    public Material.Baked particle(S machineState) {
        int first = selected(machineState).nextSetBit(0);
        return selectors.get(first < 0 ? 0 : first).model().particleMaterial();
    }
    public Material.Baked particle(S machineState, BlockAndTintGetter level, BlockPos pos, BlockState blockState) {
        int first = selected(machineState).nextSetBit(0);
        return selectors.get(first < 0 ? 0 : first).model().particleMaterial(level, pos, blockState);
    }
    public int materialFlags() {
        int result = 0;
        for (Selector<S> selector : selectors) result |= selector.model().materialFlags();
        return result;
    }
    public int materialFlags(S machineState, BlockAndTintGetter level, BlockPos pos, BlockState blockState) {
        BitSet selected = selected(machineState); int flags = 0;
        for (int i = selected.nextSetBit(0); i >= 0; i = selected.nextSetBit(i + 1))
            flags |= selectors.get(i).model().materialFlags(level, pos, blockState);
        return flags;
    }
}
