package com.gregtechceu.gtceu.client.renderer.monitor;
import com.gregtechceu.gtceu.client.model.machine.NativeMachineRenderSubmission;
import com.gregtechceu.gtceu.common.machine.multiblock.electric.CentralMonitorMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.electric.monitor.MonitorGroup;
import com.mojang.blaze3d.vertex.PoseStack;
public interface IMonitorRenderer {
    NativeMachineRenderSubmission extract(CentralMonitorMachine machine,MonitorGroup group,float partialTick,PoseStack pose);
}
