package com.lowdragmc.lowdraglib.transfer.fluid;

import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;

import java.util.Objects;

/** Presents native tank capabilities to the slot-based recipe/pipe API without mixing fluid identities. */
public final class ResourceFluidHandler implements IFluidHandler, NativeFluidView {
    private final ResourceHandler<FluidResource> handler;
    private ResourceFluidHandler(ResourceHandler<FluidResource> handler) { this.handler = Objects.requireNonNull(handler); }
    public static IFluidHandler of(ResourceHandler<FluidResource> handler) {
        return handler instanceof IFluidHandler tanks ? tanks : new ResourceFluidHandler(handler);
    }
    @Override public ResourceHandler<FluidResource> nativeView() { return handler; }
    @Override public int getTanks() { return handler.size(); }
    @Override public FluidStack getFluidInTank(int tank) {
        return handler.getResource(tank).toStack(handler.getAmountAsInt(tank));
    }
    @Override public int getTankCapacity(int tank) { return handler.getCapacityAsInt(tank, FluidResource.EMPTY); }
    @Override public boolean isFluidValid(int tank, FluidStack stack) {
        return !stack.isEmpty() && handler.isValid(tank, FluidResource.of(stack));
    }
    @Override public int fill(FluidStack resource, FluidAction action) {
        Objects.requireNonNull(action);
        if (resource.isEmpty()) return 0;
        try (Transaction transaction = openTransaction()) {
            int filled = handler.insert(FluidResource.of(resource), resource.getAmount(), transaction);
            if (action.execute()) transaction.commit();
            return filled;
        }
    }
    @Override public FluidStack drain(FluidStack resource, FluidAction action) {
        Objects.requireNonNull(action);
        if (resource.isEmpty()) return FluidStack.EMPTY;
        FluidResource identity = FluidResource.of(resource);
        try (Transaction transaction = openTransaction()) {
            int drained = handler.extract(identity, resource.getAmount(), transaction);
            FluidStack result = identity.toStack(drained);
            if (action.execute()) transaction.commit();
            return result;
        }
    }
    @Override public FluidStack drain(int maxDrain, FluidAction action) {
        Objects.requireNonNull(action);
        if (maxDrain <= 0) return FluidStack.EMPTY;
        for (int tank = 0; tank < handler.size(); tank++) {
            FluidResource identity = handler.getResource(tank);
            if (!identity.isEmpty() && handler.getAmountAsLong(tank) > 0) {
                // Probe in a child: a non-empty but non-extractable first tank must not
                // prevent draining a later tank. Never combine different identities.
                FluidStack result = drain(identity.toStack(maxDrain), action);
                if (!result.isEmpty()) return result;
            }
        }
        return FluidStack.EMPTY;
    }
    @SuppressWarnings("deprecation")
    private static Transaction openTransaction() { return Transaction.open(Transaction.getCurrentOpenedTransaction()); }
}
