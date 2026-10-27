package com.lowdragmc.lowdraglib.core.mixins;
import com.lowdragmc.lowdraglib.client.model.BreakingModelSnapshot;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.state.level.BlockBreakingRenderState;
import org.spongepowered.asm.mixin.*;
import java.util.List;
@Mixin(BlockBreakingRenderState.class)
public abstract class BreakingModelStateMixin implements BreakingModelSnapshot {
    @Unique private List<BlockStateModelPart> ldlib$parts;
    public List<BlockStateModelPart> ldlib$breakingParts() { return ldlib$parts; }
    public void ldlib$breakingParts(List<BlockStateModelPart> parts) { ldlib$parts = parts; }
}
