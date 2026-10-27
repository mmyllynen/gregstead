package com.lowdragmc.lowdraglib.client.bakedpipeline;

import com.mojang.math.Quadrant;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.cuboid.CuboidFace;
import net.minecraft.client.resources.model.cuboid.CuboidRotation;
import net.minecraft.client.resources.model.cuboid.FaceBakery;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.block.dispatch.ModelState;
import net.minecraft.core.Direction;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import javax.annotation.Nullable;

/** Direct face geometry using native winding, transforms, UV and material bake semantics. */
public final class FaceQuadBakery {
    private FaceQuadBakery() {}
    private static final ModelBaker.Interner INTERNER=new ModelBaker.Interner() {
        public Vector3fc vector(Vector3fc vector){return new Vector3f(vector);}
        public BakedQuad.MaterialInfo materialInfo(BakedQuad.MaterialInfo material){return material;}
    };
    public static BakedQuad bakeQuad(Vector3f from,Vector3f to,CuboidFace face,TextureAtlasSprite sprite,
            Direction facing,ModelState state,@Nullable CuboidRotation rotation,boolean shade,int emissivity) {
        if(emissivity<0 || emissivity>15)throw new IllegalArgumentException("Emissivity must be between 0 and 15");
        var uv=face.uvs()==null?FaceBakery.defaultFaceUV(from,to,facing):face.uvs();
        int tint=face.tintIndex()<-100?-face.tintIndex()-101:face.tintIndex();
        var transparency=sprite.contents().computeTransparency(Math.min(uv.minU(),uv.maxU())/16,
                Math.min(uv.minV(),uv.maxV())/16,Math.max(uv.minU(),uv.maxU())/16,Math.max(uv.minV(),uv.maxV())/16);
        var material=BakedQuad.MaterialInfo.of(new Material.Baked(sprite,false),transparency,tint,
                shade?null:Direction.UP,emissivity,face.faceData().ambientOcclusion());
        return FaceBakery.bakeQuad(INTERNER,from,to,uv,face.rotation(),material,facing,state,rotation,face.faceData());
    }
}
