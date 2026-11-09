package fi.gregstead.toolbeltslot;

import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

/** Serializes logical presses against authoritative inventory updates, without predicting inventory edits. */
public final class CycleQueue<T> {
    private record Pending<T>(int slot, T tool, T returned, long deadline) {}
    private final UnaryOperator<T> copy;
    private final BiPredicate<T, T> same;
    private final Predicate<T> empty;
    private Pending<T> pending;
    private int last = -1;
    private int presses;

    public CycleQueue(UnaryOperator<T> copy, BiPredicate<T, T> same, Predicate<T> empty) {
        this.copy = copy; this.same = same; this.empty = empty;
    }
    public void press() { if (presses < Integer.MAX_VALUE) presses++; }
    public void cancel() { presses = 0; }
    public void reset() { cancel(); pending = null; last = -1; }
    public void remember(int slot) { cancel(); pending = null; last = slot; }

    /** Returns one slot to swap, or -1 until the previous swap is visible on both sides. */
    public int poll(List<T> belt, T held, long now) {
        if (pending != null) {
            if (pending.slot >= belt.size() || now >= pending.deadline) { reset(); return -1; }
            if (!same.test(held, pending.tool) || !same.test(belt.get(pending.slot), pending.returned)) return -1;
            last = pending.slot; pending = null;
        }
        if (presses == 0) return -1;
        presses--;
        for (int step = 1; step <= belt.size(); step++) {
            int slot = Math.floorMod(last + step, belt.size());
            if (!empty.test(belt.get(slot))) {
                pending = new Pending<>(slot, copy.apply(belt.get(slot)), copy.apply(held), now + 5_000_000_000L);
                return slot;
            }
        }
        presses = 0;
        return -1;
    }
}
