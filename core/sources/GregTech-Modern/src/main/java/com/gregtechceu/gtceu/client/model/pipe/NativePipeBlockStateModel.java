package com.gregtechceu.gtceu.client.model.pipe;

import com.gregtechceu.gtceu.client.model.GTModelProperties;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import java.util.*;

/** Native dynamic pipe-part backend. Block entity data comes exclusively from the meshing snapshot. */
public final class NativePipeBlockStateModel implements BlockStateModel {
    private final Map<Direction,BlockStateModel> parts;
    private final Map<Direction,BlockStateModel> restrictors;
    private final BlockStateModel fallback;
    public NativePipeBlockStateModel(Map<Direction,BlockStateModel> parts,Map<Direction,BlockStateModel> restrictors,
            BlockStateModel fallback) {
        this.parts=Collections.unmodifiableMap(new IdentityHashMap<>(parts));
        this.restrictors=Collections.unmodifiableMap(new IdentityHashMap<>(restrictors));
        this.fallback=Objects.requireNonNull(fallback);
    }
    private BlockStateModel model(PipePartSelection.Part part) {
        return (part.restrictor()?restrictors:parts).getOrDefault(part.direction(),fallback);
    }
    @Override public void collectParts(BlockAndTintGetter level,BlockPos pos,BlockState state,
            RandomSource random,List<BlockStateModelPart> output) {
        var data=level.getModelData(pos);
        for(var selected:PipePartSelection.select(data.get(GTModelProperties.PIPE_CONNECTION_MASK),
                data.get(GTModelProperties.PIPE_BLOCKED_MASK),false))
            model(selected).collectParts(level,pos,state,random,output);
        var covers=data.get(GTModelProperties.PIPE_COVER_GEOMETRY);
        if(covers!=null && data.get(GTModelProperties.PIPE_CONNECTION_MASK)!=null)output.addAll(covers);
        var frame=data.get(GTModelProperties.PIPE_FRAME_STATE);
        if(frame!=null && data.get(GTModelProperties.PIPE_CONNECTION_MASK)!=null) {
            var model=Minecraft.getInstance().getModelManager().getBlockStateModelSet().get(frame);
            var frameParts=new ArrayList<BlockStateModelPart>();model.collectParts(level,pos,frame,random,frameParts);
            var covered=data.get(GTModelProperties.PIPE_COVER_MASK);
            for(var part:frameParts)output.add(new NativePipeFramePart(part,frame,level,pos,covered==null?0:covered));
        }
    }
    @Override public void collectParts(RandomSource random,List<BlockStateModelPart> output) {
        for(var selected:PipePartSelection.select(null,null,true))model(selected).collectParts(random,output);
    }
    @Override public Material.Baked particleMaterial() {return parts.getOrDefault(null,fallback).particleMaterial();}
    @Override public Material.Baked particleMaterial(BlockAndTintGetter level,BlockPos pos,BlockState state) {
        return parts.getOrDefault(null,fallback).particleMaterial(level,pos,state);
    }
    @Override public int materialFlags() {
        int flags=0;
        for(var child:parts.values())flags|=child.materialFlags();
        for(var child:restrictors.values())flags|=child.materialFlags();
        return flags|fallback.materialFlags();
    }
    @Override public int materialFlags(BlockAndTintGetter level,BlockPos pos,BlockState state) {
        var data=level.getModelData(pos);int flags=0;
        for(var selected:PipePartSelection.select(data.get(GTModelProperties.PIPE_CONNECTION_MASK),
                data.get(GTModelProperties.PIPE_BLOCKED_MASK),false))flags|=model(selected).materialFlags(level,pos,state);
        var covers=data.get(GTModelProperties.PIPE_COVER_GEOMETRY);
        if(covers!=null && data.get(GTModelProperties.PIPE_CONNECTION_MASK)!=null)for(var cover:covers)flags|=cover.materialFlags();
        var frame=data.get(GTModelProperties.PIPE_FRAME_STATE);
        if(frame!=null && data.get(GTModelProperties.PIPE_CONNECTION_MASK)!=null)
            flags|=Minecraft.getInstance().getModelManager().getBlockStateModelSet().get(frame).materialFlags(level,pos,frame);
        return flags;
    }
}
