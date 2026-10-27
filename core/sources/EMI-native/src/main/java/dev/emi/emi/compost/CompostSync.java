package dev.emi.emi.compost;

import java.util.ArrayList;
import java.util.HashMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.storage.loot.providers.number.ints.ResolvableInt;
import dev.emi.emi.recipe.NativeCompostData;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

public final class CompostSync {
    private CompostSync() {}
    public static void register(RegisterPayloadHandlersEvent event) {
        event.registrar("emi_compost_1").optional().playToClient(CompostProfilesPayload.TYPE,CompostProfilesPayload.CODEC,
            (payload,context) -> context.enqueueWork(() -> CompostProfiles.receive(payload)));
    }
    public static CompostProfilesPayload snapshot(ServerLevel level) {
        var registries=level.getServer().reloadableRegistries().lookup();
        var profiles=new HashMap<ResolvableInt,CompostProfile>();
        registries.lookupOrThrow(net.minecraft.core.registries.Registries.CONTEXT_INT_PROVIDER).listElements().forEach(holder -> {
            var key=new ResolvableInt.Reference(holder.key());
            var levels=new ArrayList<java.util.Optional<java.util.Map<Integer,Double>>>();
            for(int fill=0;fill<7;fill++)levels.add(NativeCompostData.distribution(key,registries,fill));
            profiles.put(key,new CompostProfile(levels));
        });
        return new CompostProfilesPayload(profiles);
    }
    public static void send(OnDatapackSyncEvent event) {
        var payload=snapshot(event.getPlayerList().getServer().overworld());
        event.getRelevantPlayers().filter(player -> player.connection.hasChannel(payload))
            .forEach(player -> PacketDistributor.sendToPlayer(player,payload));
    }
}
