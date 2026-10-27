package com.gregtechceu.gtceu.client.model.machine;
import com.gregtechceu.gtceu.client.model.TextureOverrideModel;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import java.util.*;
import java.util.function.BiFunction;
public final class NativeMachineTextureModel implements BlockStateModel, NativeMachineTextures.Metadata {
    private final BlockStateModel child;
    private final Set<String> replaceable;
    private final Map<String,TextureAtlasSprite> overrides;
    private final BiFunction<BlockAndTintGetter,BlockPos,Map<String,TextureAtlasSprite>> snapshot;
    public NativeMachineTextureModel(BlockStateModel child,Set<String> replaceable,Map<String,TextureAtlasSprite> overrides,
            BiFunction<BlockAndTintGetter,BlockPos,Map<String,TextureAtlasSprite>> snapshot) {
        this.child=Objects.requireNonNull(child);this.replaceable=Set.copyOf(replaceable);this.overrides=Map.copyOf(overrides);this.snapshot=snapshot;
    }
    public Set<String> replaceableTextures(){return replaceable;}
    public Map<String,TextureAtlasSprite> textureOverrides(){return overrides;}
    @Override public void collectParts(RandomSource random,List<BlockStateModelPart> output){child.collectParts(random,output);}
    @Override public void collectParts(BlockAndTintGetter level,BlockPos pos,BlockState state,RandomSource random,List<BlockStateModelPart> output){
        var replacements=snapshot.apply(level,pos);
        if(replacements==null || replacements.isEmpty())child.collectParts(level,pos,state,random,output);
        else new TextureOverrideModel<>(child,NativeMachineTextures.remap(replaceable,replacements)).collectParts(level,pos,state,random,output);
    }
    @Override public Material.Baked particleMaterial(){return child.particleMaterial();}
    @Override public Material.Baked particleMaterial(BlockAndTintGetter level,BlockPos pos,BlockState state){return child.particleMaterial(level,pos,state);}
    @Override public int materialFlags(){return child.materialFlags() | net.minecraft.client.resources.model.geometry.BakedQuad.FLAG_ANIMATED | net.minecraft.client.resources.model.geometry.BakedQuad.FLAG_TRANSLUCENT;}
    @Override public int materialFlags(BlockAndTintGetter level,BlockPos pos,BlockState state){var replacements=snapshot.apply(level,pos);
        return replacements==null || replacements.isEmpty()?child.materialFlags(level,pos,state):
                new TextureOverrideModel<>(child,NativeMachineTextures.remap(replaceable,replacements)).materialFlags(level,pos,state);}
}
