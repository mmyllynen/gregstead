package dev.latvian.mods.kubejs.util;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.registries.DeferredRegister;
import java.util.function.Supplier;

/** Preserves the removed map color value for KubeJS's actual item tint callback. */
public record NativeMapItemColor(int rgb) {
    public static final Codec<NativeMapItemColor> CODEC = Codec.INT.xmap(NativeMapItemColor::new, NativeMapItemColor::rgb);
    public static final StreamCodec<ByteBuf, NativeMapItemColor> STREAM_CODEC = ByteBufCodecs.INT.map(NativeMapItemColor::new, NativeMapItemColor::rgb);
    public static final DeferredRegister<DataComponentType<?>> REGISTRY = DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, "kubejs");
    public static final Supplier<DataComponentType<NativeMapItemColor>> TYPE = REGISTRY.register("map_color", () ->
            DataComponentType.<NativeMapItemColor>builder().persistent(CODEC).networkSynchronized(STREAM_CODEC).cacheEncoding().build());
    public static Identifier resolveLegacyId(Identifier id) {
        return id.equals(Identifier.withDefaultNamespace("map_color")) ? Identifier.fromNamespaceAndPath("kubejs", "map_color") : id;
    }
}
