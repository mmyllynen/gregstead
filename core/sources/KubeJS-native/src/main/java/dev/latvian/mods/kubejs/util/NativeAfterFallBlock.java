package dev.latvian.mods.kubejs.util;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.phys.Vec3;

/** Returns the native collision result unless the script explicitly changes velocity. */
public interface NativeAfterFallBlock {
    Vec3 kjs$movementAfterFall(BlockGetter level, Entity entity, Vec3 nativeMovement);
}
