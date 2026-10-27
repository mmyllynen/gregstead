package com.gregtechceu.gtceu.common.pipelike.fluidpipe;

import com.lowdragmc.lowdraglib.transfer.persistence.LegacySavedDataTypes;

import com.gregtechceu.gtceu.api.data.chemical.material.properties.FluidPipeProperties;
import com.gregtechceu.gtceu.api.pipenet.LevelPipeNet;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;

public class LevelFluidPipeNet extends LevelPipeNet<FluidPipeProperties, FluidPipeNet> {

    public static LevelFluidPipeNet getOrCreate(ServerLevel serverLevel) {
        return serverLevel.getDataStorage()
                .computeIfAbsent(LegacySavedDataTypes.type(
                        "gtceu_fluid_pipe_net", serverLevel, () -> new LevelFluidPipeNet(serverLevel),
                        (tag, provider) -> new LevelFluidPipeNet(serverLevel, tag, provider),
                        (data, tag, provider) -> data.save(tag, provider)));
    }

    public LevelFluidPipeNet(ServerLevel serverLevel) {
        super(serverLevel);
    }

    public LevelFluidPipeNet(ServerLevel serverLevel, CompoundTag tag, HolderLookup.Provider provider) {
        super(serverLevel, tag, provider);
    }

    @Override
    protected FluidPipeNet createNetInstance() {
        return new FluidPipeNet(this);
    }
}
