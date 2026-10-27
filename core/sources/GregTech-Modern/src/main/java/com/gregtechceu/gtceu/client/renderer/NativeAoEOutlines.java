package com.gregtechceu.gtceu.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import net.minecraft.world.phys.shapes.VoxelShape;

/** Detached outline positions/shapes/colors; submission reads only the native frame camera. */
public record NativeAoEOutlines(List<Entry> entries,boolean suppressVanilla,float width,boolean improvedTransparency) {
    public record Entry(BlockPos pos,VoxelShape shape,Integer color) {
        public Entry { pos=pos.immutable(); }
    }
    public NativeAoEOutlines { entries=List.copyOf(entries); }
    public net.neoforged.neoforge.client.CustomBlockOutlineRenderer renderer() {
        return (outline,collector,pose,state) -> {
            for(var entry:entries)submit(entry,collector,pose,state,outline.highContrast(),outline.isTranslucent());
            return suppressVanilla;
        };
    }
    private void submit(Entry entry,SubmitNodeCollector collector,PoseStack pose,LevelRenderState state,
                        boolean highContrast,boolean translucent) {
        var camera=state.cameraRenderState.pos;
        pose.pushPose();
        try {
            pose.translate(entry.pos().getX()-camera.x,entry.pos().getY()-camera.y,entry.pos().getZ()-camera.z);
            if(highContrast && entry.color()==null)
                collector.submitShapeOutline(pose,entry.shape(),RenderTypes.secondaryBlockOutline(),0xFF000000,7,translucent);
            var type=highContrast && entry.color()==null?RenderTypes.linesDepthBias():
                    improvedTransparency?RenderTypes.linesTranslucentNoDepthWrite():RenderTypes.linesTranslucent();
            int color=entry.color()!=null?entry.color():highContrast?0xFF57FFE1:0x66000000;
            collector.submitShapeOutline(pose,entry.shape(),type,color,width,translucent);
        } finally { pose.popPose(); }
    }
}
