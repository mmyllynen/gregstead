package com.gregtechceu.gtceu.client.renderer.cover;

import com.gregtechceu.gtceu.api.machine.MetaMachine;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;

import com.mojang.blaze3d.vertex.PoseStack;
import lombok.Setter;

import java.util.List;
import java.util.function.Supplier;

public class CoverTextRenderer implements IDynamicCoverRenderer {

    @Setter
    private Supplier<List<? extends Component>> text;

    public CoverTextRenderer(Supplier<List<? extends Component>> text) {
        this.text = text;
    }

    @Override
    public com.gregtechceu.gtceu.client.model.machine.NativeMachineRenderSubmission extract(
            MetaMachine machine,Direction face,float partialTick,int packedLight,int packedOverlay) {
        return NativeCoverText.capture(text.get(),Minecraft.getInstance().font);
    }
}
