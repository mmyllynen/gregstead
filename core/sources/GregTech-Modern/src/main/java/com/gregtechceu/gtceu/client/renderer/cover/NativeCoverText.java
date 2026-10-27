package com.gregtechceu.gtceu.client.renderer.cover;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.List;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.network.chat.Component;
import net.minecraft.util.LightCoordsUtil;

/** Native text submissions retain the original cover scale, wrapping and ninety-pixel limit. */
public final class NativeCoverText {
    private NativeCoverText() {}
    public static com.gregtechceu.gtceu.client.model.machine.NativeMachineRenderSubmission capture(
            List<? extends Component> texts, Font font) {
        var snapshot=texts.stream().map(Component::copy).toList();
        return (pose,collector)->submit(snapshot,font,pose,collector);
    }
    public static void submit(List<? extends Component> texts, Font font, PoseStack pose, SubmitNodeCollector collector) {
        pose.pushPose();
        try {
            pose.translate(3 / 16f, 3 / 16f, 0);
            pose.scale(1 / 144f, 1 / 144f, 1 / 144f);
            int y=0;
            for(var text:texts) {
                boolean emitted=false;
                for(var line:font.split(text,90)) {
                    if(y>=90)return;
                    collector.submitText(pose,0,y,line,false,Font.DisplayMode.NORMAL,0xff72e500,0,
                        LightCoordsUtil.FULL_BRIGHT,0);
                    y+=font.lineHeight;emitted=true;
                }
                if(!emitted)y+=font.lineHeight;
            }
        } finally {pose.popPose();}
    }
}
