package com.lowdragmc.lowdraglib.transfer.world;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

/** Snapshot positions retained by native queued updates, scoped to one callback. */
public final class NeighborChangeContext {
    public record Snapshot(Level level, BlockPos target, BlockPos source) {}
    private static final ThreadLocal<Snapshot> PENDING = new ThreadLocal<>();
    private static final ThreadLocal<Snapshot> ACTIVE = new ThreadLocal<>();
    private NeighborChangeContext() {}

    public static void withSource(Level level, BlockPos target, BlockPos source, Runnable update) {
        Snapshot previous = PENDING.get();
        PENDING.set(new Snapshot(level, target.immutable(), source.immutable()));
        try { update.run(); }
        finally { restore(PENDING, previous); }
    }

    public static @Nullable Snapshot pending() {
        return PENDING.get();
    }

    public static void notifyNeighbor(Level level, BlockPos target,
            net.minecraft.world.level.block.Block changed, BlockPos source) {
        withSource(level, target, source, () -> level.neighborChanged(target, changed, null));
    }

    public static void runUpdate(Level level, BlockPos target, Runnable update) {
        Snapshot pending = PENDING.get();
        PENDING.remove();
        Snapshot previous = ACTIVE.get();
        restore(ACTIVE, pending != null && pending.level == level && pending.target.equals(target) ? pending : null);
        try { update.run(); }
        finally { restore(ACTIVE, previous); }
    }

    public static @Nullable BlockPos source(Level level, BlockPos target) {
        Snapshot active = ACTIVE.get();
        return active != null && active.level == level && active.target.equals(target) ? active.source : null;
    }

    /** Recheck the identified side, or every adjacent side when the native source is unavailable. */
    public static boolean forEachAffectedSide(Level level, BlockPos target,
            java.util.function.BiConsumer<net.minecraft.core.Direction, BlockPos> check) {
        BlockPos source = source(level, target);
        boolean checked = false;
        for (var side : net.minecraft.core.Direction.values()) {
            BlockPos adjacent = target.relative(side);
            if (source == null || source.equals(adjacent)) {
                check.accept(side, adjacent);
                checked = true;
            }
        }
        return checked;
    }

    private static void restore(ThreadLocal<Snapshot> local, @Nullable Snapshot value) {
        if (value == null) local.remove(); else local.set(value);
    }
}
