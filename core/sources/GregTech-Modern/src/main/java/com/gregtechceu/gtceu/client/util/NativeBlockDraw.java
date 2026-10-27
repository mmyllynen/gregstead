package com.gregtechceu.gtceu.client.util;
import com.gregtechceu.gtceu.client.model.machine.NativeMachineRenderSubmission;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.*;
import net.minecraft.client.renderer.item.*;
/** Public block drawing prepares native geometry/item state before deferred submission. */
public final class NativeBlockDraw {
    private NativeBlockDraw() {}
    public static NativeMachineRenderSubmission extract(BlockAndTintGetter world,BlockPos pos,BlockState state) {
        var client=Minecraft.getInstance();
        if(state.getRenderShape()==net.minecraft.world.level.block.RenderShape.INVISIBLE && state.hasBlockEntity() && state.getBlock().asItem()!=Items.AIR) {
            var item=new ItemStack(state.getBlock());var prepared=new ItemStackRenderState();
            new ItemModelResolver(client.getModelManager()).updateForTopItem(prepared,item,ItemDisplayContext.NONE,client.level,client.player,0);
            int light=net.minecraft.util.LightCoordsUtil.getLightCoords(net.minecraft.util.LightCoordsUtil.BrightnessGetter.DEFAULT,world,state,pos);
            return (pose,collector)->prepared.submit(pose,collector,light,net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY,0);
        }
        if(state.getRenderShape()==net.minecraft.world.level.block.RenderShape.INVISIBLE)return (pose,collector)->{};
        var model=client.getModelManager().getBlockStateModelSet().get(state);
        var geometry=com.lowdragmc.lowdraglib.client.scene.NativeSceneBlockGeometry.extract(world,pos,state,model,false,false,null);
        return (pose,collector)->{pose.pushPose();try{pose.translate(-pos.getX(),-pos.getY(),-pos.getZ());geometry.submit(pose,collector);}finally{pose.popPose();}};
    }
}
