package com.lowdragmc.lowdraglib.core.mixins;

import net.minecraft.client.particle.ParticleGroup;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.ParticleRenderType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

/**
 * @author KilaBash
 * @date 2022/7/23
 * @implNote ParticleEngineMixin
 */
@Mixin(ParticleEngine.class)
public abstract class ParticleEngineMixin {

    @Shadow @Final private Map<ParticleRenderType, ParticleGroup<?>> particles;

    @Inject(method = "tick", at = @At("TAIL"))
    public void injectTick(CallbackInfo ci) {
        particles.entrySet().removeIf(entry -> entry.getValue().isEmpty());
    }

}
