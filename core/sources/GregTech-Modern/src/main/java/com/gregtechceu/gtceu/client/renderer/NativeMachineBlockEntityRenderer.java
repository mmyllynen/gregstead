package com.gregtechceu.gtceu.client.renderer;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.client.model.machine.*;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.*;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.phys.Vec3;
import java.util.*;
/** Native extraction/submission adapter; pending renderers retain their metadata for conversion. */
public final class NativeMachineBlockEntityRenderer implements BlockEntityRenderer<MetaMachine,NativeMachineBlockEntityRenderer.State> {
    public static final class State extends BlockEntityRenderState {
        private List<NativeMachineRenderSubmission> submissions=List.of();
    }
    public NativeMachineBlockEntityRenderer(BlockEntityRendererProvider.Context context) {}
    @Override public State createRenderState(){return new State();}
    @Override @SuppressWarnings({"rawtypes","unchecked"})
    public void extractRenderState(MetaMachine machine,State state,float partialTick,Vec3 camera,ModelFeatureRenderer.CrumblingOverlay breaking) {
        BlockEntityRenderer.super.extractRenderState(machine,state,partialTick,camera,breaking);
        var captures=new ArrayList<NativeMachineRenderSubmission>();
        captures.addAll(new com.gregtechceu.gtceu.client.renderer.cover.ICoverableRenderer() {}
                .extractDynamicCovers(machine,partialTick,state.lightCoords,net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY));
        var model=Minecraft.getInstance().getModelManager().getBlockStateModelSet().get(machine.getBlockState());
        model=com.gregtechceu.gtceu.client.model.machine.NativeMachineModelLookup.unwrap(model);
        if(model instanceof NativeMachineRenderMetadata metadata) {
            for(var render:metadata.dynamicRenders()) {
                if(render instanceof NativeMachineRenderExtractor extractor && ((com.gregtechceu.gtceu.client.renderer.machine.DynamicRender)render).shouldRender(machine,camera))
                    captures.add(extractor.extractSubmission(machine,partialTick));
            }
        }
        state.submissions=List.copyOf(captures);
    }
    @Override public void submit(State state,PoseStack pose,SubmitNodeCollector collector,CameraRenderState camera){
        for(var submission:state.submissions)submission.submit(pose,collector);
    }
    @Override public boolean shouldRenderOffScreen(){return true;}
    @Override @SuppressWarnings({"rawtypes","unchecked"})
    public boolean shouldRender(MetaMachine machine,Vec3 camera) {
        if(machine.getCoverContainer().hasDynamicCovers())return true;
        var model=Minecraft.getInstance().getModelManager().getBlockStateModelSet().get(machine.getBlockState());
        model=com.gregtechceu.gtceu.client.model.machine.NativeMachineModelLookup.unwrap(model);
        if(model instanceof NativeMachineRenderMetadata metadata)
            for(var render:metadata.dynamicRenders())if(((com.gregtechceu.gtceu.client.renderer.machine.DynamicRender)render).shouldRender(machine,camera))return true;
        return false;
    }
    @Override @SuppressWarnings({"rawtypes","unchecked"})
    public net.minecraft.world.phys.AABB getRenderBoundingBox(MetaMachine machine) {
        var bounds=BlockEntityRenderer.super.getRenderBoundingBox(machine);
        var model=Minecraft.getInstance().getModelManager().getBlockStateModelSet().get(machine.getBlockState());
        model=com.gregtechceu.gtceu.client.model.machine.NativeMachineModelLookup.unwrap(model);
        if(model instanceof NativeMachineRenderMetadata metadata)
            for(var render:metadata.dynamicRenders())bounds=bounds.minmax(((com.gregtechceu.gtceu.client.renderer.machine.DynamicRender)render).getRenderBoundingBox(machine));
        return bounds;
    }
}
