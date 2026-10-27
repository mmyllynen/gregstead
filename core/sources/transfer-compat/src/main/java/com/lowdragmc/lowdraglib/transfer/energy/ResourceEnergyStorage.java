package com.lowdragmc.lowdraglib.transfer.energy;

import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import java.util.Objects;

/** Exposes native external FE capabilities to GregTech's existing conversion algorithms. */
public final class ResourceEnergyStorage implements IEnergyStorage {
    private final EnergyHandler handler;
    private ResourceEnergyStorage(EnergyHandler handler) { this.handler = Objects.requireNonNull(handler); }
    public static IEnergyStorage of(EnergyHandler handler) {
        return handler instanceof IEnergyStorage energy ? energy : new ResourceEnergyStorage(handler);
    }
    @Override public long getAmountAsLong() { return handler.getAmountAsLong(); }
    @Override public long getCapacityAsLong() { return handler.getCapacityAsLong(); }
    @Override public int insert(int amount, TransactionContext transaction) { return handler.insert(amount, transaction); }
    @Override public int extract(int amount, TransactionContext transaction) { return handler.extract(amount, transaction); }
}
