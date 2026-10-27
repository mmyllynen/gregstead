package com.lowdragmc.lowdraglib.client.utils;

import com.mojang.blaze3d.vertex.*;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Stack;

@OnlyIn(Dist.CLIENT)
public class RenderUtils {
    /***
     * used to render pixels in stencil mask. (e.g. Restrict rendering results to be displayed only in Monitor Screens)
     * if you want to do the similar things in Gui(2D) not World(3D)
     * that you don't need to draw mask to build a rect mask easily.
     * @param mask draw mask
     * @param renderInMask rendering in the mask
     * @param shouldRenderMask should mask be rendered too
     */
    public static void useStencil(Runnable mask, Runnable renderInMask, boolean shouldRenderMask) {
        GL11.glStencilMask(0xFF);
        GL11.glClearStencil(0);
        GL11.glClear(GL11.GL_STENCIL_BUFFER_BIT);
        GL11.glEnable(GL11.GL_STENCIL_TEST);

        GL11.glStencilFunc(GL11.GL_ALWAYS, 1, 0xFF);
        GL11.glStencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_REPLACE);

        if (!shouldRenderMask) {
            GL11.glColorMask(false, false, false, false);
            GL11.glDepthMask(false);
        }

        mask.run();

        if (!shouldRenderMask) {
            GL11.glColorMask(true, true, true, true);
            GL11.glDepthMask(true);
        }

        GL11.glStencilMask(0x00);
        GL11.glStencilFunc(GL11.GL_EQUAL, 1, 0xFF);
        GL11.glStencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_KEEP);

        renderInMask.run();

        GL11.glDisable(GL11.GL_STENCIL_TEST);
    }

    public static void renderBlockOverLay(com.lowdragmc.lowdraglib.client.scene.NativeSceneBuffers buffers,
                                          @Nonnull PoseStack poseStack, BlockPos pos, float r, float g, float b, float scale) {
        if (pos != null) com.lowdragmc.lowdraglib.client.scene.NativeSceneOverlays.selection(buffers, poseStack, pos, r, g, b, scale);
    }

    public static void renderCubeFace(PoseStack poseStack, VertexConsumer buffer, float minX, float minY, float minZ, float maxX, float maxY, float maxZ, float r, float g, float b, float a) {
        Matrix4f mat = poseStack.last().pose();
        buffer.addVertex(mat, minX, minY, minZ).setColor(r, g, b, a);
        buffer.addVertex(mat, minX, minY, maxZ).setColor(r, g, b, a);
        buffer.addVertex(mat, minX, maxY, maxZ).setColor(r, g, b, a);
        buffer.addVertex(mat, minX, maxY, minZ).setColor(r, g, b, a);

        buffer.addVertex(mat, maxX, minY, minZ).setColor(r, g, b, a);
        buffer.addVertex(mat, maxX, maxY, minZ).setColor(r, g, b, a);
        buffer.addVertex(mat, maxX, maxY, maxZ).setColor(r, g, b, a);
        buffer.addVertex(mat, maxX, minY, maxZ).setColor(r, g, b, a);

        buffer.addVertex(mat, minX, minY, minZ).setColor(r, g, b, a);
        buffer.addVertex(mat, maxX, minY, minZ).setColor(r, g, b, a);
        buffer.addVertex(mat, maxX, minY, maxZ).setColor(r, g, b, a);
        buffer.addVertex(mat, minX, minY, maxZ).setColor(r, g, b, a);

        buffer.addVertex(mat, minX, maxY, minZ).setColor(r, g, b, a);
        buffer.addVertex(mat, minX, maxY, maxZ).setColor(r, g, b, a);
        buffer.addVertex(mat, maxX, maxY, maxZ).setColor(r, g, b, a);
        buffer.addVertex(mat, maxX, maxY, minZ).setColor(r, g, b, a);

        buffer.addVertex(mat, minX, minY, minZ).setColor(r, g, b, a);
        buffer.addVertex(mat, minX, maxY, minZ).setColor(r, g, b, a);
        buffer.addVertex(mat, maxX, maxY, minZ).setColor(r, g, b, a);
        buffer.addVertex(mat, maxX, minY, minZ).setColor(r, g, b, a);

        buffer.addVertex(mat, minX, minY, maxZ).setColor(r, g, b, a);
        buffer.addVertex(mat, maxX, minY, maxZ).setColor(r, g, b, a);
        buffer.addVertex(mat, maxX, maxY, maxZ).setColor(r, g, b, a);
        buffer.addVertex(mat, minX, maxY, maxZ).setColor(r, g, b, a);
    }

    public static void moveToFace(PoseStack poseStack, double x, double y, double z, Direction face) {
        poseStack.translate(x + 0.5 + face.getStepX() * 0.5, y + 0.5 + face.getStepY() * 0.5, z + 0.5 + face.getStepZ() * 0.5);
    }

    public static void rotateToFace(PoseStack poseStack, Direction face, @Nullable Direction spin) {
        float angle = spin == Direction.EAST ? Mth.HALF_PI : spin == Direction.SOUTH ? Mth.PI : spin == Direction.WEST ? -Mth.HALF_PI : 0;
        switch (face) {
            case UP -> {
                poseStack.scale(1.0f, -1.0f, 1.0f);
                poseStack.rotate(new Quaternionf().rotateAxis(Mth.HALF_PI, new Vector3f(1, 0, 0)));
                poseStack.rotate(new Quaternionf().rotateAxis(angle, new Vector3f(0, 0, 1)));
            }
            case DOWN -> {
                poseStack.scale(1.0f, -1.0f, 1.0f);
                poseStack.rotate(new Quaternionf().rotateAxis(-Mth.HALF_PI, new Vector3f(1, 0, 0)));
                poseStack.rotate(new Quaternionf().rotateAxis(spin == Direction.EAST ? Mth.HALF_PI : spin == Direction.NORTH ? Mth.PI : spin == Direction.WEST ? -Mth.HALF_PI : 0, new Vector3f(0, 0, 1)));
            }
            case EAST -> {
                poseStack.scale(-1.0f, -1.0f, -1.0f);
                poseStack.rotate(new Quaternionf().rotateAxis(-Mth.HALF_PI, new Vector3f(0, 1, 0)));
                poseStack.rotate(new Quaternionf().rotateAxis(angle, new Vector3f(0, 0, 1)));
            }
            case WEST -> {
                poseStack.scale(-1.0f, -1.0f, -1.0f);
                poseStack.rotate(new Quaternionf().rotateAxis(Mth.HALF_PI, new Vector3f(0, 1, 0)));
                poseStack.rotate(new Quaternionf().rotateAxis(angle, new Vector3f(0, 0, 1)));
            }
            case NORTH -> {
                poseStack.scale(-1.0f, -1.0f, -1.0f);
                poseStack.rotate(new Quaternionf().rotateAxis(angle, new Vector3f(0, 0, 1)));
            }
            case SOUTH -> {
                poseStack.scale(-1.0f, -1.0f, -1.0f);
                poseStack.rotate(new Quaternionf().rotateAxis(Mth.PI, new Vector3f(0, 1, 0)));
                poseStack.rotate(new Quaternionf().rotateAxis(angle, new Vector3f(0, 0, 1)));
            }
            default -> {
            }
        }
    }
}
