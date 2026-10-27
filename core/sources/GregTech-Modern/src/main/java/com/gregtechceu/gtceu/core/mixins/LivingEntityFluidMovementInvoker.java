package com.gregtechceu.gtceu.core.mixins;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/** Shares vanilla swimming physics without marking industrial fluids as water. */
@Mixin(LivingEntity.class)
public interface LivingEntityFluidMovementInvoker {

    @Invoker("travelInWater")
    void gtceu$travelInWater(Vec3 movementVector, double gravity, boolean falling, double previousY);
}
