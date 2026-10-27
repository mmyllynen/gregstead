package com.gregtechceu.gtceu.client.model.machine;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import java.util.List;
import java.util.function.BiFunction;
public final class NativeMachineOutputModel implements BlockStateModel {
    private final BlockStateModel base;
    private final java.util.Map<NativeMachineOutput,java.util.Optional<com.gregtechceu.gtceu.client.model.pipe.NativeCoverSnapshot>> geometry = new java.util.concurrent.ConcurrentHashMap<>();
    private java.util.Optional<com.gregtechceu.gtceu.client.model.pipe.NativeCoverSnapshot> geometry(NativeMachineOutput snapshot) {
        return geometry.computeIfAbsent(snapshot, value -> value.geometry(pipe,item,fluid));
    }
    private final TextureAtlasSprite pipe,item,fluid;
    private final BiFunction<BlockAndTintGetter,BlockPos,NativeMachineOutput> lookup;
    public NativeMachineOutputModel(BlockStateModel base,TextureAtlasSprite pipe,TextureAtlasSprite item,TextureAtlasSprite fluid,
            BiFunction<BlockAndTintGetter,BlockPos,NativeMachineOutput> lookup) {
        this.base=base;this.pipe=pipe;this.item=item;this.fluid=fluid;this.lookup=lookup;
    }
    @Override public void collectParts(RandomSource random,List<BlockStateModelPart> output){base.collectParts(random,output);}
    @Override public void collectParts(BlockAndTintGetter level,BlockPos pos,BlockState state,RandomSource random,List<BlockStateModelPart> output){
        base.collectParts(level,pos,state,random,output);
        var snapshot=lookup.apply(level,pos);
        if(snapshot!=null)geometry(snapshot).ifPresent(output::add);
    }
    @Override public Material.Baked particleMaterial(){return base.particleMaterial();}
    @Override public Material.Baked particleMaterial(BlockAndTintGetter level,BlockPos pos,BlockState state){return base.particleMaterial(level,pos,state);}
    @Override public int materialFlags(){return base.materialFlags() | new NativeMachineOutput(net.minecraft.core.Direction.NORTH,true,net.minecraft.core.Direction.SOUTH,true)
            .geometry(pipe,item,fluid).map(part -> part.materialFlags()).orElse(0);}
    @Override public int materialFlags(BlockAndTintGetter level,BlockPos pos,BlockState state){var snapshot=lookup.apply(level,pos);
        return base.materialFlags(level,pos,state) | (snapshot==null?0:geometry(snapshot).map(part->part.materialFlags()).orElse(0));}
}
