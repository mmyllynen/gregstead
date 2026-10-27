package com.gregtechceu.gtceu.api.misc.virtualregistry.entries;

import com.gregtechceu.gtceu.api.misc.virtualregistry.EntryTypes;
import com.gregtechceu.gtceu.api.misc.virtualregistry.VirtualEntry;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.neoforged.neoforge.fluids.FluidStack;
import com.lowdragmc.lowdraglib.transfer.fluid.FluidTank;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;

public class VirtualTank extends VirtualEntry {

    public static final int DEFAULT_CAPACITY = 160_000; // 160B for per second transfer
    protected static final String CAPACITY_KEY = "capacity";
    protected static final String FLUID_KEY = "fluid";
    @NotNull
    @Getter
    private final FluidTank fluidTank;

    public VirtualTank(int capacity) {
        fluidTank = new FluidTank(capacity);
    }

    public VirtualTank() {
        this(DEFAULT_CAPACITY);
    }

    @Override
    public EntryTypes<VirtualTank> getType() {
        return EntryTypes.ENDER_FLUID;
    }

    public void setFluid(FluidStack fluid) {
        this.fluidTank.setFluid(fluid);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof VirtualTank other)) return false;
        return this.fluidTank == other.fluidTank;
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.@NotNull Provider registries) {
        var tag = super.serializeNBT(registries);
        NativeVirtualTankData.write(fluidTank, tag, registries);
        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.@NotNull Provider registries, CompoundTag nbt) {
        super.deserializeNBT(registries, nbt);
        NativeVirtualTankData.read(fluidTank, nbt, registries);
    }

    @Override
    public boolean canRemove() {
        return super.canRemove() && this.fluidTank.getFluid().isEmpty();
    }
}
