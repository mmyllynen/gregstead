package com.firemerald.additionalplacements.client.models.rotated.neoforge;

import com.firemerald.additionalplacements.client.models.neoforge.PlacementModelWrapperImpl;
import com.firemerald.additionalplacements.client.models.rotated.BakedRotatedPlacementModel;
import com.firemerald.additionalplacements.client.models.BlockModelUtils;
import com.firemerald.additionalplacements.client.models.rotated.RotatedBlockModelPart;
import com.firemerald.additionalplacements.util.BlockRotation;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class BakedRotatedPlacementModelImpl extends BakedRotatedPlacementModel implements PlacementModelWrapperImpl {
	public static BakedRotatedPlacementModel of(BlockState theirModelState, BlockRotation modelRotation, boolean rotatesTexture) {
		return new BakedRotatedPlacementModelImpl(theirModelState, modelRotation, rotatesTexture);
	}

	protected BakedRotatedPlacementModelImpl(BlockState theirModelState, BlockRotation modelRotation, boolean rotatesTexture) {
		super(theirModelState, modelRotation, rotatesTexture);
    }

	@Override
	public void collectParts(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random, List<BlockStateModelPart> parts) {
		BlockStateModel theirModel = getWrappedModel();
		if (wasModelMissing()) theirModel.collectParts(level, pos, BlockModelUtils.getModeledState(state), random, parts);
		else {
			List<BlockStateModelPart> theirParts = new ArrayList<>();
			theirModel.collectParts(level, pos, BlockModelUtils.getModeledState(state), random, theirParts);
			theirParts.forEach(theirPart -> parts.add(RotatedBlockModelPart.of(theirPart, modelRotation, rotatesTexture)));
		}
	}

	@Override
	public @Nullable Object createGeometryKey(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random) {
		return this;
	}
}
