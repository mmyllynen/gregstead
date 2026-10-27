package com.lowdragmc.lowdraglib.transfer.energy;

import net.neoforged.neoforge.transfer.TransferPreconditions;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.BooleanSupplier;
import java.util.function.IntSupplier;
import java.util.function.LongConsumer;
import java.util.function.LongSupplier;

/** Native FE input backed by an existing EU buffer; tentative writes bypass callbacks until root commit. */
public final class ConvertedEnergyHandler implements IEnergyStorage {
    private final LongSupplier storedEu, capacityEu;
    private final LongConsumer writeEu;
    private final IntSupplier ratio;
    private final BooleanSupplier acceptsFe;
    private final BiConsumer<Long, Long> onCommit;
    private final SnapshotJournal<Long> journal = new SnapshotJournal<>() {
        @Override protected Long createSnapshot() { return storedEu.getAsLong(); }
        @Override protected void revertToSnapshot(Long snapshot) { writeEu.accept(snapshot); }
        @Override protected void onRootCommit(Long original) {
            long current = storedEu.getAsLong();
            if (current != original) onCommit.accept(original, current);
        }
    };
    public ConvertedEnergyHandler(LongSupplier storedEu, LongConsumer writeEu, LongSupplier capacityEu,
            IntSupplier ratio, BooleanSupplier acceptsFe, BiConsumer<Long, Long> onCommit) {
        this.storedEu = Objects.requireNonNull(storedEu);
        this.writeEu = Objects.requireNonNull(writeEu);
        this.capacityEu = Objects.requireNonNull(capacityEu);
        this.ratio = Objects.requireNonNull(ratio);
        this.acceptsFe = Objects.requireNonNull(acceptsFe);
        this.onCommit = Objects.requireNonNull(onCommit);
    }
    private int ratio() {
        int value = ratio.getAsInt();
        if (value <= 0) throw new IllegalArgumentException("Energy conversion ratio must be positive: " + value);
        return value;
    }
    public static long toFeSaturated(long eu, int ratio) {
        if (ratio <= 0) throw new IllegalArgumentException("Energy conversion ratio must be positive: " + ratio);
        try { return Math.multiplyExact(eu, (long) ratio); }
        catch (ArithmeticException overflow) { return eu < 0 ? Long.MIN_VALUE : Long.MAX_VALUE; }
    }
    @Override public long getAmountAsLong() { return toFeSaturated(storedEu.getAsLong(), ratio()); }
    @Override public long getCapacityAsLong() { return toFeSaturated(capacityEu.getAsLong(), ratio()); }
    @Override public boolean canReceive() { return acceptsFe.getAsBoolean(); }
    @Override public boolean canExtract() { return false; }
    @Override public int insert(int amount, TransactionContext transaction) {
        TransferPreconditions.checkNonNegative(amount);
        if (!canReceive() || amount == 0) return 0;
        int ratio = ratio();
        long availableEu = Math.max(0, capacityEu.getAsLong() - storedEu.getAsLong());
        long acceptedEu = Math.min(availableEu, amount / ratio);
        if (acceptedEu == 0) return 0;
        journal.updateSnapshots(transaction);
        writeEu.accept(storedEu.getAsLong() + acceptedEu);
        return (int) (acceptedEu * ratio); // bounded by the int request before multiplication
    }
    @Override public int extract(int amount, TransactionContext transaction) {
        TransferPreconditions.checkNonNegative(amount);
        return 0; // The converter outputs FE through its front-side tick logic, not this input capability.
    }
}
