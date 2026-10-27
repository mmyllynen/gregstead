package com.lowdragmc.lowdraglib.client.scene;

import com.lowdragmc.lowdraglib.core.mixins.ParticleEngineAccessor;
import com.lowdragmc.lowdraglib.core.mixins.ParticleGroupAccessor;
import net.minecraft.client.Camera;
import net.minecraft.client.particle.*;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.state.level.ParticlesRenderState;
import java.util.Collection;
import java.util.Map;

/** Fresh native group states keep each scene snapshot separate from the live world engine. */
public final class NativeSceneParticles {
    private NativeSceneParticles() {}
    public static ParticlesRenderState extract(ParticleEngine engine,
            Map<ParticleRenderType, ? extends Collection<Particle>> particles,
            Frustum frustum, Camera camera, float partialTicks) {
        var access=(ParticleEngineAccessor)engine;
        var result=new ParticlesRenderState();
        for(var type:access.ldlib$renderOrder()) {
            var source=particles.get(type);
            if(source==null || source.isEmpty() || type==ParticleRenderType.NO_RENDER) continue;
            var group=access.ldlib$createGroup(type);
            // Scene manager historically owns unbounded queues; engine.add would apply unrelated world limits.
            var queue=((ParticleGroupAccessor)group).ldlib$particles();
            for(var particle:source) if(particle.isAlive()) queue.add(particle);
            if(!group.isEmpty()) result.add(group.extractRenderState(frustum,camera,partialTicks));
        }
        for(var entry:particles.entrySet()) {
            if(entry.getKey()!=ParticleRenderType.NO_RENDER && !entry.getValue().isEmpty()
                    && access.ldlib$renderOrder().stream().noneMatch(type -> type == entry.getKey()))
                throw new IllegalStateException("Scene particle group is not registered for native rendering: " + entry.getKey());
        }
        return result;
    }
}
