package com.gregtechceu.gtceu.client.renderer.machine.impl;
import net.minecraft.core.Direction;
import org.joml.Vector3f;
import org.joml.Vector3fc;
public final class NativePlantPlacement {
    private NativePlantPlacement() {}
    public static Vector3f rotate(Vector3fc offset,Direction front) {
        var rotated=new Vector3f(offset).rotateX(-net.minecraft.util.Mth.HALF_PI);
        front.getRotation().transform(rotated);
        return rotated;
    }
}
