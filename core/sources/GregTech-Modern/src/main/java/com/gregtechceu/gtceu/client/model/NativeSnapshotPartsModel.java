package com.gregtechceu.gtceu.client.model;

import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.*;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import java.util.List;
import java.util.function.BiFunction;

/** Adds immutable prepared geometry to a native block model, preserving child item geometry. */
public final class NativeSnapshotPartsModel implements BlockStateModel {
    private final BlockStateModel child;
    private final BiFunction<BlockAndTintGetter,BlockPos,List<? extends BlockStateModelPart>> snapshots;
    private final int possibleFlags;
    public NativeSnapshotPartsModel(BlockStateModel child,
            BiFunction<BlockAndTintGetter,BlockPos,List<? extends BlockStateModelPart>> snapshots,int possibleFlags) {
        this.child=java.util.Objects.requireNonNull(child);this.snapshots=java.util.Objects.requireNonNull(snapshots);
        this.possibleFlags=possibleFlags;
    }
    @Override public void collectParts(RandomSource random,List<BlockStateModelPart> output){child.collectParts(random,output);}
    @Override public void collectParts(BlockAndTintGetter level,BlockPos pos,BlockState state,RandomSource random,List<BlockStateModelPart> output){
        child.collectParts(level,pos,state,random,output);
        var parts=snapshots.apply(level,pos);if(parts!=null)output.addAll(parts);
    }
    @Override public Material.Baked particleMaterial(){return child.particleMaterial();}
    @Override public Material.Baked particleMaterial(BlockAndTintGetter level,BlockPos pos,BlockState state){return child.particleMaterial(level,pos,state);}
    @Override public int materialFlags(){return child.materialFlags()|possibleFlags;}
    @Override public int materialFlags(BlockAndTintGetter level,BlockPos pos,BlockState state){
        int flags=child.materialFlags(level,pos,state);var parts=snapshots.apply(level,pos);
        if(parts!=null)for(var part:parts)flags|=part.materialFlags();return flags;
    }
}
