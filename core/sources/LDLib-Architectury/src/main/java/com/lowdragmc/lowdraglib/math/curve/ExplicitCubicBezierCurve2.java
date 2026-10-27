package com.lowdragmc.lowdraglib.math.curve;

import com.lowdragmc.lowdraglib.Platform;
import com.lowdragmc.lowdraglib.math.Interpolations;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.phys.Vec2;
import com.lowdragmc.lowdraglib.transfer.persistence.INBTSerializable;

/**
 * @author KilaBash
 * @date 2022/6/17
 * @implNote ExplicitCubicBezierCurve2
 */
public class ExplicitCubicBezierCurve2 extends Curve<Vec2> implements INBTSerializable<ListTag> {
    public Vec2 p0, c0, c1, p1;

    public ExplicitCubicBezierCurve2(Vec2 start, Vec2 control1, Vec2 control2, Vec2 end) {
        this.p0 = start;
        this.c0 = control1;
        this.c1 = control2;
        this.p1 = end;
    }

    public ExplicitCubicBezierCurve2(HolderLookup.Provider provider, ListTag list) {
        deserializeNBT(provider, list);
    }

    public ExplicitCubicBezierCurve2(ListTag list) {
        deserializeNBT(Platform.getFrozenRegistry(), list);
    }

    @Override
    public Vec2 getPoint(float t) {
        if (c0.x == p0.x) {
            return new Vec2(p0.x + t * (p1.x - p0.x), c0.y > p0.y ? p0.y : p1.y);
        }
        if (c1.x == p1.x) {
            return new Vec2(p0.x + t * (p1.x - p0.x), c1.y > p1.y ? p1.y : p0.y);
        }
        return new Vec2(
                p0.x + t * (p1.x - p0.x),
                (float) Interpolations.CubicBezier(t, p0.y, c0.y, c1.y, p1.y)
        );
    }

    @Override
    public ListTag serializeNBT(HolderLookup.Provider provider) {
        var list = new ListTag();
        list.add(FloatTag.valueOf(p0.x));
        list.add(FloatTag.valueOf(p0.y));

        list.add(FloatTag.valueOf(c0.x));
        list.add(FloatTag.valueOf(c0.y));

        list.add(FloatTag.valueOf(c1.x));
        list.add(FloatTag.valueOf(c1.y));

        list.add(FloatTag.valueOf(p1.x));
        list.add(FloatTag.valueOf(p1.y));
        return list;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, ListTag list) {
        p0 = new Vec2(list.getFloatOr(0, 0.0F), list.getFloatOr(1, 0.0F));
        c0 = new Vec2(list.getFloatOr(2, 0.0F), list.getFloatOr(3, 0.0F));
        c1 = new Vec2(list.getFloatOr(4, 0.0F), list.getFloatOr(5, 0.0F));
        p1 = new Vec2(list.getFloatOr(6, 0.0F), list.getFloatOr(7, 0.0F));
    }
}
