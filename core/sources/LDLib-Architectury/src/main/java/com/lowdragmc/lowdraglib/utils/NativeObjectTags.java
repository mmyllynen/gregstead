package com.lowdragmc.lowdraglib.utils;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Registry-aware native serialization retaining the original no-ID/entity and ID-only/block contracts. */
public final class NativeObjectTags {
    private static final Logger LOGGER = LoggerFactory.getLogger(NativeObjectTags.class);
    private NativeObjectTags() {}

    public static CompoundTag saveEntity(Entity entity) {
        try (var reporter = new ProblemReporter.ScopedCollector(entity.problemPath(), LOGGER)) {
            var output = TagValueOutput.createWithContext(reporter, entity.registryAccess());
            entity.saveWithoutId(output);
            return output.buildResult();
        }
    }

    public static CompoundTag saveBlockEntity(BlockEntity entity, HolderLookup.Provider registries) {
        try (var reporter = new ProblemReporter.ScopedCollector(entity.problemPath(), LOGGER)) {
            var output = TagValueOutput.createWithContext(reporter, registries);
            entity.saveWithId(output);
            return output.buildResult();
        }
    }

    public static void mergeEntity(Entity entity, CompoundTag overrides) {
        var merged = saveEntity(entity).copy().merge(overrides);
        try (var reporter = new ProblemReporter.ScopedCollector(entity.problemPath(), LOGGER)) {
            entity.load(TagValueInput.create(reporter, entity.registryAccess(), merged));
        }
    }
}
