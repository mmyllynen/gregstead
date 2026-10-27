package com.gregtechceu.gtceu.client.model.pipe;

import com.gregtechceu.gtceu.client.util.GTQuadTransformers;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import java.util.*;

/** Frame geometry with frame-local tint baked in and cover-occupied faces hidden. */
public final class NativePipeFramePart implements BlockStateModelPart {
    private final BlockStateModelPart child;
    private final BlockState frameState;
    private final BlockAndTintGetter level;
    private final BlockPos pos;
    private final int covered;
    public NativePipeFramePart(BlockStateModelPart child,BlockState frameState,BlockAndTintGetter level,BlockPos pos,int covered) {
        this.child=child;this.frameState=frameState;this.level=level;this.pos=pos.immutable();this.covered=covered;
    }
    private boolean hidden(Direction direction){return (covered & 1<<direction.ordinal())!=0;}
    @Override public List<BakedQuad> getQuads(Direction direction) {
        if(direction!=null && hidden(direction))return List.of();
        var quads=new ArrayList<BakedQuad>(child.getQuads(direction));
        // Original frames expose all uncovered directional faces through the unculled pass as well.
        if(direction==null)for(var face:Direction.values())if(!hidden(face))quads.addAll(child.getQuads(face));
        var colors=Minecraft.getInstance().getBlockColors();
        for(int i=0;i<quads.size();i++) {
            var quad=quads.get(i);
            if(quad.materialInfo().isTinted()) {
                var tint=colors.getTintSource(frameState,quad.materialInfo().tintIndex());
                quads.set(i,GTQuadTransformers.setColor(quad,tint==null?-1:tint.colorInWorld(frameState,level,pos),true));
            }
        }
        return quads;
    }
    @Override public boolean useAmbientOcclusion(){return child.useAmbientOcclusion();}
    @Override public net.minecraft.util.TriState ambientOcclusion(){return child.ambientOcclusion();}
    @Override public Material.Baked particleMaterial(){return child.particleMaterial();}
    @Override public int materialFlags(){return child.materialFlags();}
}
