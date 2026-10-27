package com.firemerald.additionalplacements.datagen;

import com.firemerald.additionalplacements.block.AdditionalPlacementBlock;

import net.minecraft.client.data.models.MultiVariant;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.client.data.models.blockstates.PropertyDispatch;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;

public abstract class SimpleModelsGenerator<T extends Block, U extends AdditionalPlacementBlock<T>, V extends SimpleModelsGenerator<T, U, V>> extends BlockModelGenerator<T, U, V> {

	public abstract PropertyDispatch<MultiVariant> dispatch(Identifier modelPrefix);
	
	@Override
	public MultiVariantGenerator generator(U block, Identifier modelPrefix) {
		return MultiVariantGenerator.dispatch(block).with(dispatch(modelPrefix));
	}
}
