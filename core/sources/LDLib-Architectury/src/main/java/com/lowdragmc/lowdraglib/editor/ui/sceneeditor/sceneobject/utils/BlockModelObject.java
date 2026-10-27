package com.lowdragmc.lowdraglib.editor.ui.sceneeditor.sceneobject.utils;

import com.lowdragmc.lowdraglib.editor.ui.sceneeditor.sceneobject.ISceneRendering;
import com.lowdragmc.lowdraglib.editor.ui.sceneeditor.sceneobject.SceneObject;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import com.lowdragmc.lowdraglib.client.scene.NativeSceneBuffers;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class BlockModelObject extends SceneObject implements ISceneRendering {
    public BlockState blockState = Blocks.STONE.defaultBlockState();

    public BlockModelObject() {
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void drawInternal(PoseStack poseStack, NativeSceneBuffers bufferSource, float partialTicks) {
        poseStack.translate(-0.5, -0.5, -0.5);
        bufferSource.submitBlock(blockState,poseStack);
    }
}
