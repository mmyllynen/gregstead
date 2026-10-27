package com.gregtechceu.gtceu.client.renderer.machine.impl;

import com.gregtechceu.gtceu.api.machine.feature.multiblock.IFluidRenderMulti;
import com.gregtechceu.gtceu.api.pattern.util.RelativeDirection;
import com.gregtechceu.gtceu.client.renderer.block.FluidBlockRenderer;
import com.gregtechceu.gtceu.client.renderer.machine.DynamicRender;
import com.gregtechceu.gtceu.client.renderer.machine.DynamicRenderType;
import com.gregtechceu.gtceu.client.util.RenderUtil;
import com.gregtechceu.gtceu.config.ConfigHolder;

import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.AABB;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class FluidAreaRender extends DynamicRender<IFluidRenderMulti, FluidAreaRender>
        implements com.gregtechceu.gtceu.client.model.machine.NativeMachineRenderExtractor<IFluidRenderMulti> {

    public static final List<RelativeDirection> DEFAULT_FACES = Collections.singletonList(RelativeDirection.UP);

    // spotless:off
    public static final MapCodec<FluidAreaRender> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            FluidBlockRenderer.CODEC.forGetter(FluidAreaRender::getFluidBlockRenderer),
            BuiltInRegistries.FLUID.byNameCodec().optionalFieldOf("fixed_fluid").forGetter(FluidAreaRender::getFixedFluid),
            RelativeDirection.CODEC.listOf().optionalFieldOf("drawn_faces", DEFAULT_FACES).forGetter(FluidAreaRender::getDrawFaces)
    ).apply(instance, FluidAreaRender::new));
    public static final DynamicRenderType<IFluidRenderMulti, FluidAreaRender> TYPE = new DynamicRenderType<>(FluidAreaRender.CODEC);
    // spotless:on

    @Getter
    private final FluidBlockRenderer fluidBlockRenderer;
    private final boolean fixedFluid;
    @Getter
    private final List<RelativeDirection> drawFaces;

    private @Nullable Fluid cachedFluid;
    private @Nullable Identifier cachedRecipe;

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public FluidAreaRender(FluidBlockRenderer fluidBlockRenderer,
                           Optional<Fluid> fixedFluid, List<RelativeDirection> drawFaces) {
        this.fluidBlockRenderer = fluidBlockRenderer;
        if (fixedFluid.isPresent()) {
            this.fixedFluid = true;
            this.cachedFluid = fixedFluid.get();
        } else {
            this.fixedFluid = false;
        }
        this.drawFaces = drawFaces.isEmpty() ? DEFAULT_FACES : drawFaces;
    }

    @Override
    public DynamicRenderType<IFluidRenderMulti, FluidAreaRender> getType() {
        return TYPE;
    }

    @Override
    public int getViewDistance() {
        return 32;
    }

    @Override
    public com.gregtechceu.gtceu.client.model.machine.NativeMachineRenderSubmission extractSubmission(IFluidRenderMulti machine,float partialTick) {
        if (!ConfigHolder.INSTANCE.client.renderer.renderFluids) return (pose,collector)->{};
        if (!machine.self().isFormed() || machine.getFluidOffsets() == null) {
            return (pose,collector)->{};
        }
        if (!fixedFluid) {
            var lastRecipe = machine.getRecipeLogic().getLastRecipe();
            if (lastRecipe == null) {
                cachedRecipe = null;
                cachedFluid = null;
            } else if (machine.self().getOffsetTimer() % 20 == 0 || !java.util.Objects.equals(lastRecipe.id, cachedRecipe)) {
                cachedRecipe = lastRecipe.id;
                if (machine.isActive()) {
                    cachedFluid = RenderUtil.getRecipeFluidToRender(lastRecipe);
                } else {
                    cachedFluid = null;
                }
            }
        }
        if (cachedFluid == null) {
            return (pose,collector)->{};
        }

        var capture=new com.gregtechceu.gtceu.client.renderer.machine.NativeVertexCapture();
        var origin=machine.self().getBlockPos();
        var offsets=java.util.List.copyOf(machine.getFluidOffsets());
        for(RelativeDirection face:drawFaces) {
            var direction=face.getRelative(machine.self().getFrontFacing(),machine.self().getUpwardsFacing(),machine.self().isFlipped());
            if(direction.getAxis()!=Direction.Axis.Y)direction=direction.getOpposite();
            fluidBlockRenderer.drawPlane(direction,offsets,new org.joml.Matrix4f(),capture,cachedFluid,
                    RenderUtil.FluidTextureType.STILL,net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY,origin);
        }
        return capture.freeze(net.minecraft.client.renderer.rendertype.RenderTypes.entityTranslucent(
                net.minecraft.client.renderer.texture.TextureAtlas.LOCATION_BLOCKS));
    }



    private Optional<Fluid> getFixedFluid() {
        if (fixedFluid) return Optional.ofNullable(cachedFluid);
        else return Optional.empty();
    }

    @Override
    public boolean shouldRenderOffScreen(IFluidRenderMulti machine) {
        return true;
    }

    @Override
    public AABB getRenderBoundingBox(IFluidRenderMulti machine) {
        AABB box = super.getRenderBoundingBox(machine);
        var offsets = machine.getFluidOffsets();
        for (var offset : offsets) {
            box = box.minmax(new AABB(offset));
        }
        return box.inflate(getViewDistance());
    }
}
