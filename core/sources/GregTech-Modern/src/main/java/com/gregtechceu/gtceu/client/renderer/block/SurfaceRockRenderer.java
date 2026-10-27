package com.gregtechceu.gtceu.client.renderer.block;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.data.pack.GTDynamicResourcePack;

import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.client.data.models.blockstates.PropertyDispatch;
import net.minecraft.client.renderer.block.dispatch.Variant;
import com.gregtechceu.gtceu.api.registry.registrate.provider.GTBlockstateProvider;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.model.DelegatedModel;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import java.util.HashSet;
import java.util.Set;

public class SurfaceRockRenderer {

    private static final Set<SurfaceRockRenderer> MODELS = new HashSet<>();

    public static void create(Block block) {
        MODELS.add(new SurfaceRockRenderer(block));
    }

    public static void reinitModels() {
        for (SurfaceRockRenderer model : MODELS) {
            Identifier blockId = BuiltInRegistries.BLOCK.getKey(model.block);
            Identifier modelId = blockId.withPrefix("block/");

            GTDynamicResourcePack.addBlockModel(blockId, new DelegatedModel(GTCEu.id("block/surface_rock")));
            var facing = PropertyDispatch.initial(BlockStateProperties.FACING).generate(direction -> {
                int x = switch (direction) { case UP -> 180; case WEST -> 270; case EAST -> 90; default -> 0; };
                int y = switch (direction) { case NORTH -> 90; case SOUTH -> 270; default -> 0; };
                return BlockModelGenerators.variant(GTBlockstateProvider.applyRotation(new Variant(modelId), x, y, 0));
            });
            GTDynamicResourcePack.addBlockState(blockId, MultiVariantGenerator.dispatch(model.block).with(facing));
            GTDynamicResourcePack.addItemModel(blockId, new DelegatedModel(modelId));
        }
    }

    private final Block block;

    protected SurfaceRockRenderer(Block block) {
        this.block = block;
    }
}
