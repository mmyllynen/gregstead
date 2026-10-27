package com.lowdragmc.lowdraglib.gui.widget;

import com.lowdragmc.lowdraglib.client.scene.*;
import com.lowdragmc.lowdraglib.utils.virtuallevel.TrackedDummyWorld;
import net.minecraft.world.level.Level;
import org.joml.Vector3f;
import java.util.HashSet;

final class ClientSceneWidgetSupport {
    private ClientSceneWidgetSupport() {}
    static net.minecraft.world.item.ItemStack hoverStack(SceneWidget widget, net.minecraft.world.level.block.state.BlockState state) {
        return state.getBlock().getCloneItemStack(widget.getDummyWorld(), widget.hoverPosFace.pos(), state,
                false, net.minecraft.client.Minecraft.getInstance().player);
    }
    static void create(SceneWidget widget, Level world, boolean useFBOSceneRenderer) {
        if (world == null) return;
        widget.core = new HashSet<>();
        widget.dummyWorld = new TrackedDummyWorld(world);
        widget.dummyWorld.setRenderFilter(pos -> widget.renderer.renderedBlocksMap.keySet().stream().anyMatch(c -> c.contains(pos)));
        if (widget.renderer != null) {
            widget.renderer.deleteCacheBuffer();
        }
        if (useFBOSceneRenderer) {
            widget.renderer = new FBOWorldSceneRenderer(widget.dummyWorld,1080, 1080);
        } else {
            // GUI extraction defers composition: a main-target draw here would be overwritten by the GUI pass.
            widget.renderer = new FBOWorldSceneRenderer(widget.dummyWorld, 1, 1).matchGuiResolution();
        }
        widget.center = new Vector3f(0, 0, 0);
        widget.renderer.useOrtho(widget.useOrtho);
        widget.renderer.setOnLookingAt(ray -> {});
        widget.renderer.setBeforeBatchEnd(widget::renderBeforeBatchEnd);
        widget.renderer.setAfterWorldRender(widget::renderBlockOverLay);
        if (widget.beforeWorldRender != null) {
            widget.renderer.setBeforeWorldRender(s -> widget.beforeWorldRender.accept(widget));
        }
        widget.renderer.setCameraLookAt(widget.center, widget.camZoom(), Math.toRadians(widget.rotationPitch), Math.toRadians(widget.rotationYaw));
        widget.renderer.useCacheBuffer(widget.useCache);
        widget.renderer.setParticleManager(widget.createParticleManager());
        widget.clickPosFace = null;
        widget.hoverPosFace = null;
        widget.hoverItem = null;
        widget.selectedPosFace = null;
    }
}
