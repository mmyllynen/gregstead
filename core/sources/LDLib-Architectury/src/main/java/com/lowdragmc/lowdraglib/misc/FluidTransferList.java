package com.lowdragmc.lowdraglib.misc;

import com.lowdragmc.lowdraglib.transfer.fluid.IFluidHandlerModifiable;

import lombok.Setter;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import com.lowdragmc.lowdraglib.transfer.persistence.INBTSerializable;
import net.neoforged.neoforge.fluids.FluidStack;
import com.lowdragmc.lowdraglib.transfer.fluid.IFluidHandler;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

/**
 * @author KilaBash
 * @date 2023/2/25
 * @implNote FluidTransferList
 */
public class FluidTransferList implements IFluidHandlerModifiable, INBTSerializable<CompoundTag>, com.lowdragmc.lowdraglib.transfer.fluid.NativeFluidView {
    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(FluidTransferList.class);

    public final IFluidHandler[] transfers;
    @Setter
    protected Predicate<FluidStack> filter = fluid -> true;

    public FluidTransferList(IFluidHandler... transfers) {
        this.transfers = transfers;
    }

    public FluidTransferList(List<IFluidHandler> transfers) {
        this.transfers = transfers.toArray(IFluidHandler[]::new);
    }

    @Override
    public net.neoforged.neoforge.transfer.ResourceHandler<net.neoforged.neoforge.transfer.fluid.FluidResource> nativeView() {
        return new com.lowdragmc.lowdraglib.transfer.fluid.CombinedFluidView(transfers,fluid -> filter.test(fluid));
    }

    @Override
    public int getTanks() {
        return Arrays.stream(transfers).mapToInt(IFluidHandler::getTanks).sum();
    }

    @NotNull
    @Override
    public FluidStack getFluidInTank(int tank) {
        int index = 0;
        for (IFluidHandler transfer : transfers) {
            if (tank - index < transfer.getTanks()) {
                return transfer.getFluidInTank(tank - index);
            }
            index += transfer.getTanks();
        }
        return FluidStack.EMPTY;
    }

    @Override
    public void setFluidInTank(int tank, @NotNull FluidStack fluidStack) {
        int index = 0;
        for (IFluidHandler transfer : transfers) {
            if (transfer instanceof IFluidHandlerModifiable modifiable) {
                if (tank - index < transfer.getTanks()) {
                    modifiable.setFluidInTank(tank - index, fluidStack);
                    return;
                }
            }
            index += transfer.getTanks();
        }
    }

    @Override
    public int getTankCapacity(int tank) {
        int index = 0;
        for (IFluidHandler transfer : transfers) {
            if (tank - index < transfer.getTanks()) {
                return transfer.getTankCapacity(tank - index);
            }
            index += transfer.getTanks();
        }
        return 0;
    }

    @Override
    public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
        if (!filter.test(stack)) {
            return false;
        }
        int index = 0;
        for (IFluidHandler transfer : transfers) {
            if (tank - index < transfer.getTanks()) {
                return transfer.isFluidValid(tank - index, stack);
            }
            index += transfer.getTanks();
        }
        return false;
    }

    @Override
    public int fill(FluidStack resource, FluidAction action) {
        if (resource.isEmpty() || !filter.test(resource)) return 0;
        var copied = resource.copy();
        for (var transfer : transfers) {
            var candidate = copied.copy();
            copied.shrink(transfer.fill(candidate, action));
            if (copied.isEmpty()) break;
        }
        return resource.getAmount() - copied.getAmount();
    }

    @Override
    public @NotNull FluidStack drain(FluidStack resource, FluidAction action) {
        if (resource.isEmpty() || !filter.test(resource)) return FluidStack.EMPTY;
        var copied = resource.copy();
        for (var transfer : transfers) {
            var candidate = copied.copy();
            copied.shrink(transfer.drain(candidate, action).getAmount());
            if (copied.isEmpty()) break;
        }
        copied.setAmount(resource.getAmount() - copied.getAmount());
        return copied;
    }

    @Override
    public @NotNull FluidStack drain(int maxDrain, FluidAction action) {
        if (maxDrain == 0) {
            return FluidStack.EMPTY;
        }
        FluidStack totalDrained = null;
        for (var storage : transfers) {
            if (totalDrained == null || totalDrained.isEmpty()) {
                totalDrained = storage.drain(maxDrain, action);
                if (totalDrained.isEmpty()) {
                    totalDrained = null;
                } else {
                    maxDrain -= totalDrained.getAmount();
                }
            } else {
                FluidStack copy = totalDrained.copy();
                copy.setAmount(maxDrain);
                FluidStack drain = storage.drain(copy, action);
                totalDrained.grow(drain.getAmount());
                maxDrain -= drain.getAmount();
            }
            if (maxDrain <= 0) break;
        }
        return totalDrained == null ? FluidStack.EMPTY : totalDrained;
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        var tag = new CompoundTag();
        var list = new ListTag();
        for (IFluidHandler transfer : transfers) {
            if (transfer instanceof INBTSerializable<?> serializable) {
                list.add(serializable.serializeNBT(provider));
            } else {
                LOG.warn("[FluidTransferList] internal tank doesn't support serialization");
            }
        }
        tag.put("tanks", list);
        tag.putByte("type", (list.isEmpty() ? (byte) 0 : list.getFirst().getId()));
        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag nbt) {
        var list = nbt.getListOrEmpty("tanks");
        for (int i = 0; i < list.size(); i++) {
            if (transfers[i] instanceof INBTSerializable serializable) {
                serializable.deserializeNBT(provider, list.get(i));
            } else {
                LOG.warn("[FluidTransferList] internal tank doesn't support serialization");
            }
        }
    }

    @Override
    public boolean supportsFill(int tank) {
        for (IFluidHandler transfer : transfers) {
            if (tank >= transfer.getTanks()) {
                tank -= transfer.getTanks();
                continue;
            }

            if (transfer instanceof IFluidHandlerModifiable modifiable) {
                return modifiable.supportsFill(tank);
            }
        }

        return true;
    }

    @Override
    public boolean supportsDrain(int tank) {
        for (IFluidHandler transfer : transfers) {
            if (tank >= transfer.getTanks()) {
                tank -= transfer.getTanks();
                continue;
            }

            if (transfer instanceof IFluidHandlerModifiable modifiable) {
                return modifiable.supportsDrain(tank);
            }
        }

        return true;
    }
}
