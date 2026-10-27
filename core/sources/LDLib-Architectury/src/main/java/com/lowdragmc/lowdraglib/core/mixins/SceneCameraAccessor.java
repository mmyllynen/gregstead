package com.lowdragmc.lowdraglib.core.mixins;
import net.minecraft.client.Camera;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Camera.class)
public interface SceneCameraAccessor {
    @Invoker("setupPerspective") void ldlib$scenePerspective(float near, float far, float fov, float width, float height);
    @Accessor("fov") void ldlib$sceneFov(float value);
    @Accessor("level") void ldlib$sceneLevel(Level level);
    @Accessor("initialized") void ldlib$sceneInitialized(boolean value);
}
