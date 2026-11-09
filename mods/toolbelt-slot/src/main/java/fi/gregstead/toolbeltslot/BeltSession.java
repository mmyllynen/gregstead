package fi.gregstead.toolbeltslot;

import java.util.function.IntConsumer;

/** One radial-menu opening captures one hotbar position. No inventory contents are moved here. */
public record BeltSession(int slot) {
    public BeltSession {
        if (slot < 0 || slot > 8) throw new IllegalArgumentException("Invalid hotbar slot");
    }

    public static BeltSession open(int bound, int current, boolean override) {
        return new BeltSession(override || bound < 0 ? current : bound);
    }

    public void synchronize(IntConsumer select, Runnable sync) {
        select.accept(slot);
        sync.run();
    }

    /** Selection must reach the server before the upstream swap payload, even within one frame. */
    public void swap(IntConsumer select, Runnable sync, Runnable send) {
        synchronize(select, sync);
        send.run();
    }
}
