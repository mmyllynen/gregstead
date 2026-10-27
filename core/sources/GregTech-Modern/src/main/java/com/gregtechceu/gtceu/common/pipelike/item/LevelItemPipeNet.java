package com.gregtechceu.gtceu.common.pipelike.item;

import com.lowdragmc.lowdraglib.transfer.persistence.LegacySavedDataTypes;

import com.gregtechceu.gtceu.api.data.chemical.material.properties.ItemPipeProperties;
import com.gregtechceu.gtceu.api.pipenet.LevelPipeNet;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;

public class LevelItemPipeNet extends LevelPipeNet<ItemPipeProperties, ItemPipeNet> {

    public static LevelItemPipeNet getOrCreate(ServerLevel serverLevel) {
        return serverLevel.getDataStorage()
                .computeIfAbsent(LegacySavedDataTypes.type(
                        "gtceu_item_pipe_net", serverLevel, () -> new LevelItemPipeNet(serverLevel),
                        (tag, provider) -> new LevelItemPipeNet(serverLevel, tag, provider),
                        (data, tag, provider) -> data.save(tag, provider)));
    }

    public LevelItemPipeNet(ServerLevel serverLevel) {
        super(serverLevel);
    }

    public LevelItemPipeNet(ServerLevel serverLevel, CompoundTag tag, HolderLookup.Provider provider) {
        super(serverLevel, tag, provider);
    }

    @Override
    protected ItemPipeNet createNetInstance() {
        return new ItemPipeNet(this);
    }
}
