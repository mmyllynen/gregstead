package com.firemerald.additionalplacements.client.models.neoforge;

import com.firemerald.additionalplacements.client.models.WrappedBlockModelPart;
import net.minecraft.util.TriState;
import net.neoforged.neoforge.client.extensions.BlockStateModelPartExtension;

public interface WrappedBlockModelPartImpl extends WrappedBlockModelPart, BlockStateModelPartExtension {
    @Override
    default TriState ambientOcclusion() {
        return getVisual().ambientOcclusion();
    }
}
