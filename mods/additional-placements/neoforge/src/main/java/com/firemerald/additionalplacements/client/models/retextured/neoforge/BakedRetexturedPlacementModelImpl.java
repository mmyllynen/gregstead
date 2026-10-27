package com.firemerald.additionalplacements.client.models.retextured.neoforge;

import com.firemerald.additionalplacements.client.models.neoforge.PlacementModelWrapperImpl;
import com.firemerald.additionalplacements.client.models.retextured.BakedRetexturedPlacementModel;
import com.firemerald.additionalplacements.client.models.BlockModelUtils;
import com.firemerald.additionalplacements.client.models.retextured.RetexturedBlockModelPart;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class BakedRetexturedPlacementModelImpl extends BakedRetexturedPlacementModel implements PlacementModelWrapperImpl {
	public static BakedRetexturedPlacementModel of(BlockStateModel ourModel, BlockState theirModelState) {
		return new BakedRetexturedPlacementModelImpl(ourModel, theirModelState);
	}

	protected BakedRetexturedPlacementModelImpl(BlockStateModel ourModel, BlockState theirModelState) {
		super(ourModel, theirModelState);
	}

	@Override
	public void collectParts(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random, List<BlockStateModelPart> parts) {
		BlockStateModel ourModel = getWrappedModel();
		BlockStateModel theirModel = getVisualModel();
		if (wasModelMissing()) ourModel.collectParts(level, pos, state, random, parts);
		else {
			List<BlockStateModelPart> theirParts = new ArrayList<>();
			theirModel.collectParts(level, pos, BlockModelUtils.getModeledState(state), random, theirParts);
			if (!theirParts.isEmpty()) {
				List<BlockStateModelPart> wrappedParts = new ArrayList<>();
				ourModel.collectParts(level, pos, state, random, wrappedParts);
				wrappedParts.forEach(ourPart -> parts.add(RetexturedBlockModelPart.of(ourPart, theirParts)));
			}
		}
	}

	@Override
	public @Nullable Object createGeometryKey(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random) {
		return this;
	}
}
