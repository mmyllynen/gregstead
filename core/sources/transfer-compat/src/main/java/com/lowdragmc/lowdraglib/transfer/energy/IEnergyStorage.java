package com.lowdragmc.lowdraglib.transfer.energy;

import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.transaction.Transaction;

/** Internal FE operations backed by the native energy transaction contract. */
public interface IEnergyStorage extends EnergyHandler {
    default int receiveEnergy(int maximum, boolean simulate) {
        if (maximum <= 0) return 0;
        try (Transaction transaction = openTransaction()) {
            int received = insert(maximum, transaction);
            if (!simulate) transaction.commit();
            return received;
        }
    }
    default int extractEnergy(int maximum, boolean simulate) {
        if (maximum <= 0) return 0;
        try (Transaction transaction = openTransaction()) {
            int extracted = extract(maximum, transaction);
            if (!simulate) transaction.commit();
            return extracted;
        }
    }
    default int getEnergyStored() { return getAmountAsInt(); }
    default int getMaxEnergyStored() { return getCapacityAsInt(); }
    default boolean canReceive() { return receiveEnergy(Integer.MAX_VALUE, true) > 0; }
    default boolean canExtract() { return extractEnergy(Integer.MAX_VALUE, true) > 0; }
    @SuppressWarnings("deprecation")
    private static Transaction openTransaction() { return Transaction.open(Transaction.getCurrentOpenedTransaction()); }
}
