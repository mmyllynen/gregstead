package com.gregtechceu.gtceu.client.renderer.machine.impl;

import com.gregtechceu.gtceu.api.item.MetaMachineItem;
import com.gregtechceu.gtceu.client.renderer.machine.DynamicRender;
import com.gregtechceu.gtceu.client.renderer.machine.DynamicRenderType;
import com.gregtechceu.gtceu.client.util.RenderUtil;
import com.gregtechceu.gtceu.common.data.GTMachines;
import com.gregtechceu.gtceu.common.data.item.GTDataComponents;
import com.gregtechceu.gtceu.common.machine.storage.CreativeTankMachine;
import com.gregtechceu.gtceu.common.machine.storage.QuantumTankMachine;

import net.minecraft.core.Direction;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.MapCodec;


import javax.annotation.Nullable;

import static com.gregtechceu.gtceu.client.renderer.machine.impl.QuantumChestItemRender.*;
import static com.gregtechceu.gtceu.common.machine.storage.QuantumTankMachine.TANK_CAPACITY;

public class QuantumTankFluidRender extends DynamicRender<QuantumTankMachine, QuantumTankFluidRender>
        implements com.gregtechceu.gtceu.client.model.machine.NativeMachineRenderExtractor<QuantumTankMachine> {

    // spotless:off
    public static final MapCodec<QuantumTankFluidRender> CODEC = MapCodec.unit(QuantumTankFluidRender::new);
    public static final DynamicRenderType<QuantumTankMachine, QuantumTankFluidRender> TYPE = new DynamicRenderType<>(QuantumTankFluidRender.CODEC);
    // spotless:on

    private static final float MIN = 0.16f;
    private static final float MAX = 0.84f;

    private static @Nullable Item CREATIVE_FLUID_ITEM = null;

    public QuantumTankFluidRender() {}

    @Override
    public DynamicRenderType<QuantumTankMachine, QuantumTankFluidRender> getType() {
        return TYPE;
    }

    @Override
    public com.gregtechceu.gtceu.client.model.machine.NativeMachineRenderSubmission extractSubmission(
            QuantumTankMachine machine,float partialTick) {
        FluidStack chosen=machine.getStored().isEmpty()?machine.getLockedFluid():machine.getStored();
        if(chosen.isEmpty())return (pose,collector)->{};
        var fluid=chosen.copy();
        var front=machine.getFrontFacing();
        var shape=NativeQuantumTankShape.create(front,fluid.getFluid().getFluidType().isLighterThanAir(),
                machine.getStoredAmount(),machine.getMaxAmount(),machine instanceof CreativeTankMachine);
        var sprite=RenderUtil.FluidTextureType.STILL.map(fluid);
        int color=com.lowdragmc.lowdraglib.transfer.client.FluidVisuals.color(fluid)|0xff000000;
        var geometry=NativeTankVertices.capture(shape,sprite,color,net.minecraft.util.LightCoordsUtil.FULL_BRIGHT,
                net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY);
        var rotation=new PoseStack();setupModelRotation(machine,rotation);
        var matrix=new org.joml.Matrix4f(rotation.last().pose());
        var font=net.minecraft.client.Minecraft.getInstance().font;
        var amount=new NativeQuantumChestDisplay(null,machine instanceof CreativeTankMachine?"∞":machine.getStoredAmount()<=0?"*":
                com.gregtechceu.gtceu.utils.FormattingUtil.formatNumberReadable(machine.getStoredAmount(),false),
                machine instanceof CreativeTankMachine,front,0);
        return (pose,collector)->{
            pose.pushPose();
            try {
                pose.mulPose(matrix);
                geometry.submit(pose,collector,net.minecraft.client.renderer.rendertype.RenderTypes.entityTranslucent(
                        net.minecraft.client.renderer.texture.TextureAtlas.LOCATION_BLOCKS));
                amount.submitAmount(pose,collector,font);
            } finally {pose.popPose();}
        };
    }

    @Override
    public java.util.Optional<com.gregtechceu.gtceu.client.model.machine.NativeMachineRenderSubmission> extractItem(ItemStack stack,ItemDisplayContext context) {
        var content=stack.get(GTDataComponents.LARGE_FLUID_CONTENT);
        if(content==null || content.stored().isEmpty())return java.util.Optional.empty();
        if(CREATIVE_FLUID_ITEM==null)CREATIVE_FLUID_ITEM=GTMachines.CREATIVE_FLUID.getItem();
        var fluid=content.stored().copy();long count=content.amount()==0?fluid.getAmount():content.amount();
        long capacity=stack.getItem() instanceof MetaMachineItem item?TANK_CAPACITY.getLong(item.getDefinition()):0;
        boolean creative=stack.is(CREATIVE_FLUID_ITEM);
        var shape=NativeQuantumTankShape.create(Direction.NORTH,fluid.getFluid().getFluidType().isLighterThanAir(),count,capacity,creative);
        var geometry=NativeTankVertices.capture(shape,RenderUtil.FluidTextureType.STILL.map(fluid),
                com.lowdragmc.lowdraglib.transfer.client.FluidVisuals.color(fluid)|0xff000000,net.minecraft.util.LightCoordsUtil.FULL_BRIGHT,
                net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY);
        var text=new NativeQuantumChestDisplay(null,creative?"∞":count<=0?"*":com.gregtechceu.gtceu.utils.FormattingUtil.formatNumberReadable(count,false),creative,Direction.NORTH,0);
        var font=net.minecraft.client.Minecraft.getInstance().font;
        return java.util.Optional.of((pose,collector)->{geometry.submit(pose,collector,net.minecraft.client.renderer.rendertype.RenderTypes.entityTranslucent(
                net.minecraft.client.renderer.texture.TextureAtlas.LOCATION_BLOCKS));text.submitAmount(pose,collector,font);});
    }
}
