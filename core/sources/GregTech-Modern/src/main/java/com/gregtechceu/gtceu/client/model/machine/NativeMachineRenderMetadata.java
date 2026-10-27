package com.gregtechceu.gtceu.client.model.machine;
import com.gregtechceu.gtceu.client.renderer.machine.DynamicRender;
import net.minecraft.client.renderer.block.dispatch.*;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import java.util.*;
public record NativeMachineRenderMetadata(NativeMachineTextureModel child, List<DynamicRender<?,?>> dynamicRenders)
        implements BlockStateModel, NativeMachineTextures.Metadata {
    public NativeMachineRenderMetadata { dynamicRenders=List.copyOf(dynamicRenders); }
    public Set<String> replaceableTextures(){return child.replaceableTextures();}
    public Map<String,TextureAtlasSprite> textureOverrides(){return child.textureOverrides();}
    public void collectParts(RandomSource random,List<BlockStateModelPart> output){child.collectParts(random,output);}
    public void collectParts(BlockAndTintGetter level,BlockPos pos,BlockState state,RandomSource random,List<BlockStateModelPart> output){child.collectParts(level,pos,state,random,output);}
    public Material.Baked particleMaterial(){return child.particleMaterial();}
    public Material.Baked particleMaterial(BlockAndTintGetter level,BlockPos pos,BlockState state){return child.particleMaterial(level,pos,state);}
    public int materialFlags(){return child.materialFlags();}
    public int materialFlags(BlockAndTintGetter level,BlockPos pos,BlockState state){return child.materialFlags(level,pos,state);}
}
