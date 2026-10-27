package com.gregtechceu.gtceu.common.capability;

import com.lowdragmc.lowdraglib.transfer.persistence.LegacySavedDataTypes;

import com.gregtechceu.gtceu.api.placeholder.Placeholder;

import com.lowdragmc.lowdraglib.transfer.annotation.MethodsReturnNonnullByDefault;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class PlaceholderSavedData extends SavedData {

    private final ServerLevel level;
    private final CompoundTag tag;

    public static PlaceholderSavedData getOrCreate(ServerLevel level) {
        return level.getDataStorage()
                .computeIfAbsent(
                        LegacySavedDataTypes.type(
                        "gtceu_placeholder_data", level, () -> new PlaceholderSavedData(level),
                        (tag, provider) -> new PlaceholderSavedData(level, tag, provider),
                        (data, tag, provider) -> data.save(tag, provider)));
    }

    public PlaceholderSavedData(ServerLevel level) {
        this(level, new CompoundTag(), (HolderLookup.Provider) null);
    }

    public PlaceholderSavedData(ServerLevel level, CompoundTag tag, @Nullable HolderLookup.Provider provider) {
        this.level = level;
        this.tag = tag.getCompoundOrEmpty("data");
    }

    public CompoundTag getPlaceholderData(Placeholder placeholder) {
        if (!tag.contains(placeholder.getName()))
            tag.put(placeholder.getName(), new CompoundTag());
        return tag.getCompoundOrEmpty(placeholder.getName());
    }

    public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
        tag.put("data", this.tag);
        return tag;
    }
}
