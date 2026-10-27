package com.gregtechceu.gtceu.client.renderer.monitor;
import com.gregtechceu.gtceu.client.renderer.machine.NativeVertexCapture;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.LightCoordsUtil;
public final class NativeMonitorPrimitives {
    private NativeMonitorPrimitives() {}
    public static NativeVertexCapture rectangle(PoseStack pose,CompoundTag tag) {
        var capture=new NativeVertexCapture();float x=tag.getFloatOr("width",0),y=tag.getFloatOr("height",0);int color=tag.getIntOr("color",0);
        capture.addVertex(pose.last().pose(),0,y,0).setColor(color).setLight(LightCoordsUtil.FULL_BRIGHT);
        capture.addVertex(pose.last().pose(),x,y,0).setColor(color).setLight(LightCoordsUtil.FULL_BRIGHT);
        capture.addVertex(pose.last().pose(),x,0,0).setColor(color).setLight(LightCoordsUtil.FULL_BRIGHT);
        capture.addVertex(pose.last().pose(),0,0,0).setColor(color).setLight(LightCoordsUtil.FULL_BRIGHT);
        return capture;
    }
    public static NativeVertexCapture quad(PoseStack pose,CompoundTag tag) {
        var capture=new NativeVertexCapture();
        for(int i=1;i<=4;i++)capture.addVertex(pose.last().pose(),tag.getFloatOr("x"+i,0),tag.getFloatOr("y"+i,0),0)
                .setColor(tag.getIntOr("color"+i,0)).setLight(LightCoordsUtil.FULL_BRIGHT);
        return capture;
    }
}
