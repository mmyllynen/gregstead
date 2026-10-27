package com.lowdragmc.lowdraglib.client.renderer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.*;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.*;
import javax.annotation.Nullable;

public class ATESRRendererProvider<T extends BlockEntity> implements BlockEntityRenderer<T, ATESRRendererProvider.State> {
    public static class State extends BlockEntityRenderState {
        private NativeDelegatedBlockEntity.Submission submission;
    }
    public ATESRRendererProvider() {}
    public ATESRRendererProvider(BlockEntityRendererProvider.Context context) {}
    public State createRenderState() { return new State(); }
    public void extractRenderState(T entity,State state,float partialTicks,Vec3 camera,ModelFeatureRenderer.CrumblingOverlay breaking) {
        BlockEntityRenderer.super.extractRenderState(entity,state,partialTicks,camera,breaking);
        state.submission = null;
        var renderer=getRenderer(entity);
        if(renderer!=null) state.submission=renderer.extractBlockEntity(entity,partialTicks,camera,breaking);
    }
    public void submit(State state,PoseStack pose,SubmitNodeCollector collector,CameraRenderState camera) {
        if(state.submission!=null)state.submission.submit(pose,collector,camera);
    }
    @Nullable public NativeBlockEntitySource getRenderer(T entity) {
        return entity.getLevel()!=null && entity.getBlockState().getBlock() instanceof NativeBlockModelProvider provider
                ? provider.getNativeBlockRenderer(entity.getBlockState()) : null;
    }
    public boolean hasRenderer(T entity) { var renderer=getRenderer(entity);return renderer!=null && renderer.hasTESR(entity); }
    public boolean shouldRender(T entity,Vec3 camera) {
        var renderer=getRenderer(entity);return renderer==null ? BlockEntityRenderer.super.shouldRender(entity,camera) : renderer.shouldRender(entity,camera);
    }
    public AABB getRenderBoundingBox(T entity) {
        var renderer=getRenderer(entity);return renderer==null ? BlockEntityRenderer.super.getRenderBoundingBox(entity) : renderer.getRenderBoundingBox(entity);
    }
    // Native dispatcher asks this without an entity; entity-specific policy is handled in shouldRender.
    public boolean shouldRenderOffScreen() { return false; }
    public boolean isGlobalRenderer(T entity) { var renderer=getRenderer(entity);return renderer!=null && renderer.isGlobalRenderer(entity); }
}
