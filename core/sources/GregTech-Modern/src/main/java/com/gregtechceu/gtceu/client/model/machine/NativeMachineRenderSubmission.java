package com.gregtechceu.gtceu.client.model.machine;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
/** Render submission captures all data during extraction and must not retain a machine/world. */
@FunctionalInterface
public interface NativeMachineRenderSubmission {
    void submit(PoseStack pose, SubmitNodeCollector collector);
}
