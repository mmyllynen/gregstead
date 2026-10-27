package com.gregtechceu.gtceu.api.recipe.content;
import com.mojang.serialization.*;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.util.LenientJsonParser;
import io.netty.handler.codec.*;
/** Original UTF JSON wire framing, with the native buffer's exact registry context. */
public final class NativeContentNetwork {
    private NativeContentNetwork() {}
    public static <T> void write(RegistryFriendlyByteBuf buffer,Codec<T> codec,T value) {
        var json=codec.encodeStart(buffer.registryAccess().createSerializationContext(JsonOps.INSTANCE),value)
                .getOrThrow(error->new EncoderException("Failed to encode: "+error+" "+value));
        buffer.writeUtf(json.toString());
    }
    public static <T> T read(RegistryFriendlyByteBuf buffer,Codec<T> codec) {
        var json=LenientJsonParser.parse(buffer.readUtf());
        return codec.parse(buffer.registryAccess().createSerializationContext(JsonOps.INSTANCE),json)
                .getOrThrow(error->new DecoderException("Failed to decode JSON: "+error));
    }
}
