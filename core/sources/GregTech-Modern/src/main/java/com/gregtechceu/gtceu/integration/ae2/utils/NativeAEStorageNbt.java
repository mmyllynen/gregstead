package com.gregtechceu.gtceu.integration.ae2.utils;

import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.storage.*;
import java.util.function.Consumer;
import java.util.function.Function;

/** Keeps existing outer NBT structures while invoking AE2's real native ValueIO API. */
public final class NativeAEStorageNbt {
    private NativeAEStorageNbt() {}
    public static CompoundTag write(HolderLookup.Provider registries, Consumer<ValueOutput> action) {
        try (var reporter = new ProblemReporter.ScopedCollector(com.mojang.logging.LogUtils.getLogger())) {
            var output = TagValueOutput.createWithContext(reporter, registries);
            action.accept(output);
            return output.buildResult();
        }
    }
    public static <T> T read(HolderLookup.Provider registries, CompoundTag tag, Function<ValueInput, T> action) {
        try (var reporter = new ProblemReporter.ScopedCollector(com.mojang.logging.LogUtils.getLogger())) {
            return action.apply(TagValueInput.create(reporter, registries, tag));
        }
    }
    public static CompoundTag writeStack(HolderLookup.Provider registries, GenericStack stack) {
        return write(registries, output -> GenericStack.writeTag(output, stack));
    }
    public static GenericStack readStack(HolderLookup.Provider registries, CompoundTag tag) {
        return read(registries, tag, GenericStack::readTag);
    }
    public static CompoundTag writeKey(HolderLookup.Provider registries, AEKey key) {
        return write(registries, key::toTagGeneric);
    }
    public static AEKey readKey(HolderLookup.Provider registries, CompoundTag tag) {
        return read(registries, tag, AEKey::fromTagGeneric);
    }
}
