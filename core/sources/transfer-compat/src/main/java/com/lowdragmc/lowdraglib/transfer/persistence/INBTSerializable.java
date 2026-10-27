package com.lowdragmc.lowdraglib.transfer.persistence;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.Tag;

/** Registry-aware NBT contract retained for existing LDLib persistence and synchronization accessors. */
public interface INBTSerializable<T extends Tag> {
    T serializeNBT(HolderLookup.Provider registries);
    void deserializeNBT(HolderLookup.Provider registries, T tag);
}
