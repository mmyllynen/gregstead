package dev.emi.emi.loot;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

public record ToolLootPayload(Map<Identifier,List<ItemStack>> profiles) implements CustomPacketPayload {
    public static final Type<ToolLootPayload> TYPE=new Type<>(Identifier.parse("emi:tool_loot"));
    public static final StreamCodec<RegistryFriendlyByteBuf,ToolLootPayload> CODEC=StreamCodec.ofMember(ToolLootPayload::write,ToolLootPayload::read);
    public ToolLootPayload {
        var copy=new HashMap<Identifier,List<ItemStack>>();
        profiles.forEach((id,stacks)->copy.put(id,stacks.stream().map(ItemStack::copy).toList()));
        profiles=Map.copyOf(copy);
    }
    @Override public Type<ToolLootPayload> type() { return TYPE; }
    private void write(RegistryFriendlyByteBuf buffer) {
        buffer.writeVarInt(profiles.size());
        profiles.forEach((id,stacks)-> {
            buffer.writeIdentifier(id);buffer.writeVarInt(stacks.size());
            stacks.forEach(stack->ItemStack.OPTIONAL_STREAM_CODEC.encode(buffer,stack));
        });
    }
    private static int size(RegistryFriendlyByteBuf buffer,int maximum) {
        int count=buffer.readVarInt();
        if(count<0 || count>maximum)throw new io.netty.handler.codec.DecoderException("Tool loot metadata exceeds limit");
        return count;
    }
    private static ToolLootPayload read(RegistryFriendlyByteBuf buffer) {
        var profiles=new HashMap<Identifier,List<ItemStack>>();
        int count=size(buffer,4096);
        for(int i=0;i<count;i++) {
            var id=buffer.readIdentifier();var stacks=new java.util.ArrayList<ItemStack>();
            int entries=size(buffer,256);
            for(int j=0;j<entries;j++)stacks.add(ItemStack.OPTIONAL_STREAM_CODEC.decode(buffer));
            if(profiles.put(id,stacks)!=null)throw new io.netty.handler.codec.DecoderException("Duplicate tool loot profile");
        }
        return new ToolLootPayload(profiles);
    }
}
