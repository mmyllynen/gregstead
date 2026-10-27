package com.gregtechceu.gtceu.client.util;

import com.gregtechceu.gtceu.core.IGTBakedQuad;
import net.minecraft.client.renderer.FaceInfo;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.model.geom.builders.UVPair;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.neoforged.neoforge.client.model.quad.BakedColors;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import java.util.function.UnaryOperator;

public final class GTQuadTransformers {
    private GTQuadTransformers() {}
    public static UnaryOperator<BakedQuad> offset(float by){return offset(by,by,by);}
    public static UnaryOperator<BakedQuad> offset(float x,float y,float z) {
        if(x==0 && y==0 && z==0)return UnaryOperator.identity();
        return quad -> {
            var positions=new Vector3fc[4];var face=FaceInfo.fromFacing(quad.direction());
            for(int i=0;i<4;i++) {
                var normal=face.getVertexInfo(i);
                positions[i]=new Vector3f(quad.position(i)).add(normal.xFace()==FaceInfo.Extent.MIN_X?-x:x,
                        normal.yFace()==FaceInfo.Extent.MIN_Y?-y:y,normal.zFace()==FaceInfo.Extent.MIN_Z?-z:z);
            }
            return preserveKey(quad,new BakedQuad(positions[0],positions[1],positions[2],positions[3],
                    quad.packedUV0(),quad.packedUV1(),quad.packedUV2(),quad.packedUV3(),quad.direction(),
                    quad.materialInfo(),quad.bakedNormals(),quad.bakedColors()));
        };
    }
    public static BakedQuad normalizeLegacyTint(BakedQuad quad) {
        var material=quad.materialInfo();int index=material.tintIndex();
        if(index>=-100)return quad;
        var replacement=new BakedQuad.MaterialInfo(material.sprite(),material.layer(),material.itemRenderType(),
                material.itemGlintRenderType(),material.itemGlintSpecialRenderType(),-index-101,
                material.shadeDirectionOverride(),material.lightEmission(),material.ambientOcclusion());
        return preserveKey(quad,new BakedQuad(quad.position0(),quad.position1(),quad.position2(),quad.position3(),
                quad.packedUV0(),quad.packedUV1(),quad.packedUV2(),quad.packedUV3(),quad.direction(),replacement,
                quad.bakedNormals(),quad.bakedColors()));
    }
    public static BakedQuad setColor(BakedQuad quad,int argb,boolean clearTintIndex) {
        var material=quad.materialInfo();
        if(clearTintIndex)material=new BakedQuad.MaterialInfo(material.sprite(),material.layer(),material.itemRenderType(),
                material.itemGlintRenderType(),material.itemGlintSpecialRenderType(),-1,material.shadeDirectionOverride(),
                material.lightEmission(),material.ambientOcclusion());
        return preserveKey(quad,new BakedQuad(quad.position0(),quad.position1(),quad.position2(),quad.position3(),
                quad.packedUV0(),quad.packedUV1(),quad.packedUV2(),quad.packedUV3(),quad.direction(),material,
                quad.bakedNormals(),BakedColors.of(argb)));
    }
    public static BakedQuad setSprite(BakedQuad quad,TextureAtlasSprite sprite) {
        var material=quad.materialInfo();var old=material.sprite();var uv=new long[4];
        for(int i=0;i<4;i++)uv[i]=UVPair.pack(
                Mth.map(UVPair.unpackU(quad.packedUV(i)),old.getU0(),old.getU1(),sprite.getU0(),sprite.getU1()),
                Mth.map(UVPair.unpackV(quad.packedUV(i)),old.getV0(),old.getV1(),sprite.getV0(),sprite.getV1()));
        var replacement=new BakedQuad.MaterialInfo(sprite,material.layer(),material.itemRenderType(),material.itemGlintRenderType(),
                material.itemGlintSpecialRenderType(),material.tintIndex(),material.shadeDirectionOverride(),
                material.lightEmission(),material.ambientOcclusion());
        return preserveKey(quad,new BakedQuad(quad.position0(),quad.position1(),quad.position2(),quad.position3(),
                uv[0],uv[1],uv[2],uv[3],quad.direction(),replacement,quad.bakedNormals(),quad.bakedColors()));
    }
    public static BakedQuad copy(BakedQuad quad) {
        return preserveKey(quad,new BakedQuad(new Vector3f(quad.position0()),new Vector3f(quad.position1()),
                new Vector3f(quad.position2()),new Vector3f(quad.position3()),quad.packedUV0(),quad.packedUV1(),
                quad.packedUV2(),quad.packedUV3(),quad.direction(),quad.materialInfo(),quad.bakedNormals(),quad.bakedColors()));
    }
    private static BakedQuad preserveKey(BakedQuad source,BakedQuad target) {
        if((Object)source instanceof IGTBakedQuad tagged && (Object)target instanceof IGTBakedQuad replacement)
            replacement.gtceu$setTextureKey(tagged.gtceu$getTextureKey());
        return target;
    }
}
