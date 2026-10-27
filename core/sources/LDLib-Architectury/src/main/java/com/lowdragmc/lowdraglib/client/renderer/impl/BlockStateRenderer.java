package com.lowdragmc.lowdraglib.client.renderer.impl;

import com.lowdragmc.lowdraglib.LDLib;
import com.lowdragmc.lowdraglib.Platform;
import com.lowdragmc.lowdraglib.client.renderer.IRenderer;
import com.lowdragmc.lowdraglib.utils.data.BlockInfo;
import com.lowdragmc.lowdraglib.utils.virtuallevel.FacadeBlockWorld;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import lombok.Getter;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import java.util.Set;
import net.neoforged.neoforge.model.data.ModelData;
import net.minecraft.util.TriState;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

//@LDLRegisterClient(name = "block_model", group = "renderer")
public class BlockStateRenderer implements IRenderer {

    @Getter
    protected BlockInfo blockInfo;

    protected BlockStateRenderer() {
        blockInfo = null;
    }

    public BlockStateRenderer(BlockState state) {
        this(BlockInfo.fromBlockState(state == null ? Blocks.BARRIER.defaultBlockState() : state));
    }

    public BlockStateRenderer(BlockInfo blockInfo) {
        this.blockInfo = blockInfo == null ? new BlockInfo(Blocks.BARRIER) : blockInfo;
        if (LDLib.isClient()) {
            registerEvent();
        }
    }

    public BlockState getState(@Nullable BlockState blockState) {
        BlockState state = getBlockInfo().getBlockState();
        if (blockState == null) return state;
        Direction facing = Direction.NORTH;
        if (blockState.hasProperty(BlockStateProperties.FACING)) {
            facing = blockState.getValue(BlockStateProperties.FACING);
        } else if (blockState.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
            facing = blockState.getValue(BlockStateProperties.HORIZONTAL_FACING);
        }
        try {
            switch (facing) {
                case EAST -> state = state.rotate(Rotation.CLOCKWISE_90);
                case WEST -> state = state.rotate(Rotation.COUNTERCLOCKWISE_90);
                case SOUTH -> state = state.rotate(Rotation.CLOCKWISE_180);
            }
        } catch (Exception ignore) {

        }
        return state;
    }

