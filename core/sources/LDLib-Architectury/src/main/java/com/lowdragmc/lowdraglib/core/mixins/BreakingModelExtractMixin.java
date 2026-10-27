package com.lowdragmc.lowdraglib.core.mixins;
import com.lowdragmc.lowdraglib.client.model.NativeBreakingGeometry;
import net.minecraft.client.*;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.extract.LevelExtractor;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import net.minecraft.world.level.block.RenderShape;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(LevelExtractor.class)
public abstract class BreakingModelExtractMixin {
    @Shadow private ClientLevel level;
    @Shadow @Final private Minecraft minecraft;
    @Inject(method="extractBlockDestroyAnimation", at=@At("RETURN"), require=1)
    private void ldlib$capture(Camera camera, LevelRenderState frame, CallbackInfo callback) {
        for (var state : frame.blockBreakingRenderStates) {
            if (state.blockState().getRenderShape() == RenderShape.MODEL) NativeBreakingGeometry.capture(
                    state, level, minecraft.getModelManager().getBlockStateModelSet().get(state.blockState()));
        }
    }
}
