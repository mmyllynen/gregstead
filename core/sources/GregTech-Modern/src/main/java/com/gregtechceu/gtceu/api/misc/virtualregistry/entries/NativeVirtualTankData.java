package com.gregtechceu.gtceu.api.misc.virtualregistry.entries;

import com.lowdragmc.lowdraglib.transfer.fluid.FluidTank;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.neoforged.neoforge.fluids.FluidStack;

/** Original virtual tank fields, resolved using the supplied world's registries. */
public final class NativeVirtualTankData {
    private NativeVirtualTankData() {}
    public static void write(FluidTank tank, CompoundTag tag, HolderLookup.Provider registries) {
        tag.putInt("capacity", tank.getCapacity());
        if (!tank.getFluid().isEmpty()) {
            tag.put("fluid", FluidStack.CODEC.encodeStart(registries.createSerializationContext(NbtOps.INSTANCE), tank.getFluid()).getOrThrow());
        } else {
            tag.remove("fluid");
        }
    }
    public static void read(FluidTank tank, CompoundTag tag, HolderLookup.Provider registries) {
        int capacity = tag.getIntOr("capacity", 0);
        FluidStack fluid = tag.contains("fluid") ? FluidStack.CODEC.parse(
                registries.createSerializationContext(NbtOps.INSTANCE), tag.getCompoundOrEmpty("fluid")).getOrThrow() : FluidStack.EMPTY;
        tank.setCapacity(capacity);
        tank.setFluid(fluid);
    }
}
