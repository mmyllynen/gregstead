package io.github.altarofabundance.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.altarofabundance.PedestalBlockEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public final class PedestalRenderer implements BlockEntityRenderer<PedestalBlockEntity, PedestalRenderer.State> {
    private final ItemModelResolver resolver;

    public static final class State extends BlockEntityRenderState {
        final ItemStackRenderState item = new ItemStackRenderState();
        float angle;
        float bob;
    }

    public PedestalRenderer(BlockEntityRendererProvider.Context context) {
        resolver = context.itemModelResolver();
    }

    @Override
    public State createRenderState() { return new State(); }

    @Override
    public void extractRenderState(PedestalBlockEntity entity, State state, float partialTick, Vec3 camera,
                                  ModelFeatureRenderer.@Nullable CrumblingOverlay overlay) {
        BlockEntityRenderer.super.extractRenderState(entity, state, partialTick, camera, overlay);
        float time = entity.getLevel() == null ? 0 : entity.getLevel().getGameTime() + partialTick;
        state.angle = (time * 2) % 360;
        state.bob = (float) Math.sin(time / 12) * 0.035F;
        resolver.updateForTopItem(state.item, entity.displayedItem(), ItemDisplayContext.GROUND,
            entity.getLevel(), null, (int) entity.getBlockPos().asLong());
    }

    @Override
    public void submit(State state, PoseStack poses, SubmitNodeCollector collector, CameraRenderState camera) {
        if (state.item.isEmpty()) return;
        poses.pushPose();
        poses.translate(0.5, 0.95 + state.bob, 0.5);
        poses.rotateDegrees(Axis.YP, state.angle);
        poses.scale(0.8F, 0.8F, 0.8F);
        state.item.submit(poses, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
        poses.popPose();
    }
}
