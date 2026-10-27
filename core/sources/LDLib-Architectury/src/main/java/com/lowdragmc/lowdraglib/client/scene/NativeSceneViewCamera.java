package com.lowdragmc.lowdraglib.client.scene;

import com.lowdragmc.lowdraglib.core.mixins.SceneCameraAccessor;
import net.minecraft.client.Camera;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/** Scene camera placement independent of the main camera's player settings and tick/update loop. */
public final class NativeSceneViewCamera extends Camera {
    public void configure(Level level, Entity entity, Vec3 eye, float yaw, float pitch, float fov, float width, float height) {
        var access = (SceneCameraAccessor) (Object) this;
        access.ldlib$sceneLevel(level);
        setEntity(entity);
        setRotation(yaw, pitch);
        setPosition(eye);
        access.ldlib$scenePerspective(PROJECTION_Z_NEAR, 10000, fov, width, height);
        access.ldlib$sceneFov(fov);
        access.ldlib$sceneInitialized(true);
    }
}