    public net.minecraft.client.renderer.item.ItemModel getNativeItemModel(ItemStack stack) {
        return new com.lowdragmc.lowdraglib.client.model.NativeRepresentedItemModel(() -> getBlockInfo().getItemStackForm());
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    @Nonnull
    public TextureAtlasSprite getParticleTexture(@Nullable BlockAndTintGetter level, @Nullable BlockPos pos, ModelData modelData) {
        var item = com.lowdragmc.lowdraglib.client.model.NativeItemProperties.extract(getBlockInfo().getItemStackForm());
        return item.isEmpty() ? IRenderer.super.getParticleTexture(level,pos,modelData)
                : item.pickParticleMaterial(RandomSource.create(0)).sprite();
    }



    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean useBlockLight(ItemStack stack) {
        return com.lowdragmc.lowdraglib.client.model.NativeItemProperties.extract(getBlockInfo().getItemStackForm()).usesBlockLight();
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public List<BakedQuad> renderModel(@Nullable BlockAndTintGetter level, @Nullable BlockPos pos, @Nullable BlockState state, @Nullable Direction side, RandomSource rand, ModelData data, @Nullable ChunkSectionLayer renderType) {
        return com.lowdragmc.lowdraglib.client.model.NativeBlockModelQueries.quads(
                getState(state), level, pos, data, side, rand, renderType);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public Set<ChunkSectionLayer> getRenderTypes(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource rand, ModelData modelData) {
        return com.lowdragmc.lowdraglib.client.model.NativeQuadLayers.layers(
                com.lowdragmc.lowdraglib.client.model.NativeBlockModelQueries.parts(
                        getState(state), level, pos, modelData, rand));
    }

    @OnlyIn(Dist.CLIENT)
    @Nullable
    public BlockEntity getBlockEntity(net.minecraft.world.level.BlockGetter world, BlockPos pos) {
        BlockInfo blockInfo = getBlockInfo();
        BlockEntity tile = blockInfo.getBlockEntity(pos, Platform.getFrozenRegistry());
        if (tile != null && world instanceof Level) {
            try {
                var state = getState(world.getBlockState(pos));
                tile.setBlockState(state);
                tile.setLevel(new FacadeBlockWorld((Level) world, pos, state, tile));
            } catch (Throwable throwable) {
                blockInfo.setHasBlockEntity(false);
            }
        }
        return tile;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean hasTESR(BlockEntity tileEntity) {
        if (!getBlockInfo().getBlockState().getFluidState().isEmpty()) {
            return true;
        }
        tileEntity = getBlockEntity(tileEntity.getLevel(), tileEntity.getBlockPos());
        if (tileEntity == null) {
            return false;
        }
        return Minecraft.getInstance().getBlockEntityRenderDispatcher().getRenderer(tileEntity) != null;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean isGlobalRenderer(BlockEntity tileEntity) {
        tileEntity = getBlockEntity(tileEntity.getLevel(), tileEntity.getBlockPos());
        if (tileEntity == null) return false;
        return com.lowdragmc.lowdraglib.client.renderer.NativeDelegatedBlockEntity.global(tileEntity);
    }

    @Override
    public com.lowdragmc.lowdraglib.client.renderer.NativeDelegatedBlockEntity.Submission extractBlockEntity(
            BlockEntity host,float partialTicks,net.minecraft.world.phys.Vec3 camera,
            net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay breaking) {
        var hasFluid = !getBlockInfo().getBlockState().getFluidState().isEmpty();
        if (hasFluid && !(host.getLevel() instanceof BlockAndTintGetter)) {
            throw new UnsupportedOperationException("Represented fluid requires a native tint world view");
        }
        var fluid = hasFluid ? extractRepresentedFluid((BlockAndTintGetter) host.getLevel(),host.getBlockPos()) : null;
        var ber = extractRepresentedBER(host,partialTicks,camera,breaking);
        return (pose,collector,view) -> {
            if(fluid!=null)fluid.submit(pose,collector);
            if(ber!=null)ber.submit(pose,collector,view);
        };
    }

    public com.lowdragmc.lowdraglib.client.renderer.NativeRepresentedFluid extractRepresentedFluid(
            BlockAndTintGetter level, BlockPos pos) {
        return com.lowdragmc.lowdraglib.client.renderer.NativeRepresentedFluid.extract(level,pos,getBlockInfo().getBlockState());
    }

    @Nullable
    public com.lowdragmc.lowdraglib.client.renderer.NativeDelegatedBlockEntity.Submission extractRepresentedBER(
            BlockEntity host, float partialTicks, net.minecraft.world.phys.Vec3 camera,
            net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay breaking) {
        var represented = getBlockEntity(host.getLevel(), host.getBlockPos());
        if (represented == null) return null;
        try {
            return com.lowdragmc.lowdraglib.client.renderer.NativeDelegatedBlockEntity.extract(represented,partialTicks,camera,breaking);
        } catch (Exception exception) {
            getBlockInfo().setHasBlockEntity(false);
            return null;
        }
    }



    @Override
    @OnlyIn(Dist.CLIENT)
    public TriState useAO() {
        var state = getBlockInfo().getBlockState();
        if (state.getRenderShape() != RenderShape.INVISIBLE) {
            return com.lowdragmc.lowdraglib.client.model.NativeBlockModelQueries.ambientOcclusion(state);
        }
        return TriState.DEFAULT;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean isGui3d() {
        return com.lowdragmc.lowdraglib.client.model.NativeItemProperties.gui3d(getBlockInfo().getItemStackForm());
    }
}
