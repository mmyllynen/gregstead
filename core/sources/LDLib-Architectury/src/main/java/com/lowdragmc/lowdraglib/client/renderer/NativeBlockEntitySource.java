package com.lowdragmc.lowdraglib.client.renderer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.*;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
public interface NativeBlockEntitySource {
    default boolean hasTESR(BlockEntity entity) { return false; }
    default boolean isGlobalRenderer(BlockEntity entity) { return false; }
    default int getViewDistance() { return 64; }
    default boolean shouldRender(BlockEntity entity, Vec3 camera) { return Vec3.atCenterOf(entity.getBlockPos()).closerThan(camera,getViewDistance()); }
    default AABB getRenderBoundingBox(BlockEntity entity) { return new AABB(entity.getBlockPos()); }
    default NativeDelegatedBlockEntity.Submission extractBlockEntity(BlockEntity entity,float partialTicks,Vec3 camera,
            ModelFeatureRenderer.CrumblingOverlay breaking) {
        if(hasTESR(entity)) throw new UnsupportedOperationException("Renderer BER extraction has not been ported");
        return null;
    }
}
