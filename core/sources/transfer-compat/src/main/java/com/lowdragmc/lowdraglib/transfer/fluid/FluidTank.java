package com.lowdragmc.lowdraglib.transfer.fluid;

import com.lowdragmc.lowdraglib.transfer.persistence.INBTSerializable;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.fluid.FluidStacksResourceHandler;
import net.neoforged.neoforge.transfer.transaction.Transaction;

import java.util.Objects;
import java.util.function.Predicate;

/** A single tank backed by NeoForge's native transactional storage, with the recipe simulation API. */
public class FluidTank extends FluidStacksResourceHandler
        implements IFluidTank, IFluidHandlerModifiable, INBTSerializable<CompoundTag> {
    protected Predicate<FluidStack> validator;

    public FluidTank(int capacity) { this(capacity, stack -> true); }
    public FluidTank(int capacity, Predicate<FluidStack> validator) {
        super(1, checkedCapacity(capacity));
        this.validator = Objects.requireNonNull(validator);
    }
    private static int checkedCapacity(int capacity) {
        if (capacity < 0) throw new IllegalArgumentException("Negative tank capacity: " + capacity);
        return capacity;
    }
    @Override public int getTanks() { return size(); }
    @Override public FluidStack getFluid() { return stacks.get(0); }
    @Override public int getFluidAmount() { return getFluid().getAmount(); }
    @Override public int getCapacity() { return capacity; }
    public int getSpace() { return Math.max(0, capacity - getFluidAmount()); }
    @Override public FluidStack getFluidInTank(int tank) { checkTank(tank); return getFluid(); }
    @Override public int getTankCapacity(int tank) { checkTank(tank); return capacity; }
    @Override public boolean isFluidValid(FluidStack stack) { return validator.test(stack); }
    @Override public boolean isFluidValid(int tank, FluidStack stack) { checkTank(tank); return isFluidValid(stack); }
    @Override public boolean isValid(int index, FluidResource resource) {
        return isFluidValid(index, resource.toStack(1));
    }
    public FluidTank setValidator(Predicate<FluidStack> validator) {
        requireDirectEdit();
        this.validator = Objects.requireNonNull(validator);
        return this;
    }
    public FluidTank setCapacity(int capacity) { requireDirectEdit(); this.capacity = checkedCapacity(capacity); return this; }
    public void setFluid(FluidStack stack) {
        requireDirectEdit();
        Objects.requireNonNull(stack);
        set(0, FluidResource.of(stack), stack.getAmount());
    }
    @Override public void setFluidInTank(int tank, FluidStack stack) { checkTank(tank); setFluid(stack); }
    @Override public int fill(FluidStack resource, FluidAction action) {
        Objects.requireNonNull(action);
        if (resource.isEmpty() || !isFluidValid(resource)) return 0;
        try (Transaction transaction = openTransaction()) {
            int filled = insert(0, FluidResource.of(resource), resource.getAmount(), transaction);
            if (action.execute()) transaction.commit();
            return filled;
        }
    }
    @Override public FluidStack drain(FluidStack resource, FluidAction action) {
        Objects.requireNonNull(action);
        if (resource.isEmpty() || !FluidResource.of(resource).matches(getFluid())) return FluidStack.EMPTY;
        return drain(resource.getAmount(), action);
    }
    @Override public FluidStack drain(int maxDrain, FluidAction action) {
        Objects.requireNonNull(action);
        if (maxDrain <= 0 || getFluid().isEmpty()) return FluidStack.EMPTY;
        FluidResource resource = FluidResource.of(getFluid());
        try (Transaction transaction = openTransaction()) {
            int drained = extract(0, resource, maxDrain, transaction);
            FluidStack result = resource.toStack(drained);
            if (action.execute()) transaction.commit();
            return result;
        }
    }
    @SuppressWarnings("deprecation")
    private static Transaction openTransaction() { return Transaction.open(Transaction.getCurrentOpenedTransaction()); }
    @Override protected void onContentsChanged(int index, FluidStack previousContents) { onContentsChanged(); }
    protected void onContentsChanged() {}
    private void checkTank(int tank) { Objects.checkIndex(tank, size()); }
    protected static void requireDirectEdit() {
        if (Transaction.getLifecycle() == Transaction.Lifecycle.OPEN ||
                Transaction.getLifecycle() == Transaction.Lifecycle.CLOSING) {
            throw new IllegalStateException("Direct tank edits cannot bypass an open transfer transaction");
        }
    }
    public CompoundTag writeToNBT(HolderLookup.Provider registries, CompoundTag tag) {
        var ops = registries.createSerializationContext(NbtOps.INSTANCE);
        CompoundTag encoded = (CompoundTag) FluidStack.OPTIONAL_CODEC.encodeStart(ops, getFluid()).getOrThrow();
        return tag.merge(encoded);
    }
    public FluidTank readFromNBT(HolderLookup.Provider registries, CompoundTag tag) {
        requireDirectEdit();
        var ops = registries.createSerializationContext(NbtOps.INSTANCE);
        FluidStack restored = FluidStack.OPTIONAL_CODEC.parse(ops, tag).getOrThrow();
        setStacks(NonNullList.of(FluidStack.EMPTY, restored));
        return this;
    }
    @Override public CompoundTag serializeNBT(HolderLookup.Provider registries) {
        return writeToNBT(registries, new CompoundTag());
    }
    @Override public void deserializeNBT(HolderLookup.Provider registries, CompoundTag tag) { readFromNBT(registries, tag); }
}
