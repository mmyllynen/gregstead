package com.gregtechceu.gtceu.client.model;

import com.gregtechceu.gtceu.client.util.GTQuadTransformers;
import com.gregtechceu.gtceu.core.IGTBakedQuad;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.*;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.util.TriState;
import java.util.*;
import java.util.function.UnaryOperator;

public class TextureOverrideModel<T extends BlockStateModel> implements BlockStateModel {
    public static final UnaryOperator<BakedQuad> OVERLAY_OFFSET=GTQuadTransformers.offset(0.002f);
    private final T child;
    private final Map<String,TextureAtlasSprite> textureOverrides;
    public TextureOverrideModel(T child,Map<String,TextureAtlasSprite> overrides) {
        this.child=Objects.requireNonNull(child);textureOverrides=Map.copyOf(overrides);
    }
    public T getChild(){return child;}
    public Map<String,TextureAtlasSprite> getTextureOverrides(){return textureOverrides;}
    @Override public void collectParts(RandomSource random,List<BlockStateModelPart> output) {
        var parts=new ArrayList<BlockStateModelPart>();child.collectParts(random,parts);parts.forEach(part->output.add(new Part(part)));
    }
    @Override public void collectParts(BlockAndTintGetter level,BlockPos pos,BlockState state,RandomSource random,List<BlockStateModelPart> output) {
        var parts=new ArrayList<BlockStateModelPart>();child.collectParts(level,pos,state,random,parts);parts.forEach(part->output.add(new Part(part)));
    }
    @Override public Material.Baked particleMaterial(){return child.particleMaterial();}
    @Override public Material.Baked particleMaterial(BlockAndTintGetter level,BlockPos pos,BlockState state){return child.particleMaterial(level,pos,state);}
    private int flags(int flags) {
        return textureOverrides.values().stream().anyMatch(sprite->sprite.contents().isAnimated())?flags|BakedQuad.FLAG_ANIMATED:flags;
    }
    @Override public int materialFlags(){return flags(child.materialFlags());}
    @Override public int materialFlags(BlockAndTintGetter level,BlockPos pos,BlockState state){return flags(child.materialFlags(level,pos,state));}
    private final class Part implements BlockStateModelPart {
        private final BlockStateModelPart original;
        Part(BlockStateModelPart original){this.original=original;}
        public List<BakedQuad> getQuads(Direction direction){return retextureQuads(original.getQuads(direction),textureOverrides);}
        public boolean useAmbientOcclusion(){return original.useAmbientOcclusion();}
        public TriState ambientOcclusion(){return original.ambientOcclusion();}
        public Material.Baked particleMaterial(){return original.particleMaterial();}
        public int materialFlags(){return flags(original.materialFlags());}
    }
    public static List<BakedQuad> retextureQuads(List<BakedQuad> quads,Map<String,TextureAtlasSprite> overrides) {
        var result=new ArrayList<BakedQuad>();
        for(var quad:quads) {
            String key=(Object)quad instanceof IGTBakedQuad tagged?tagged.gtceu$getTextureKey():null;
            if(key==null || key.isEmpty())continue;
            if(key.charAt(0)=='#')key=key.substring(1);
            var replacement=overrides.get(key);result.add(replacement==null?quad:GTQuadTransformers.setSprite(quad,replacement));
        }
        return result;
    }
}
