package com.gregtechceu.gtceu.client.model.machine;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.item.*;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.world.item.*;
import org.joml.*;
import java.util.*;
/** Native special layer owns an immutable list of already-extracted display submissions. */
public final class NativeItemDisplayLayer {
    private NativeItemDisplayLayer() {}
    public static void append(ItemStackRenderState output,ItemStack stack,ItemDisplayContext context,
            ModelRenderProperties properties,Matrix4fc transform,List<NativeMachineRenderSubmission> captures,Object identity) {
        if(captures.isEmpty())return;
        var renderer=new Display(List.copyOf(captures));
        var layer=output.newLayer();layer.setupSpecialModel(renderer,renderer.extractArgument(stack));
        layer.setLocalTransform(transform);layer.setExtents(()->new Vector3fc[]{new Vector3f(0,0,0),new Vector3f(1,1,1)});
        properties.applyToLayer(layer,context);output.setAnimated();output.appendModelIdentityElement(identity);
    }
    private record Display(List<NativeMachineRenderSubmission> captures) implements SpecialModelRenderer<List<NativeMachineRenderSubmission>> {
        public void submit(List<NativeMachineRenderSubmission> data,PoseStack pose,SubmitNodeCollector collector,int light,int overlay,boolean foil,int outline){
            for(var submission:data)submission.submit(pose,collector);
        }
        public void getExtents(java.util.function.Consumer<Vector3fc> output){output.accept(new Vector3f(0,0,0));output.accept(new Vector3f(1,1,1));}
        public List<NativeMachineRenderSubmission> extractArgument(ItemStack stack){return captures;}
    }
}
