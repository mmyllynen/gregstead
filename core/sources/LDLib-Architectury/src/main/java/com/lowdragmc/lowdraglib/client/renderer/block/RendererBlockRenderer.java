package com.lowdragmc.lowdraglib.client.renderer.block;

import com.lowdragmc.lowdraglib.client.renderer.IRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import java.util.Set;
import net.neoforged.neoforge.model.data.ModelData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

class RendererBlockRenderer implements IRenderer {

    @Override public com.lowdragmc.lowdraglib.client.renderer.NativeDelegatedBlockEntity.Submission extractBlockEntity(
            BlockEntity entity,float partialTicks,net.minecraft.world.phys.Vec3 camera,
            net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay breaking) {
        return getMachine(entity).map(machine -> machine.getRenderer().extractBlockEntity(entity,partialTicks,camera,breaking)).orElse(null);
    }

    public Optional<RendererBlockEntity> getMachine(@Nullable BlockEntity blockEntity) {
        return Optional.ofNullable(blockEntity).filter(RendererBlockEntity.class::isInstance).map(RendererBlockEntity.class::cast);
    }

    public Optional<RendererBlockEntity> getMachine(@Nullable BlockAndTintGetter level, @Nullable BlockPos pos) {
        if (level == null || pos == null)
            return Optional.empty();
        return getMachine(level.getBlockEntity(pos));
    }



    @Override
    @OnlyIn(Dist.CLIENT)
    public List<BakedQuad> renderModel(@Nullable BlockAndTintGetter level, @Nullable BlockPos pos, @Nullable BlockState state, @Nullable Direction side, RandomSource rand, ModelData data, @Nullable ChunkSectionLayer renderType) {
        return getMachine(level, pos)
                .map(machine -> machine.getRenderer().renderModel(level, pos, state, side, rand, data, renderType))
                .orElseGet(Collections::emptyList);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public Set<ChunkSectionLayer> getRenderTypes(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource rand, ModelData modelData) {
        return getMachine(level, pos)
                .map(machine -> machine.getRenderer().getRenderTypes(level, pos, state, rand, modelData))
                .orElseGet(() -> IRenderer.super.getRenderTypes(level, pos, state, rand, modelData));
    }

    @Override
    @NotNull
    @OnlyIn(Dist.CLIENT)
    public TextureAtlasSprite getParticleTexture(@Nullable BlockAndTintGetter level, @Nullable BlockPos pos, ModelData modelData) {
        return getMachine(level, pos)
                .map(machine -> machine.getRenderer().getParticleTexture(level, pos, modelData))
                .orElseGet(() -> IRenderer.super.getParticleTexture(level, pos, modelData));
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean hasTESR(BlockEntity blockEntity) {
        return getMachine(blockEntity).map(machine -> machine.getRenderer().hasTESR(blockEntity)).orElse(false);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean isGlobalRenderer(BlockEntity blockEntity) {
        return getMachine(blockEntity).map(machine -> machine.getRenderer().isGlobalRenderer(blockEntity)).orElse(false);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean shouldRender(BlockEntity blockEntity, Vec3 cameraPos) {
        return getMachine(blockEntity).map(machine -> machine.getRenderer().shouldRender(blockEntity, cameraPos)).orElseGet(() -> IRenderer.super.shouldRender(blockEntity, cameraPos));
    }


}
