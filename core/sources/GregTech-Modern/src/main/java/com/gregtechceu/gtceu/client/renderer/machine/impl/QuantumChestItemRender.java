package com.gregtechceu.gtceu.client.renderer.machine.impl;

import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.client.renderer.machine.DynamicRender;
import com.gregtechceu.gtceu.client.renderer.machine.DynamicRenderType;
import com.gregtechceu.gtceu.client.util.PoseStackExtensions;
import com.gregtechceu.gtceu.common.data.GTMachines;
import com.gregtechceu.gtceu.common.data.item.GTDataComponents;
import com.gregtechceu.gtceu.common.machine.storage.CreativeChestMachine;
import com.gregtechceu.gtceu.common.machine.storage.QuantumChestMachine;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import net.minecraft.client.Minecraft;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.MapCodec;
import lombok.experimental.ExtensionMethod;
import org.jetbrains.annotations.Nullable;

import static com.gregtechceu.gtceu.utils.GTMatrixUtils.*;

@ExtensionMethod(PoseStackExtensions.class)
public class QuantumChestItemRender extends DynamicRender<QuantumChestMachine, QuantumChestItemRender>
        implements com.gregtechceu.gtceu.client.model.machine.NativeMachineRenderExtractor<QuantumChestMachine> {

    // spotless:off
    public static final MapCodec<QuantumChestItemRender> CODEC = MapCodec.unit(QuantumChestItemRender::new);
    public static final DynamicRenderType<QuantumChestMachine, QuantumChestItemRender> TYPE = new DynamicRenderType<>(QuantumChestItemRender.CODEC);
    // spotless:on

    private static @Nullable Item CREATIVE_CHEST_ITEM = null;

    public QuantumChestItemRender() {}

    @Override
    public DynamicRenderType<QuantumChestMachine, QuantumChestItemRender> getType() {
        return TYPE;
    }

    @Override
    public com.gregtechceu.gtceu.client.model.machine.NativeMachineRenderSubmission extractSubmission(
            QuantumChestMachine machine, float partialTick) {
        var display = captureDisplay(machine, partialTick);
        var rotation = new PoseStack();
        setupModelRotation(machine, rotation);
        var matrix = new org.joml.Matrix4f(rotation.last().pose());
        var font = Minecraft.getInstance().font;
        return (pose, collector) -> {
            pose.pushPose();
            try { pose.mulPose(matrix); display.submit(pose, collector, font); }
            finally { pose.popPose(); }
        };
    }

    public NativeQuantumChestDisplay captureDisplay(QuantumChestMachine machine, float partialTick) {
        var client = Minecraft.getInstance();
        return NativeQuantumChestDisplay.capture(machine.getStored(), machine.getLockedItem(), machine.getStoredAmount(),
                machine instanceof CreativeChestMachine, machine.getFrontFacing(), machine.getLevel().getGameTime()+partialTick,
                stack -> {
                    var state = new net.minecraft.client.renderer.item.ItemStackRenderState();
                    new net.minecraft.client.renderer.item.ItemModelResolver(client.getModelManager()).updateForTopItem(
                            state, stack, ItemDisplayContext.FIXED, client.level, client.player,
                            Item.getId(stack.getItem())+stack.getDamageValue());
                    return state;
                }, amount -> FormattingUtil.formatNumberReadable(amount, false));
    }

    @Override
    public java.util.Optional<com.gregtechceu.gtceu.client.model.machine.NativeMachineRenderSubmission> extractItem(ItemStack stack,ItemDisplayContext context) {
        var content=stack.get(GTDataComponents.LARGE_ITEM_CONTENT);
        if(content==null)return java.util.Optional.empty();
        if(CREATIVE_CHEST_ITEM==null)CREATIVE_CHEST_ITEM=GTMachines.CREATIVE_ITEM.getItem();
        var client=Minecraft.getInstance();
        float tick=client.level==null?0:client.level.getGameTime()+client.getDeltaTracker().getGameTimeDeltaPartialTick(false);
        var display=NativeQuantumChestDisplay.capture(content.stored(),ItemStack.EMPTY,content.amount(),stack.is(CREATIVE_CHEST_ITEM),Direction.NORTH,tick,
                item->{var state=new net.minecraft.client.renderer.item.ItemStackRenderState();
                    new net.minecraft.client.renderer.item.ItemModelResolver(client.getModelManager()).updateForTopItem(state,item,ItemDisplayContext.FIXED,client.level,client.player,
                            Item.getId(item.getItem())+item.getDamageValue());return state;},amount->FormattingUtil.formatNumberReadable(amount,false));
        var font=client.font;
        return java.util.Optional.of((pose,collector)->{pose.pushPose();try{display.submit(pose,collector,font);}finally{pose.popPose();}});
    }

    public static void setupModelRotation(MetaMachine machine, PoseStack poseStack) {
        var frontFacing = machine.getFrontFacing();
        var upwardFacing = machine.getUpwardsFacing();

        poseStack.translate(0.5f, 0.5f, 0.5f);
        rotateMatrix(poseStack.last().pose(),
                upwardFacingAngle(upwardFacing) + (upwardFacing.getAxis() == Direction.Axis.X ? Mth.PI : 0),
                getDirectionAxis(frontFacing));
        poseStack.translate(-0.5f, -0.5f, -0.5f);
    }

}
