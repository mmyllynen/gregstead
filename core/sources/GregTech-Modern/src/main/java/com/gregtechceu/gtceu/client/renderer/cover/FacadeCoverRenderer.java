package com.gregtechceu.gtceu.client.renderer.cover;

import com.gregtechceu.gtceu.api.cover.CoverBehavior;
import com.gregtechceu.gtceu.client.model.TextureOverrideModel;
import com.gregtechceu.gtceu.client.util.GTQuadTransformers;
import com.gregtechceu.gtceu.client.util.StaticFaceBakery;
import com.gregtechceu.gtceu.common.cover.FacadeCover;
import com.gregtechceu.gtceu.utils.GTUtil;

import com.lowdragmc.lowdraglib.client.bakedpipeline.FaceQuad;

import net.minecraft.util.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.renderer.block.dispatch.BlockModelRotation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.model.data.ModelData;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class FacadeCoverRenderer implements ICoverRenderer {

    private static final AABB FACADE_PLANE = new AABB(0.01, 0.01, 0.01, 0.99, 0.99, 1 / 16f);
    private static final EnumSet<Direction> FACADE_EDGE_FACES = EnumSet.of(Direction.DOWN, Direction.UP,
            Direction.SOUTH, Direction.WEST, Direction.EAST);
    private static final Map<Direction, AABB> COVER_BACK_CUBES = Util.make(new EnumMap<>(Direction.class), map -> {
        for (Direction dir : GTUtil.DIRECTIONS) {
            var normal = dir.getUnitVec3i();
            var cube = new AABB(
                    normal.getX() > 0 ? 1.01 : -0.01,
                    normal.getY() > 0 ? 1.01 : -0.01,
                    normal.getZ() > 0 ? 1.01 : -0.01,
                    normal.getX() >= 0 ? 1.01 : -0.01,
                    normal.getY() >= 0 ? 1.01 : -0.01,
                    normal.getZ() >= 0 ? 1.01 : -0.01);
            map.put(dir, cube);
        }
    });

    public static final FacadeCoverRenderer INSTANCE = new FacadeCoverRenderer();
    private FacadeCoverRenderer() {}

    @OnlyIn(Dist.CLIENT)
    public List<BakedQuad> getFacadeQuads(BlockState state, BlockAndTintGetter level, @NotNull RandomSource rand,
                                          @NotNull ModelData extraData, @Nullable RenderType renderType) {
        var mc = Minecraft.getInstance();
        if (mc.level == null) {
            return Collections.emptyList();
        }

        List<BakedQuad> quads = new LinkedList<>();
        var model = mc.getModelManager().getBlockStateModelSet().get(state);

        if (state.getRenderShape() == RenderShape.MODEL) {

            List<BakedQuad> facadeQuads = new LinkedList<>();
            facadeQuads.addAll(NativeFacadeQuads.collect(model,level,BlockPos.ZERO,state,null,rand));
            facadeQuads.addAll(NativeFacadeQuads.collect(model,level,BlockPos.ZERO,state,Direction.NORTH,rand));

            // offset all the cover quads by a small value and bake their tint color into the vertices
            BlockColors blockColors = Minecraft.getInstance().getBlockColors();
            for (BakedQuad quad : facadeQuads) {
                if (quad.materialInfo().tintIndex() >= 0) {
                    // if the quad has a tint index set, bake the tint into the vertex
                    var tint = blockColors.getTintSource(state, quad.materialInfo().tintIndex());
                    int color = tint == null ? -1 : level == null ? tint.color(state) : tint.colorInWorld(state, level, BlockPos.ZERO);
                    quad = GTQuadTransformers.setColor(quad, color, true);
                } else {
                    // otherwise just copy the quad so we don't mutate the original model with the overlay offset
                    quad = GTQuadTransformers.copy(quad);
                }
                quad = TextureOverrideModel.OVERLAY_OFFSET.apply(quad);

                quads.add(quad);
            }

            for (Direction modelSide : FACADE_EDGE_FACES) {
                quads.add(StaticFaceBakery.bakeFace(FACADE_PLANE, modelSide, ICoverableRenderer.COVER_BACK_PLATE[0]));
            }
        }
        return quads;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void renderCover(List<BakedQuad> quads, Direction side, RandomSource rand,
                            @NotNull CoverBehavior coverBehavior, BlockPos pos, BlockAndTintGetter level,
                            @NotNull ModelData modelData, @Nullable RenderType renderType) {
        if (!(coverBehavior instanceof FacadeCover facadeCover)) {
            return;
        }
        BlockState state = facadeCover.getFacadeState();
        if (state.getRenderShape() != RenderShape.MODEL) {
            return;
        }

        Direction attachedSide = coverBehavior.attachedSide;

        var model = Minecraft.getInstance().getModelManager().getBlockStateModelSet().get(state);

        List<BakedQuad> facadeQuads = NativeFacadeQuads.collect(model,level,pos,state,attachedSide,rand);
        facadeQuads = new LinkedList<>(facadeQuads);

        List<BakedQuad> coverQuads = new ArrayList<>();
        if (side == attachedSide) {
            coverQuads.addAll(facadeQuads);
        } else if (side == null && coverBehavior.coverHolder.shouldRenderBackSide()) {
            AABB cube = COVER_BACK_CUBES.get(attachedSide);

            for (BakedQuad quad : facadeQuads) {
                coverQuads.add(FaceQuad.bakeFace(cube, attachedSide.getOpposite(),
                        quad.materialInfo().sprite(), BlockModelRotation.IDENTITY,
                        quad.materialInfo().tintIndex(), 0, false, quad.materialInfo().shadeDirectionOverride() != Direction.UP));
            }
        }

        // offset all the cover quads by a small value and bake their tint color into the vertices
        BlockColors blockColors = Minecraft.getInstance().getBlockColors();
        for (BakedQuad quad : coverQuads) {
            if (quad.materialInfo().tintIndex() >= 0) {
                // if the quad has a tint index set, bake the tint into the vertex
                var tint = blockColors.getTintSource(state, quad.materialInfo().tintIndex());
                int color = tint == null ? -1 : level == null ? tint.color(state) : tint.colorInWorld(state, level, pos);
                quad = GTQuadTransformers.setColor(quad, color, true);
            } else {
                // otherwise just copy the quad so we don't mutate the original model with the overlay offset
                quad = GTQuadTransformers.copy(quad);
            }
            quad = TextureOverrideModel.OVERLAY_OFFSET.apply(quad);

            quads.add(quad);
        }
    }

}
