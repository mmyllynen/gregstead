package dev.latvian.mods.kubejs.core.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.*;
import com.llamalad7.mixinextras.sugar.Local;
import dev.latvian.mods.kubejs.util.NativeAfterFallBlock;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;

/** Runs after fall handling and immediately before the native collision velocity is committed. */
@Mixin(Entity.class)
public abstract class EntityAfterFallMixin {
    @Shadow public boolean verticalCollision;
    @Shadow public boolean verticalCollisionBelow;
    @WrapOperation(method = "restituteMovementAfterCollisions(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;ZZLnet/minecraft/world/phys/Vec3;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;setDeltaMovement(Lnet/minecraft/world/phys/Vec3;)V"))
    private void kjs$afterFall(Entity entity, Vec3 velocity, Operation<Void> original,
            @Local(argsOnly = true) BlockState effectState) {
        if (verticalCollision && verticalCollisionBelow && effectState.getBlock() instanceof NativeAfterFallBlock callback)
            velocity = callback.kjs$movementAfterFall(entity.level(), entity, velocity);
        original.call(entity, velocity);
    }
}
