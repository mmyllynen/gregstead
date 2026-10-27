package com.gregtechceu.gtceu.client.renderer.monitor;

import com.gregtechceu.gtceu.client.renderer.GTRenderTypes;
import com.gregtechceu.gtceu.client.util.ClientImageCache;
import com.gregtechceu.gtceu.common.machine.multiblock.electric.CentralMonitorMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.electric.monitor.MonitorGroup;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import org.joml.Matrix4f;

public class MonitorImageRenderer implements IMonitorRenderer {

    private final String url;

    public MonitorImageRenderer(String url) {
        this.url = url;
    }

    @Override
    public com.gregtechceu.gtceu.client.model.machine.NativeMachineRenderSubmission extract(CentralMonitorMachine machine,MonitorGroup group,float partialTick,PoseStack poseStack) {
        BlockPos rel=group.getRow(0,machine::toRelative).getFirst();
        BlockPos size=group.getRow(-1,machine::toRelative).getLast().offset(-rel.getX()+1,-rel.getY()+1,-rel.getZ()+1);
        Identifier texture=ClientImageCache.getOrLoadTexture(url);
        if(texture==null)return (pose,collector)->{};
        var pose=new PoseStack();pose.mulPose(poseStack.last().pose());pose.translate(rel.getX(),rel.getY(),rel.getZ());
        var transform=new Matrix4f(pose.last().pose());
        var vertices=new com.gregtechceu.gtceu.client.renderer.machine.NativeVertexCapture();
        float x=size.getX(),y=size.getY();
        vertices.addVertex(0,y,0).setColor(-1).setUv(0,1).setLight(net.minecraft.util.LightCoordsUtil.FULL_BRIGHT);
        vertices.addVertex(x,y,0).setColor(-1).setUv(1,1).setLight(net.minecraft.util.LightCoordsUtil.FULL_BRIGHT);
        vertices.addVertex(x,0,0).setColor(-1).setUv(1,0).setLight(net.minecraft.util.LightCoordsUtil.FULL_BRIGHT);
        vertices.addVertex(0,0,0).setColor(-1).setUv(0,0).setLight(net.minecraft.util.LightCoordsUtil.FULL_BRIGHT);
        var submission=vertices.freeze(GTRenderTypes.guiTexture(texture));
        return (target,collector)->{target.pushPose();try{target.mulPose(transform);submission.submit(target,collector);}finally{target.popPose();}};
    }
}
