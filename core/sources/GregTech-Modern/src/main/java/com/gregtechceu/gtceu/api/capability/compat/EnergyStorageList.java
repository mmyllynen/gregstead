package com.gregtechceu.gtceu.api.capability.compat;

import com.lowdragmc.lowdraglib.transfer.energy.IEnergyStorage;
import net.neoforged.neoforge.transfer.TransferPreconditions;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import java.util.List;

/** Combined native FE capability; every child participates in the caller's transaction. */
public class EnergyStorageList implements IEnergyStorage {
    public final IEnergyStorage[] storages;
    public EnergyStorageList(IEnergyStorage[] storages) { this.storages = storages.clone(); }
    public EnergyStorageList(List<IEnergyStorage> storages) { this(storages.toArray(IEnergyStorage[]::new)); }
    @Override public int insert(int amount, TransactionContext transaction) {
        TransferPreconditions.checkNonNegative(amount);
        int inserted = 0;
        for (IEnergyStorage storage : storages) {
            inserted += storage.insert(amount - inserted, transaction);
            if (inserted == amount) break;
        }
        return inserted;
    }
    @Override public int extract(int amount, TransactionContext transaction) {
        TransferPreconditions.checkNonNegative(amount);
        int extracted = 0;
        for (IEnergyStorage storage : storages) {
            extracted += storage.extract(amount - extracted, transaction);
            if (extracted == amount) break;
        }
        return extracted;
    }
    @Override public long getAmountAsLong() {
        long result = 0;
        for (IEnergyStorage storage : storages) result = addSaturated(result, storage.getAmountAsLong());
        return result;
    }
    @Override public long getCapacityAsLong() {
        long result = 0;
        for (IEnergyStorage storage : storages) result = addSaturated(result, storage.getCapacityAsLong());
        return result;
    }
    private static long addSaturated(long left, long right) {
        return right > Long.MAX_VALUE - left ? Long.MAX_VALUE : left + right;
    }
    @Override public boolean canReceive() {
        for (IEnergyStorage storage : storages) if (storage.canReceive()) return true;
        return false;
    }
    @Override public boolean canExtract() {
        for (IEnergyStorage storage : storages) if (storage.canExtract()) return true;
        return false;
    }
}
