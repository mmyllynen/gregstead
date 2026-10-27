package com.gregtechceu.gtceu.client.renderer.machine.impl;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.core.Direction;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.client.renderer.texture.OverlayTexture;
import org.joml.Quaternionf;
import java.util.function.Function;

/** Detached quantum chest display submission uses prepared native item state and text. */
public record NativeQuantumChestDisplay(ItemStackRenderState item, String amount, boolean creative,
                                         Direction front, float totalTick) {
    public static NativeQuantumChestDisplay capture(ItemStack stored,ItemStack locked,long count,boolean creative,
            Direction front,float tick,Function<ItemStack,ItemStackRenderState> itemResolver,Function<Long,String> formatter) {
        ItemStack chosen=stored.isEmpty()?locked:stored;
        return new NativeQuantumChestDisplay(chosen.isEmpty()?null:itemResolver.apply(chosen.copy()),
                creative?"∞":count<=0?"*":formatter.apply(count),creative,front,tick);
    }
    public void submit(PoseStack pose,SubmitNodeCollector collector,Font font) {
        if(item==null)return;
        pose.pushPose();
        try {
            pose.translate(.5f,.5f,.5f);
            if(front.getAxis()==Direction.Axis.Y)pose.rotate(new org.joml.Vector3f(0,0,-1).rotationTo(new org.joml.Vector3f(front.getStepX(),front.getStepY(),front.getStepZ()),new Quaternionf()));
            pose.rotate(new Quaternionf().rotateY(totalTick*Mth.TWO_PI/80));
            pose.scale(.6f,.6f,.6f);
            item.submit(pose,collector,LightCoordsUtil.FULL_BRIGHT,OverlayTexture.NO_OVERLAY,0);
        } finally {pose.popPose();}
        submitAmount(pose,collector,font);
    }
    public void submitAmount(PoseStack pose,SubmitNodeCollector collector,Font font) {
        pose.pushPose();
        try {
            pose.translate(front.getStepX()*-1/16f,front.getStepY()*-1/16f,front.getStepZ()*-1/16f);
            pose.translate(.5f+front.getStepX()*.5f,.5f+front.getStepY()*.5f,.5f+front.getStepZ()*.5f);
            float angle=Mth.HALF_PI*switch(front){case UP,WEST->1;case DOWN,EAST->-1;case SOUTH->2;case NORTH->0;};
            var rotation=new Quaternionf();
            if(front.getAxis()==Direction.Axis.Y){pose.scale(1,-1,1);rotation.rotateX(angle);}
            else {pose.scale(-1,-1,-1);rotation.rotateY(angle);}
            if(front==Direction.DOWN)rotation.rotateZ(Mth.PI);
            pose.rotate(rotation);pose.scale(1/64f,1/64f,0);pose.translate(-32,-32,0);
            if(creative){pose.translate(32,38,0);pose.scale(3,3,1);pose.translate(-32,-38,0);}
            collector.submitText(pose,32-font.getSplitter().stringWidth(amount)/2f,38-font.lineHeight/2f,
                    net.minecraft.network.chat.Component.literal(amount).getVisualOrderText(),false,Font.DisplayMode.SEE_THROUGH,
                    0xffffffff,0,LightCoordsUtil.FULL_BRIGHT,0);
        } finally {pose.popPose();}
    }
}
