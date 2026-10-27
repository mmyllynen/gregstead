package com.gregtechceu.gtceu.data.model;

import com.tterrag.registrate.providers.generators.RegistrateLegacyBlockModelBuilder;
import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.client.resources.model.cuboid.FaceBakery;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.model.generators.template.FaceBuilder;
import it.unimi.dsi.fastutil.objects.Reference2FloatMap;
import org.joml.Vector3f;

public final class NativePipeElements {
    private NativePipeElements() {}
    @FunctionalInterface public interface FaceConfigurator {void accept(Direction face,String texture,FaceBuilder builder);}
    public static void add(RegistrateLegacyBlockModelBuilder model,Direction endFace,boolean useEndWithFullCube,
            Reference2FloatMap<Direction> endpoints,float offset,int sideTint,int endTint,
            float x1,float y1,float z1,float x2,float y2,float z2,
            Identifier side,Identifier end,String sideKey,String endKey,FaceConfigurator configure) {
        if(side==null && (endFace==null || end==null))return;
        var sideSlot=TextureSlot.create(sideKey);var endSlot=TextureSlot.create(endKey);
        if(side!=null)model.texture(sideSlot,new Material(side));
        if(endFace!=null && end!=null)model.texture(endSlot,new Material(end));
        boolean full=!useEndWithFullCube && x1==y1 && x1==z1 && x1<=0 && x2==y2 && x2==z2 && x2>=16;
        model.transformTemplate(template -> template.element(element -> {
            element.from(x1-offset,y1-offset,z1-offset).to(x2+offset,y2+offset,z2+offset);
            for(var direction:Direction.values()) {
                boolean isEnd=!full && endFace!=null && (endFace==direction || endFace.getOpposite()==direction);
                if(isEnd ? end==null : side==null)continue;
                element.face(direction,face -> {
                    face.texture(isEnd?endSlot:sideSlot).tintindex(isEnd?endTint:sideTint);
                    // Overlay expansion prevents z-fighting; it must not expand the texture footprint.
                    var uv = FaceBakery.defaultFaceUV(new Vector3f(x1,y1,z1),new Vector3f(x2,y2,z2),direction);
                    face.uvs(uv.minU(),uv.minV(),uv.maxU(),uv.maxV());
                    configure.accept(direction,isEnd?endKey:sideKey,face);
                    float point=endpoints.getFloat(direction);
                    if(point>=16 || point<=0)face.cullface(direction);
                });
            }
        }));
    }
}
