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
/** Controller-prepared casing replaces only designated base slots; overlays remain on the part. */
public final class NativeControllerGeometryModel implements BlockStateModel {
    private final BlockStateModel child;
    private final Map<String,TextureAtlasSprite> blankTextures;
    private final BiFunction<BlockAndTintGetter,BlockPos,List<? extends BlockStateModelPart>> snapshot;
    public NativeControllerGeometryModel(BlockStateModel child,Set<String> replaceable,TextureAtlasSprite blank,
            BiFunction<BlockAndTintGetter,BlockPos,List<? extends BlockStateModelPart>> snapshot) {
        this.child=Objects.requireNonNull(child);this.snapshot=snapshot;
        var textures=new LinkedHashMap<String,TextureAtlasSprite>();replaceable.forEach(key->textures.put(key,blank));blankTextures=Map.copyOf(textures);
    }
    @Override public void collectParts(RandomSource random,List<BlockStateModelPart> output){child.collectParts(random,output);}
    @Override public void collectParts(BlockAndTintGetter level,BlockPos pos,BlockState state,RandomSource random,List<BlockStateModelPart> output){
        var prepared=snapshot.apply(level,pos);
        if(prepared==null || prepared.isEmpty())child.collectParts(level,pos,state,random,output);
        else {
            if(blankTextures.isEmpty())child.collectParts(level,pos,state,random,output);
            else new TextureOverrideModel<>(child,blankTextures).collectParts(level,pos,state,random,output);
            output.addAll(prepared);
        }
    }
    @Override public Material.Baked particleMaterial(){return child.particleMaterial();}
    @Override public Material.Baked particleMaterial(BlockAndTintGetter level,BlockPos pos,BlockState state){return child.particleMaterial(level,pos,state);}
    @Override public int materialFlags(){return child.materialFlags() | net.minecraft.client.resources.model.geometry.BakedQuad.FLAG_TRANSLUCENT | net.minecraft.client.resources.model.geometry.BakedQuad.FLAG_ANIMATED;}
    @Override public int materialFlags(BlockAndTintGetter level,BlockPos pos,BlockState state){
        int flags=child.materialFlags(level,pos,state);var prepared=snapshot.apply(level,pos);
        if(prepared!=null)for(var part:prepared)flags|=part.materialFlags();return flags;
    }
}
