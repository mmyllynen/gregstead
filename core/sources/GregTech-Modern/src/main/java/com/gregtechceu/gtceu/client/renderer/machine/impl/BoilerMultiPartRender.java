package com.gregtechceu.gtceu.client.renderer.machine.impl;

import com.gregtechceu.gtceu.api.block.property.GTBlockStateProperties;
import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.pattern.util.RelativeDirection;
import com.gregtechceu.gtceu.client.model.machine.IControllerModelRenderer;
import com.gregtechceu.gtceu.client.renderer.machine.DynamicRender;
import com.gregtechceu.gtceu.client.renderer.machine.DynamicRenderType;
import com.gregtechceu.gtceu.common.block.BoilerFireboxType;
import com.gregtechceu.gtceu.common.data.GTBlocks;

import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.model.data.ModelData;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Supplier;

public class BoilerMultiPartRender extends DynamicRender<MultiblockControllerMachine, BoilerMultiPartRender>
                                   implements IControllerModelRenderer {

    // spotless:off
    public static final MapCodec<BoilerMultiPartRender> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            BlockState.CODEC.fieldOf("firebox_idle").forGetter(BoilerMultiPartRender::getFireboxIdle),
            BlockState.CODEC.fieldOf("firebox_active").forGetter(BoilerMultiPartRender::getFireboxActive),
            BlockState.CODEC.fieldOf("casing_block").forGetter(BoilerMultiPartRender::getCasing)
    ).apply(instance, BoilerMultiPartRender::new));
    public static final DynamicRenderType<MultiblockControllerMachine, BoilerMultiPartRender> TYPE = new DynamicRenderType<>(BoilerMultiPartRender.CODEC);
    // spotless:on

    @Getter
    private final BlockState fireboxIdle, fireboxActive;
    @Getter
    private final BlockState casing;


    public BoilerMultiPartRender(BoilerFireboxType fireboxType, Supplier<? extends Block> casingBlock) {
        this(GTBlocks.ALL_FIREBOXES.get(fireboxType).getDefaultState(),
                GTBlocks.ALL_FIREBOXES.get(fireboxType).getDefaultState().setValue(GTBlockStateProperties.ACTIVE, true),
                casingBlock.get().defaultBlockState());
    }

    public BoilerMultiPartRender(BlockState fireboxIdle, BlockState fireboxActive, BlockState casing) {
        this.fireboxIdle = fireboxIdle;
        this.fireboxActive = fireboxActive;
        this.casing = casing;
    }

    @Override
    public DynamicRenderType<MultiblockControllerMachine, BoilerMultiPartRender> getType() {
        return TYPE;
    }



    @Override
    public com.gregtechceu.gtceu.client.model.machine.NativeMachineRenderSubmission extractSubmission(MultiblockControllerMachine machine,float partialTick) {
        return (pose,collector)->{};
    }

    @Override
    public boolean shouldRender(MultiblockControllerMachine machine, Vec3 cameraPos) {
        return false;
    }

    @Override
    public boolean isBlockEntityRenderer() {
        return false;
    }

    @SuppressWarnings("DataFlowIssue")
    @Override
    @OnlyIn(Dist.CLIENT)
    public void renderPartModel(List<BakedQuad> quads, MultiblockControllerMachine controller, IMultiPart part,
                                Direction frontFacing, @Nullable Direction side, RandomSource rand,
                                @NotNull ModelData modelData, @Nullable RenderType renderType) {
        BlockPos partPos = part.self().getBlockPos();

        BlockState selected = selectPartState(controller, partPos);
        var model = net.minecraft.client.Minecraft.getInstance().getModelManager().getBlockStateModelSet().get(selected);
        emitQuads(quads, model, (BlockAndTintGetter) controller.getLevel(), partPos, selected, side, rand, modelData, renderType);
    }

    public BlockState selectPartState(MultiblockControllerMachine controller, BlockPos partPos) {
        Direction relativeDown = RelativeDirection.DOWN.getRelative(controller.getFrontFacing(),
                controller.getUpwardsFacing(), controller.isFlipped());
        return NativeBoilerPartState.select(controller.getBlockPos(), partPos, relativeDown,
                controller instanceof IRecipeLogicMachine machine && machine.getRecipeLogic().isWorking(),
                fireboxIdle, fireboxActive, casing);
    }

    private static void emitQuads(List<BakedQuad> quads, @Nullable BlockStateModel model,
                                  BlockAndTintGetter level, BlockPos pos, BlockState state,
                                  @Nullable Direction side, RandomSource rand,
                                  ModelData modelData, @Nullable RenderType renderType) {
        if (model == null) return;
        var parts = new java.util.ArrayList<BlockStateModelPart>();
        model.collectParts(level, pos, state, rand, parts);
        for (var part : parts) quads.addAll(part.getQuads(side));
    }
}
