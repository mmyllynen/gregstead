package com.gregtechceu.gtceu.client.renderer.placeholder;

import com.gregtechceu.gtceu.api.item.IComponentItem;
import com.gregtechceu.gtceu.api.item.component.IItemComponent;
import com.gregtechceu.gtceu.api.item.component.IMonitorModuleItem;
import com.gregtechceu.gtceu.api.placeholder.IPlaceholderRenderer;
import com.gregtechceu.gtceu.common.machine.multiblock.electric.CentralMonitorMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.electric.monitor.MonitorGroup;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

import com.mojang.blaze3d.vertex.PoseStack;

public class ModulePlaceholderRenderer implements IPlaceholderRenderer {

    @Override
    public com.gregtechceu.gtceu.client.model.machine.NativeMachineRenderSubmission extract(CentralMonitorMachine machine, MonitorGroup group, float partialTick, PoseStack poseStack, CompoundTag tag) {
        var captures=new java.util.ArrayList<com.gregtechceu.gtceu.client.model.machine.NativeMachineRenderSubmission>();
        ItemStack stack = ItemStack.CODEC.parse(machine.getLevel().registryAccess().createSerializationContext(net.minecraft.nbt.NbtOps.INSTANCE),tag).result().orElse(ItemStack.EMPTY);
        if (stack.getItem() instanceof IComponentItem componentItem) {
            for (IItemComponent component : componentItem.getComponents()) {
                if (component instanceof IMonitorModuleItem module) {
                    captures.add(module.getRenderer(stack).extract(machine,group,partialTick,poseStack));
                }
            }
        }
        var prepared=java.util.List.copyOf(captures);
        return (pose,collector)->{for(var capture:prepared)capture.submit(pose,collector);};
    }
}
