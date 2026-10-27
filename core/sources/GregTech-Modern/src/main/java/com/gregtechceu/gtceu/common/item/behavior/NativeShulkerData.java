package com.gregtechceu.gtceu.common.item.behavior;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.world.level.storage.TagValueInput;

/** Restores the saved box state through the actual native block entity reader. */
public final class NativeShulkerData {
    private NativeShulkerData() {}
    public static void restore(ShulkerBoxBlockEntity box, CompoundTag data, HolderLookup.Provider registries) {
        try (var reporter = new ProblemReporter.ScopedCollector(box.problemPath(), com.mojang.logging.LogUtils.getLogger())) {
            box.loadWithComponents(TagValueInput.create(reporter, registries, data));
        }
        box.setChanged();
    }
}
