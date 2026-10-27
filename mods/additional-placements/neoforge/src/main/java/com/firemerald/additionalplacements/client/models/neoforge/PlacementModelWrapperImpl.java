package com.firemerald.additionalplacements.client.models.neoforge;

import com.firemerald.additionalplacements.client.models.BlockModelUtils;
import com.firemerald.additionalplacements.client.models.PlacementModelWrapper;
import org.jspecify.annotations.NullMarked;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.extensions.BlockStateModelExtension;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@ParametersAreNonnullByDefault
@NullMarked
public interface PlacementModelWrapperImpl extends PlacementModelWrapper, BlockStateModelExtension {
    @Override
    @Nullable
    Object createGeometryKey(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random);

    @Override
    void collectParts(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random, List<BlockStateModelPart> parts);

    @Override
    default Material.Baked particleMaterial(BlockAndTintGetter level, BlockPos pos, BlockState state) {
        return getVisualModel().particleMaterial(level, pos, BlockModelUtils.getModeledState(state));
    }

    @Override
    default @BakedQuad.MaterialFlags int materialFlags(BlockAndTintGetter level, BlockPos pos, BlockState state) {
        return getVisualModel().materialFlags(level, pos, BlockModelUtils.getModeledState(state));
    }
}
