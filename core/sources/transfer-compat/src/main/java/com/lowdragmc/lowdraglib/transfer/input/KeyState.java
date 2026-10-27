package com.lowdragmc.lowdraglib.transfer.input;

import java.util.WeakHashMap;

/** Per-player held state; duplicate network updates are not press/release transitions. */
public final class KeyState<P> {
    private final WeakHashMap<P, Boolean> states = new WeakHashMap<>();
    public boolean isDown(P player) { return Boolean.TRUE.equals(states.get(player)); }
    public boolean update(P player, boolean down) {
        boolean previous = isDown(player);
        states.put(player, down);
        return previous != down;
    }
}
