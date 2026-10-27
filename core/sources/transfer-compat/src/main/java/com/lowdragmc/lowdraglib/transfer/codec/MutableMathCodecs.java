package com.lowdragmc.lowdraglib.transfer.codec;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector4f;

/** Native math encodings with owned mutable values for LDLib managed fields. */
public final class MutableMathCodecs {
    private MutableMathCodecs() {}

    public static final Codec<Vector3f> VECTOR3F = ExtraCodecs.VECTOR3F.xmap(Vector3f::new, value -> value);
    public static final Codec<Vector4f> VECTOR4F = ExtraCodecs.VECTOR4F.xmap(Vector4f::new, value -> value);
    public static final Codec<Quaternionf> QUATERNIONF = ExtraCodecs.QUATERNIONF.xmap(Quaternionf::new, value -> value);
    public static final StreamCodec<ByteBuf, Vector3f> VECTOR3F_STREAM = ByteBufCodecs.VECTOR3F.map(Vector3f::new, value -> value);
    public static final StreamCodec<ByteBuf, Quaternionf> QUATERNIONF_STREAM = ByteBufCodecs.QUATERNIONF.map(Quaternionf::new, value -> value);
}
