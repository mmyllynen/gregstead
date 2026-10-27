package com.lowdragmc.lowdraglib.transfer.routing;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import java.util.function.*;
/** Snapshots existing routing state; readers must return detached state. */
public final class RoutingStateJournal<T> extends SnapshotJournal<T> {
    private final Supplier<T> read;private final Consumer<T> restore;
    public RoutingStateJournal(Supplier<T> read,Consumer<T> restore){this.read=read;this.restore=restore;}
    protected T createSnapshot(){return read.get();}
    protected void revertToSnapshot(T saved){restore.accept(saved);}
}
