package com.lowdragmc.lowdraglib.core.mixins;

import net.minecraft.client.particle.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;
import java.util.List;

@Mixin(ParticleEngine.class)
public interface ParticleEngineAccessor {
    @Accessor("resourceManager") ParticleResources ldlib$resources();
    @Accessor("particleRenderOrder") List<ParticleRenderType> ldlib$renderOrder();
    @Invoker("createParticleGroup") ParticleGroup<?> ldlib$createGroup(ParticleRenderType type);
}
