package com.gregtechceu.gtceu.client.model.machine;
import com.gregtechceu.gtceu.api.machine.feature.IMachineFeature;
public interface NativeMachineRenderExtractor<T extends IMachineFeature> {
    NativeMachineRenderSubmission extractSubmission(T machine, float partialTick);
}
