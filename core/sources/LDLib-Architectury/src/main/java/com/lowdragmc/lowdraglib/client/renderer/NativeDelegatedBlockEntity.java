package com.lowdragmc.lowdraglib.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import javax.annotation.Nullable;

/** Extraction-owned vanilla BER delegation; submit stores no live block entity/world. */
public final class NativeDelegatedBlockEntity {
    private NativeDelegatedBlockEntity() {}
    public interface Submission {
        void submit(PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera);
    }
    public static @Nullable Submission extract(BlockEntity entity, float partialTicks, Vec3 camera,
            @Nullable ModelFeatureRenderer.CrumblingOverlay breaking) {
        var renderer = Minecraft.getInstance().getBlockEntityRenderDispatcher().getRenderer(entity);
        return renderer == null ? null : capture(renderer, entity, partialTicks, camera, breaking, null);
    }
    public static @Nullable Submission extract(BlockEntity entity, float partialTicks, Vec3 camera,
            @Nullable ModelFeatureRenderer.CrumblingOverlay breaking, int lightCoords) {
        var renderer=Minecraft.getInstance().getBlockEntityRenderDispatcher().getRenderer(entity);
        return renderer==null?null:capture(renderer,entity,partialTicks,camera,breaking,lightCoords);
    }
    private static <T extends BlockEntity, S extends BlockEntityRenderState> Submission capture(
            BlockEntityRenderer<T,S> renderer, T entity, float partialTicks, Vec3 camera,
            @Nullable ModelFeatureRenderer.CrumblingOverlay breaking, @Nullable Integer lightOverride) {
        S state = renderer.createRenderState();
        renderer.extractRenderState(entity,state,partialTicks,camera,breaking);
        if(lightOverride!=null)state.lightCoords=lightOverride;
        return (pose,collector,view) -> renderer.submit(state,pose,collector,view);
    }
    public static boolean global(BlockEntity entity) {
        var renderer = Minecraft.getInstance().getBlockEntityRenderDispatcher().getRenderer(entity);
        return renderer != null && renderer.shouldRenderOffScreen();
    }
}
