package com.firemerald.additionalplacements.network;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;
import net.minecraft.network.FriendlyByteBuf;

/** The upstream VarInt collection wire format, with a bounded element count. */
public final class APBuffer {
    private static int count(FriendlyByteBuf buffer) {
        int size = buffer.readVarInt();
        if (size < 0 || size > 65536 || size > buffer.readableBytes()) throw new IllegalArgumentException("Invalid AP collection size: " + size);
        return size;
    }
    public static <T> List<T> readList(FriendlyByteBuf buffer, Function<FriendlyByteBuf,T> read) {
        int size=count(buffer); List<T> result=new ArrayList<>(size);
        for (int i=0;i<size;i++) result.add(read.apply(buffer));
        return result;
    }
    public static <T> void writeList(FriendlyByteBuf buffer, Collection<T> values, BiConsumer<FriendlyByteBuf,T> write) {
        buffer.writeVarInt(values.size()); values.forEach(value -> write.accept(buffer,value));
    }
    public static <K,V> Map<K,V> readMap(FriendlyByteBuf buffer, Function<FriendlyByteBuf,K> key, Function<FriendlyByteBuf,V> value) {
        int size=count(buffer); Map<K,V> result=new LinkedHashMap<>();
        for(int i=0;i<size;i++) result.put(key.apply(buffer),value.apply(buffer));
        return result;
    }
    public static <K,V> void writeMap(FriendlyByteBuf buffer, Map<K,V> values, BiConsumer<FriendlyByteBuf,K> key, BiConsumer<FriendlyByteBuf,V> value) {
        buffer.writeVarInt(values.size()); values.forEach((k,v) -> {key.accept(buffer,k);value.accept(buffer,v);});
    }
}
