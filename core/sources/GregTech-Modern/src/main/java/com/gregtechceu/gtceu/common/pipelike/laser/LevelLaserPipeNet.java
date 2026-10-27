package com.gregtechceu.gtceu.common.pipelike.laser;

import com.lowdragmc.lowdraglib.transfer.persistence.LegacySavedDataTypes;

import com.gregtechceu.gtceu.api.pipenet.LevelPipeNet;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;

public class LevelLaserPipeNet extends LevelPipeNet<LaserPipeProperties, LaserPipeNet> {

    private static final String DATA_ID = "gtceu_laser_pipe_net";

    public static LevelLaserPipeNet getOrCreate(ServerLevel serverLevel) {
        return serverLevel.getDataStorage()
                .computeIfAbsent(LegacySavedDataTypes.type(
                        DATA_ID, serverLevel, () -> new LevelLaserPipeNet(serverLevel),
                        (tag, provider) -> new LevelLaserPipeNet(serverLevel, tag, provider),
                        (data, tag, provider) -> data.save(tag, provider)));
    }

    public LevelLaserPipeNet(ServerLevel serverLevel) {
        super(serverLevel);
    }

    public LevelLaserPipeNet(ServerLevel serverLevel, CompoundTag tag, HolderLookup.Provider provider) {
        super(serverLevel, tag, provider);
    }

    @Override
    protected LaserPipeNet createNetInstance() {
        return new LaserPipeNet(this);
    }
}
