package dev.emi.emi.loot;

import com.mojang.serialization.JsonOps;
import java.util.HashMap;
import java.util.List;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootTable;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

public final class ToolLootSync {
    private ToolLootSync() {}
    public static void register(RegisterPayloadHandlersEvent event) {
        event.registrar("emi_tool_loot_1").optional().playToClient(ToolLootPayload.TYPE,ToolLootPayload.CODEC,
            (payload,context)->context.enqueueWork(()->ToolLootProfiles.receive(payload)));
    }
    public static ToolLootPayload snapshot(ServerLevel level) {
        var registries=level.getServer().reloadableRegistries().lookup();
        var ops=RegistryOps.create(JsonOps.INSTANCE,registries);
        var profiles=new HashMap<Identifier,List<ItemStack>>();
        level.registryAccess().lookupOrThrow(Registries.BLOCK_TRANSFORMER).listElements().forEach(holder-> {
            for(var transform:net.neoforged.neoforge.common.DataMapHooks.appendDatamapTransformers(holder,holder.value().transforms())) {
                transform.loot().ifPresent(key->registries.lookupOrThrow(Registries.LOOT_TABLE).get(key).ifPresent(table-> {
                    var encoded=LootTable.DIRECT_CODEC.encodeStart(ops,table.value());
                    encoded.result().flatMap(NativeToolLoot::fixedOutputs).ifPresent(stacks->profiles.put(key.identifier(),stacks));
                    if(!profiles.containsKey(key.identifier()))dev.emi.emi.runtime.EmiLog.warn("Unsupported native tool loot profile "+key.identifier());

                }));
            }
        });
        return new ToolLootPayload(profiles);
    }
    public static void send(OnDatapackSyncEvent event) {
        var payload=snapshot(event.getPlayerList().getServer().overworld());
        event.getRelevantPlayers().filter(player->player.connection.hasChannel(payload))
            .forEach(player->PacketDistributor.sendToPlayer(player,payload));
    }
}
