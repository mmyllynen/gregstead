package com.gregtechceu.gtceu.client.util;

import com.gregtechceu.gtceu.client.model.GTModelProperties;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import java.util.*;

public final class NativeBlockTints {
    private NativeBlockTints() {}
    @FunctionalInterface public interface Color {int get(BlockState state,BlockAndTintGetter level,BlockPos pos,int layer);}
    public static List<BlockTintSource> layers(int count,Color color) {
        if(count<0)throw new IllegalArgumentException("Negative tint layer count");
        var sources=new ArrayList<BlockTintSource>();
        for(int layer=0;layer<count;layer++) {
            final int index=layer;
            sources.add(new BlockTintSource() {
                public int color(BlockState state){return color.get(state,null,null,index);}
                public int colorInWorld(BlockState state,BlockAndTintGetter level,BlockPos pos){return color.get(state,level,pos,index);}
            });
        }
        return List.copyOf(sources);
    }
    public static List<BlockTintSource> opaqueLayers(int count, Color color) {
        return layers(count, (state, level, pos, layer) -> {
            int value = color.get(state, level, pos, layer);
            return value == -1 ? -1 : value | 0xff000000;
        });
    }
    public static List<BlockTintSource> materialPipe(Color fallback) {
        return layers(3,(state,level,pos,layer)-> {
            if(level!=null && pos!=null && (layer==0 || layer==1)) {
                var paint=level.getModelData(pos).get(GTModelProperties.PIPE_PAINT_COLOR);
                if(paint!=null)return paint|0xff000000;
            }
            int color=fallback.get(state,level,pos,layer);
            return color==-1?-1:color|0xff000000;
        });
    }
    public static List<BlockTintSource> opticalPipe() {
        return layers(5,(state,level,pos,layer)-> {
            if(level==null || pos==null)return -1;
            var data=level.getModelData(pos);
            var frame=layer==3?data.get(GTModelProperties.PIPE_FRAME_PRIMARY_COLOR)
                    :layer==4?data.get(GTModelProperties.PIPE_FRAME_SECONDARY_COLOR):null;
            if(frame!=null)return frame|0xff000000;
            var paint=data.get(GTModelProperties.PIPE_PAINT_COLOR);
            return paint==null?-1:paint|0xff000000;
        });
    }
}
