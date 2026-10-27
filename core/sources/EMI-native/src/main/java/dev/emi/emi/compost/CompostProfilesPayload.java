package dev.emi.emi.compost;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.storage.loot.providers.number.ints.ResolvableInt;

public record CompostProfilesPayload(Map<ResolvableInt,CompostProfile> profiles) implements CustomPacketPayload {
    public static final Type<CompostProfilesPayload> TYPE=new Type<>(Identifier.parse("emi:compost_profiles"));
    public static final StreamCodec<RegistryFriendlyByteBuf,CompostProfilesPayload> CODEC=StreamCodec.ofMember(CompostProfilesPayload::write,CompostProfilesPayload::read);
    public CompostProfilesPayload { profiles=Map.copyOf(profiles); }
    @Override public Type<CompostProfilesPayload> type() { return TYPE; }
    private void write(RegistryFriendlyByteBuf buffer) {
        buffer.writeVarInt(profiles.size());
        profiles.forEach((key,profile) -> {
            ResolvableInt.STREAM_CODEC.encode(buffer,key);
            for(var level:profile.levels()) {
                buffer.writeBoolean(level.isPresent());
                level.ifPresent(values -> {
                    buffer.writeVarInt(values.size());
                    values.forEach((layers,chance) -> {buffer.writeVarInt(layers);buffer.writeDouble(chance);});
                });
            }
        });
    }
    private static int size(RegistryFriendlyByteBuf buffer,int maximum) {
        int size=buffer.readVarInt();
        if(size<0 || size>maximum)throw new io.netty.handler.codec.DecoderException("Compost metadata size exceeds limit");
        return size;
    }
    private static CompostProfilesPayload read(RegistryFriendlyByteBuf buffer) {
        int count=size(buffer,16384);var profiles=new HashMap<ResolvableInt,CompostProfile>();
        for(int i=0;i<count;i++) {
            var key=ResolvableInt.STREAM_CODEC.decode(buffer);
            var levels=new ArrayList<Optional<Map<Integer,Double>>>();
            for(int fill=0;fill<7;fill++) {
                if(!buffer.readBoolean()) { levels.add(Optional.empty());continue; }
                int entries=size(buffer,1024);var values=new HashMap<Integer,Double>();
                for(int j=0;j<entries;j++) if(values.put(buffer.readVarInt(),buffer.readDouble())!=null)throw new io.netty.handler.codec.DecoderException("Duplicate compost outcome");
                levels.add(Optional.of(values));
            }
            if(profiles.put(key,new CompostProfile(levels))!=null)throw new io.netty.handler.codec.DecoderException("Duplicate compost profile");
        }
        return new CompostProfilesPayload(profiles);
    }
}
