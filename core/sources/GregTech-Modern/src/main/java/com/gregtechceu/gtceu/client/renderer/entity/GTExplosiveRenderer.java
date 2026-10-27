package com.gregtechceu.gtceu.client.renderer.entity;

import com.gregtechceu.gtceu.common.entity.GTExplosiveEntity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockModelResolver;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.TntMinecartRenderer;
import net.minecraft.client.renderer.entity.TntRenderer;
import net.minecraft.client.renderer.entity.state.TntRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;

public class GTExplosiveRenderer<T extends GTExplosiveEntity>
        extends EntityRenderer<T, GTExplosiveRenderer.State> {

    private final BlockModelResolver blockModelResolver;

    public GTExplosiveRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.5F;
        this.blockModelResolver = context.getBlockModelResolver();
    }

    public static class State extends TntRenderState {
        public boolean whiteFlash;
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(T entity, State state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        int fuse = entity.getFuse();
        state.fuseRemainingInTicks = fuse - partialTicks + 1.0F;
        state.whiteFlash = fuse / 5 % 2 == 0;
        blockModelResolver.update(state.blockState, entity.getExplosiveState(), TntRenderer.BLOCK_DISPLAY_CONTEXT);
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        poseStack.pushPose();
        poseStack.translate(0.0F, 0.5F, 0.0F);
        if (state.fuseRemainingInTicks < 10.0F) {
            float scale = 1.0F + TntRenderer.getSwellAmount(state.fuseRemainingInTicks);
            poseStack.scale(scale, scale, scale);
        }
        poseStack.rotateDegrees(Axis.YP, -90.0F);
        poseStack.translate(-0.5F, -0.5F, 0.5F);
        poseStack.rotateDegrees(Axis.YP, 90.0F);
        if (!state.blockState.isEmpty()) {
            TntMinecartRenderer.submitWhiteSolidBlock(state.blockState, poseStack, collector,
                    state.lightCoords, state.whiteFlash, state.outlineColor);
        }
        poseStack.popPose();
        super.submit(state, poseStack, collector, camera);
    }
}
