package com.gregtechceu.gtceu.client.renderer.machine.impl;

import com.gregtechceu.gtceu.api.capability.IMonitorComponent;
import com.gregtechceu.gtceu.api.item.ComponentItem;
import com.gregtechceu.gtceu.api.item.component.IItemComponent;
import com.gregtechceu.gtceu.api.item.component.IMonitorModuleItem;
import com.gregtechceu.gtceu.client.renderer.machine.DynamicRender;
import com.gregtechceu.gtceu.client.renderer.machine.DynamicRenderType;
import com.gregtechceu.gtceu.client.util.RenderUtil;
import com.gregtechceu.gtceu.common.machine.multiblock.electric.CentralMonitorMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.electric.monitor.MonitorGroup;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;

public class CentralMonitorRender extends DynamicRender<CentralMonitorMachine, CentralMonitorRender>
        implements com.gregtechceu.gtceu.client.model.machine.NativeMachineRenderExtractor<CentralMonitorMachine> {

    // spotless:off
    public static final Codec<CentralMonitorRender> CODEC = MapCodec.unit(CentralMonitorRender::new).codec();
    public static final DynamicRenderType<CentralMonitorMachine, CentralMonitorRender> TYPE = new DynamicRenderType<CentralMonitorMachine, CentralMonitorRender>(MapCodec.unit(CentralMonitorRender::new));
    // spotless:on
    private static final float SCREEN_OFFSET_Z = 0.01f;

    public CentralMonitorRender() {}

    @Override
    public DynamicRenderType<CentralMonitorMachine, CentralMonitorRender> getType() {
        return TYPE;
    }

    @Override
    public com.gregtechceu.gtceu.client.model.machine.NativeMachineRenderSubmission extractSubmission(CentralMonitorMachine machine,float partialTick) {
        var poseStack=new PoseStack();
        RenderUtil.moveToFace(poseStack,.5f,.5f,.5f,machine.getFrontFacing());
        RenderUtil.rotateToFace(poseStack,machine.getFrontFacing(),machine.getUpwardsFacing());
        poseStack.translate(-machine.getRightDist()-.5f,-machine.getUpDist()-.5f,SCREEN_OFFSET_Z);
        var captures=new java.util.ArrayList<com.gregtechceu.gtceu.client.model.machine.NativeMachineRenderSubmission>();
        if(machine.getRecipeLogic().isActive())for(MonitorGroup group:machine.getMonitorGroups()) {
            ItemStack stack=group.getItemStackHandler().getStackInSlot(0);
            if(stack.getItem() instanceof ComponentItem item)for(IItemComponent component:item.getComponents()) {
                if(component instanceof IMonitorModuleItem module) captures.add(module.getRenderer(stack).extract(machine,group,partialTick,poseStack));
            }
        }
        var prepared=java.util.List.copyOf(captures);
        return (pose,collector)->{for(var capture:prepared)capture.submit(pose,collector);};
    }

    @Override
    public boolean shouldRenderOffScreen(CentralMonitorMachine machine) {
        return true;
    }

    @Override
    public boolean shouldRender(CentralMonitorMachine machine, Vec3 cameraPos) {
        return machine.isFormed();
    }

    @Override
    public AABB getRenderBoundingBox(CentralMonitorMachine machine) {
        BlockPos pos = machine.getBlockPos();
        BoundingBox bounds = new BoundingBox(
                pos.getX() - 1, pos.getY() - 1, pos.getZ() - 1,
                pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1);

        for (int row = 0; row <= machine.getUpDist() + machine.getDownDist(); row++) {
            for (int col = 0; col <= machine.getLeftDist() + machine.getRightDist(); col++) {
                IMonitorComponent component = machine.getComponent(row, col);
                if (component != null && component.isMonitor()) {
                    // noinspection deprecation
                    bounds.encapsulate(component.getBlockPos());
                }
            }
        }
        return AABB.of(bounds);
    }
}
