package com.gregtechceu.gtceu.api.misc.forge;

import com.gregtechceu.gtceu.api.capability.IThermalFluidHandlerItemStack;
import com.gregtechceu.gtceu.common.data.item.GTDataComponents;

import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.fluid.ItemAccessFluidHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;

public class ThermalFluidHandlerItemStack extends ItemAccessFluidHandler implements IThermalFluidHandlerItemStack {

    @Getter
    private final int maxFluidTemperature;
    @Getter
    private final boolean gasProof;
    @Getter
    private final boolean acidProof;
    @Getter
    private final boolean cryoProof;
    @Getter
    private final boolean plasmaProof;

    /**
     * @param container The container itemStack, data is stored on it directly as NBT.
     * @param capacity  The maximum capacity of this fluid tank.
     */
    public ThermalFluidHandlerItemStack(@NotNull ItemAccess container, int capacity, int maxFluidTemperature,
                                        boolean gasProof, boolean acidProof, boolean cryoProof, boolean plasmaProof) {
        super(container, GTDataComponents.FLUID_CONTENT.get(), capacity);
        this.maxFluidTemperature = maxFluidTemperature;
        this.gasProof = gasProof;
        this.acidProof = acidProof;
        this.cryoProof = cryoProof;
        this.plasmaProof = plasmaProof;
    }

    @Override
    protected net.neoforged.neoforge.transfer.item.ItemResource update(
            net.neoforged.neoforge.transfer.item.ItemResource container, int index,
            FluidResource resource, int amount) {
        // Empty cells must stack with fresh cells; retain every unrelated item component.
        return amount == 0 ? container.without(component) : super.update(container, index, resource, amount);
    }

    @Override
    public boolean isValid(int index,FluidResource resource) {
        return super.isValid(index,resource) && canFillFluidType(resource.toStack(capacity));
    }

    @Override
    public boolean canFillFluidType(FluidStack fluid) {
        return IThermalFluidHandlerItemStack.super.canFillFluidType(fluid);
    }
}
