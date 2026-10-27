package com.lowdragmc.lowdraglib.client.model;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import java.util.List;
public interface BreakingModelSnapshot {
    List<BlockStateModelPart> ldlib$breakingParts();
    void ldlib$breakingParts(List<BlockStateModelPart> parts);
}
