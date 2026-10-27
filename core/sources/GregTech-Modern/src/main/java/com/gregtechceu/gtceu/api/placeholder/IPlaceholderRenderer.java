package com.gregtechceu.gtceu.api.placeholder;

import com.gregtechceu.gtceu.common.machine.multiblock.electric.CentralMonitorMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.electric.monitor.MonitorGroup;

import net.minecraft.nbt.CompoundTag;

import com.mojang.blaze3d.vertex.PoseStack;

public interface IPlaceholderRenderer {

    com.gregtechceu.gtceu.client.model.machine.NativeMachineRenderSubmission extract(CentralMonitorMachine machine, MonitorGroup group, float partialTick, PoseStack poseStack, CompoundTag tag);
}
