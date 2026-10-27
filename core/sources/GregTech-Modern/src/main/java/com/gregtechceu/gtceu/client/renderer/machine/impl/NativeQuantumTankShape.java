package com.gregtechceu.gtceu.client.renderer.machine.impl;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import java.util.*;
/** Original tank front-face geometry including sideways/up/down gas and liquid fill. */
public record NativeQuantumTankShape(AABB bounds,Set<Direction> faces) {
    public NativeQuantumTankShape { faces=Set.copyOf(faces); }
    public static NativeQuantumTankShape create(Direction front,boolean gas,long amount,long maximum,boolean creative) {
        float min=.16f,max=.84f;
        float fraction=creative || maximum<=amount?1f:(float)amount/maximum;
        float top=gas?max:min+fraction*(max-min);
        float bottom=gas?min+(1-fraction)*(max-min):min;
        float minY,maxY,minZ,maxZ;
        var faces=EnumSet.of(front);
        if(front.getAxis()==Direction.Axis.Y) {
            minY=min;maxY=max;
            if(front==Direction.UP){minZ=bottom;maxZ=top;faces.add(gas?Direction.SOUTH:Direction.NORTH);}
            else {minZ=1-top;maxZ=1-bottom;faces.add(gas?Direction.NORTH:Direction.SOUTH);}
        } else {minY=bottom;maxY=top;minZ=min;maxZ=max;faces.add(gas?Direction.DOWN:Direction.UP);}
        return new NativeQuantumTankShape(new AABB(min,minY,minZ,max,maxY,maxZ),faces);
    }
}
