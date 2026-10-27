package dev.emi.emi.compost;

import java.util.Map;
import java.util.Optional;
import net.minecraft.world.level.storage.loot.providers.number.ints.ResolvableInt;

public final class CompostProfiles {
    private CompostProfiles() {}
    private record State(Map<ResolvableInt,CompostProfile> profiles,boolean ready) {}
    private static volatile State state=new State(Map.of(),false);
    private static final java.util.concurrent.CopyOnWriteArrayList<Runnable> listeners=new java.util.concurrent.CopyOnWriteArrayList<>();
    public static boolean ready() { return state.ready(); }
    public static Map<ResolvableInt,CompostProfile> all() { return state.profiles(); }
    public static void onReceive(Runnable listener) { listeners.add(listener); }
    public static void receive(CompostProfilesPayload payload) {
        state=new State(payload.profiles(),true);listeners.forEach(Runnable::run);
    }
    public static void clear() { state=new State(Map.of(),false); }
    public static Optional<CompostProfile> get(ResolvableInt value) {
        if(value instanceof ResolvableInt.Constant constant) {
            var outcome=Optional.of(Map.of(Math.max(0,constant.value()),1.0));
            return Optional.of(new CompostProfile(java.util.Collections.nCopies(7,outcome)));
        }
        return Optional.ofNullable(state.profiles().get(value));
    }
}
