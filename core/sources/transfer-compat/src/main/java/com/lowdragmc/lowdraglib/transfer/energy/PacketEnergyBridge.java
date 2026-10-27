package com.lowdragmc.lowdraglib.transfer.energy;

import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import java.util.Objects;
import java.util.function.IntSupplier;

/** Converts whole EU packets into FE, retaining all undelivered FE in a long buffer. */
public final class PacketEnergyBridge {
    private final IEnergyStorage destination;
    private final IntSupplier ratio;
    private long bufferedFe;
    private final SnapshotJournal<Long> journal = new SnapshotJournal<>() {
        @Override protected Long createSnapshot() { return bufferedFe; }
        @Override protected void revertToSnapshot(Long snapshot) { bufferedFe = snapshot; }
    };
    public PacketEnergyBridge(IEnergyStorage destination, IntSupplier ratio) {
        this.destination = Objects.requireNonNull(destination);
        this.ratio = Objects.requireNonNull(ratio);
    }
    public long getBufferedFe() { return bufferedFe; }
    /** Returns the number of newly consumed EU packets, not the FE transferred from old buffered packets. */
    public long accept(long voltage, long amperage) {
        long packetFe = voltage > 0 && amperage > 0 ?
                ConvertedEnergyHandler.toFeSaturated(voltage, ratio.getAsInt()) : 0;
        long availableNewPackets = packetFe > 0 ? amperage : 0;
        int request = boundedRequest(bufferedFe, packetFe, availableNewPackets);
        if (request == 0) return 0;
        try (Transaction transaction = openTransaction()) {
            int accepted = destination.insert(request, transaction);
            if (accepted == 0) return 0;
            long fromBuffer = Math.min(bufferedFe, accepted);
            long fromNewPackets = accepted - fromBuffer;
            long consumed = fromNewPackets == 0 ? 0 : 1 + (fromNewPackets - 1) / packetFe;
            // If a new packet was consumed, its previous buffer has been emptied.
            // consumed * packetFe is at most Integer.MAX_VALUE + packetFe - 1,
            // and consumed is 1 whenever packetFe itself exceeds Integer.MAX_VALUE.
            journal.updateSnapshots(transaction);
            bufferedFe = bufferedFe - fromBuffer + consumed * packetFe - fromNewPackets;
            transaction.commit();
            return consumed;
        }
    }
    private static int boundedRequest(long buffered, long packet, long packets) {
        if (buffered >= Integer.MAX_VALUE) return Integer.MAX_VALUE;
        long remaining = Integer.MAX_VALUE - buffered;
        if (packets > 0 && packet > remaining / packets) return Integer.MAX_VALUE;
        return (int) (buffered + packet * packets);
    }
    @SuppressWarnings("deprecation")
    private static Transaction openTransaction() { return Transaction.open(Transaction.getCurrentOpenedTransaction()); }
}
