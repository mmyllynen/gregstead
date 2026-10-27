package com.gregtechceu.gtceu.client.renderer.cover;

import com.gregtechceu.gtceu.api.machine.MetaMachine;

import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.core.Direction;

import com.mojang.blaze3d.vertex.PoseStack;

public interface IDynamicCoverRenderer {

    com.gregtechceu.gtceu.client.model.machine.NativeMachineRenderSubmission extract(
            MetaMachine machine, Direction face, float partialTick, int packedLight, int packedOverlay);
}
