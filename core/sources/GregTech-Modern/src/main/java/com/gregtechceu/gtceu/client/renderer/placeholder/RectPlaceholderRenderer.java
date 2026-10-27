package com.gregtechceu.gtceu.client.renderer.placeholder;

import com.gregtechceu.gtceu.api.placeholder.IPlaceholderRenderer;
import com.gregtechceu.gtceu.client.renderer.GTRenderTypes;
import com.gregtechceu.gtceu.common.machine.multiblock.electric.CentralMonitorMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.electric.monitor.MonitorGroup;

import net.minecraft.nbt.CompoundTag;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import org.joml.Matrix4f;

public class RectPlaceholderRenderer implements IPlaceholderRenderer {

    @Override
    public com.gregtechceu.gtceu.client.model.machine.NativeMachineRenderSubmission extract(CentralMonitorMachine machine, MonitorGroup group, float partialTick, PoseStack poseStack, CompoundTag tag) {
        var consumer = com.gregtechceu.gtceu.client.renderer.monitor.NativeMonitorPrimitives.rectangle(poseStack,tag);
        return consumer.freeze(GTRenderTypes.getMonitor());
    }
}
