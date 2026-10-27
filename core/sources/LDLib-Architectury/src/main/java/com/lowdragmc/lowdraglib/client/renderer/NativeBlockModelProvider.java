package com.lowdragmc.lowdraglib.client.renderer;

import net.minecraft.world.level.block.state.BlockState;
import javax.annotation.Nullable;

public interface NativeBlockModelProvider {
    @Nullable NativeBlockRenderer getNativeBlockRenderer(BlockState state);
}
