package com.gregtechceu.gtceu.common.pipelike.cable;

import com.lowdragmc.lowdraglib.transfer.persistence.LegacySavedDataTypes;

import com.gregtechceu.gtceu.api.data.chemical.material.properties.WireProperties;
import com.gregtechceu.gtceu.api.pipenet.LevelPipeNet;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;

public class LevelEnergyNet extends LevelPipeNet<WireProperties, EnergyNet> {

    public static LevelEnergyNet getOrCreate(ServerLevel serverLevel) {
        return serverLevel.getDataStorage()
                .computeIfAbsent(LegacySavedDataTypes.type(
                        "gtceu_energy_net", serverLevel, () -> new LevelEnergyNet(serverLevel),
                        (tag, provider) -> new LevelEnergyNet(serverLevel, tag, provider),
                        (data, tag, provider) -> data.save(tag, provider)));
    }

    public LevelEnergyNet(ServerLevel serverLevel) {
        super(serverLevel);
    }

    public LevelEnergyNet(ServerLevel serverLevel, CompoundTag tag, HolderLookup.Provider provider) {
        super(serverLevel, tag, provider);
    }

    @Override
    protected EnergyNet createNetInstance() {
        return new EnergyNet(this);
    }
}
