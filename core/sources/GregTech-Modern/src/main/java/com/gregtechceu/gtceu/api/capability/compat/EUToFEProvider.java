package com.gregtechceu.gtceu.api.capability.compat;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.IEnergyContainer;
import com.gregtechceu.gtceu.utils.GTMath;
import com.gregtechceu.gtceu.utils.GTUtil;

import net.minecraft.core.Direction;
import com.lowdragmc.lowdraglib.transfer.energy.IEnergyStorage;
import com.lowdragmc.lowdraglib.transfer.energy.PacketEnergyBridge;

public class EUToFEProvider implements IEnergyContainer {

    /**
     * Internally used FE Buffer so that a very large packet of EU is not partially destroyed
     * on the conversion to FE. This is hidden from the player, but ensures that no energy
     * is ever lost on conversion, no matter the voltage tier or FE storage abilities.
     */
    private final PacketEnergyBridge packetBridge;

    private final IEnergyStorage energyStorage;

    public EUToFEProvider(IEnergyStorage energyStorage) {
        this.energyStorage = energyStorage;
        this.packetBridge = new PacketEnergyBridge(energyStorage, () -> FeCompat.ratio(false));
    }

    @Override
    public long acceptEnergyFromNetwork(Direction facing, long voltage, long amperage) {
        return packetBridge.accept(voltage, amperage);
    }

    @Override
    public long changeEnergy(long delta) {
        if (delta == 0) return 0;
        else if (delta < 0) return -FeCompat.extractEu(energyStorage, -delta, false);
        else return FeCompat.insertEu(energyStorage, delta, false);
    }

    @Override
    public long getEnergyCapacity() {
        return FeCompat.toEu(energyStorage.getCapacityAsLong(), FeCompat.ratio(false));
    }

    @Override
    public long getEnergyStored() {
        return FeCompat.toEu(energyStorage.getAmountAsLong(), FeCompat.ratio(false));
    }

    /**
     * Most RF/FE cables blindly try to insert energy without checking if there is space, since the receiving
     * IEnergyStorage should handle it.
     * This simulates that behavior in most places by allowing our "is there space" checks to pass and letting the
     * cable attempt to insert energy.
     * If the wrapped TE actually cannot accept any more energy, the energy transfer will return 0 before any
     * changes to our internal rf buffer.
     */
    @Override
    public long getEnergyCanBeInserted() {
        return Math.max(1, getEnergyCapacity() - getEnergyStored());
    }

    @Override
    public long getInputAmperage() {
        return getInputVoltage() == 0 ? 0 : 2;
    }

    @Override
    public long getInputVoltage() {
        long maxInput = energyStorage.receiveEnergy(Integer.MAX_VALUE, true);

        if (maxInput == 0) return 0;
        return GTValues.V[GTUtil.getTierByVoltage(FeCompat.toEu(maxInput, FeCompat.ratio(false)))];
    }

    @Override
    public boolean inputsEnergy(Direction facing) {
        return energyStorage.canReceive();
    }

    /**
     * Wrapped FE-consumers should not be able to output EU.
     */
    @Override
    public boolean outputsEnergy(Direction facing) {
        return false;
    }

    /**
     * Hide this BlockEntity EU-capability in TOP. Allows FE-machines to
     * "silently" accept EU without showing their charge in EU in TOP.
     * Let the machine display it in FE instead, however it chooses to.
     */
    @Override
    public boolean isOneProbeHidden() {
        return true;
    }
}
