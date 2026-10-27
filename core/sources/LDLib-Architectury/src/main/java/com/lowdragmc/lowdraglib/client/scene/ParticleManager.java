package com.lowdragmc.lowdraglib.client.scene;

import com.google.common.collect.Queues;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.world.level.Level;

import java.util.*;

/**
 * @author KilaBash
 * @date 2022/06/05
 * @implNote ParticleManager, for LParticle
 */
public class ParticleManager {
    private final Queue<Particle> waitToAdded = Queues.newArrayDeque();
    private final Map<ParticleRenderType, Queue<Particle>> particles = new IdentityHashMap<>();

    public Level level;

    public void setLevel(Level level) {
        this.level = level;
    }

    public void clearAllParticles() {
        synchronized (waitToAdded) {
            waitToAdded.clear();
            particles.clear();
        }
    }

    public void addParticle(Particle particle) {
        synchronized (waitToAdded) {
            waitToAdded.add(particle);
        }
    }

    public int getParticleAmount() {
        int amount = waitToAdded.size();
        amount += particles.values().stream().mapToInt(Collection::size).sum();
        return amount;
    }

    public void tick() {
        if (!waitToAdded.isEmpty()) {
            synchronized (waitToAdded) {
                for (var particle : waitToAdded) {
                    particles.computeIfAbsent(particle.getGroup(), type -> Queues.newArrayDeque()).add(particle);
                }
                waitToAdded.clear();
            }
        }
        this.particles.forEach((particleRenderType, particleQueue) -> this.tickParticleList(particleQueue));
    }

    private void tickParticleList(Collection<Particle> pParticles) {
        if (!pParticles.isEmpty()) {
            var iterator = pParticles.iterator();
            while(iterator.hasNext()) {
                var particle = iterator.next();
                particle.tick();
                if (!particle.isAlive()) {
                    iterator.remove();
                }
            }
        }

    }

    public net.minecraft.client.renderer.state.level.ParticlesRenderState extract(
            net.minecraft.client.renderer.culling.Frustum frustum, Camera camera, float partialTicks) {
        return NativeSceneParticles.extract(Minecraft.getInstance().particleEngine, particles,
                frustum, camera, partialTicks);
    }

    public void render(PoseStack pose, Camera camera, float partialTicks,
            com.mojang.blaze3d.pipeline.RenderTarget target, NativeSceneCamera sceneCamera) {
        var frustum=new net.minecraft.client.renderer.culling.Frustum(sceneCamera.view(),sceneCamera.projection());
        frustum.prepare(0,0,0);
        var state=extract(frustum,camera,partialTicks);
        var cameraState=new net.minecraft.client.renderer.state.level.CameraRenderState();
        cameraState.pos=camera.position();
        cameraState.orientation.set(camera.rotation());
        cameraState.initialized=true;
        var submits=new net.minecraft.client.renderer.SubmitNodeStorage();
        try(var renderer=new NativeSceneFeaturePass()) {
            state.submit(submits,cameraState);
            renderer.render(submits,target,sceneCamera.projection(),sceneCamera.type(),
                    sceneCamera.view().mul(pose.last().pose()),sceneCamera.area());
        } finally { state.reset(); }
    }

    public static Comparator<ParticleRenderType> makeParticleRenderTypeComparator(List<ParticleRenderType> renderOrder) {
        Comparator<ParticleRenderType> vanillaComparator = Comparator.comparingInt(renderOrder::indexOf);
        return (typeOne, typeTwo) ->
        {
            boolean vanillaOne = renderOrder.contains(typeOne);
            boolean vanillaTwo = renderOrder.contains(typeTwo);

            if (vanillaOne && vanillaTwo)
            {
                return vanillaComparator.compare(typeOne, typeTwo);
            }
            if (!vanillaOne && !vanillaTwo)
            {
                return Integer.compare(System.identityHashCode(typeOne), System.identityHashCode(typeTwo));
            }
            return vanillaOne ? -1 : 1;
        };
    }

}
