package com.gregtechceu.gtceu.client.model.machine;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import com.lowdragmc.lowdraglib.client.model.custommodel.CustomBakedModel;
/** Preserve native CTM rendering while reading the wrapped machine metadata. */
public final class NativeMachineModelLookup {
    private NativeMachineModelLookup() {}
    public static BlockStateModel unwrap(BlockStateModel model) {
        while(model instanceof CustomBakedModel<?> wrapped)model=wrapped.child();
        return model;
    }
}
