package com.lowdragmc.lowdraglib.core.mixins;
import com.lowdragmc.lowdraglib.client.model.BreakingModelSnapshot;
import com.llamalad7.mixinextras.injector.wrapoperation.*;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.*;
import net.minecraft.client.renderer.state.level.BlockBreakingRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import java.util.List;
@Mixin(LevelRenderer.class)
public abstract class BreakingModelSubmitMixin {
    @WrapOperation(method="submitBlockDestroyAnimation", at=@At(value="INVOKE", target="Lnet/minecraft/client/renderer/block/dispatch/BlockStateModel;collectParts(Lnet/minecraft/client/renderer/block/BlockAndTintGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/util/RandomSource;Ljava/util/List;)V"), require=1)
    private void ldlib$parts(BlockStateModel model, BlockAndTintGetter level, BlockPos pos, BlockState state,
            RandomSource random, List<BlockStateModelPart> parts, Operation<Void> original, @Local BlockBreakingRenderState breaking) {
        var snapshot = ((BreakingModelSnapshot) (Object) breaking).ldlib$breakingParts();
        if (snapshot == null) original.call(model, level, pos, state, random, parts);
        else parts.addAll(snapshot);
    }
    @WrapOperation(method="submitBlockDestroyAnimation", at=@At(value="INVOKE", target="Lnet/minecraft/client/renderer/block/dispatch/BlockStateModel;hasMaterialFlag(I)Z"), require=1)
    private boolean ldlib$flags(BlockStateModel model, int flag, Operation<Boolean> original, @Local BlockBreakingRenderState breaking) {
        var snapshot = ((BreakingModelSnapshot) (Object) breaking).ldlib$breakingParts();
        return snapshot == null ? original.call(model, flag) : snapshot.stream().anyMatch(part -> (part.materialFlags() & flag) != 0);
    }
}
