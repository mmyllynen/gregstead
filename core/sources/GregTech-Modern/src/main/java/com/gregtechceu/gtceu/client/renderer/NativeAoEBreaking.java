package com.gregtechceu.gtceu.client.renderer;

import java.util.Collection;
import java.util.HashSet;
import net.minecraft.core.BlockPos;
import net.minecraft.client.renderer.state.level.BlockBreakingRenderState;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import net.minecraft.world.level.BlockGetter;

/** Adds detached area-tool destruction states without drawing the hit block twice. */
public final class NativeAoEBreaking {
    private NativeAoEBreaking() {}
    public static void append(LevelRenderState state,Collection<BlockPos> positions,BlockGetter world,int progress) {
        if(progress<0 || progress>9)return;
        var existing=new HashSet<BlockPos>();
        for(var breaking:state.blockBreakingRenderStates)existing.add(breaking.blockPos());
        for(var pos:positions)if(existing.add(pos.immutable()))
            state.blockBreakingRenderStates.add(new BlockBreakingRenderState(pos.immutable(),world.getBlockState(pos),progress));
    }
}
