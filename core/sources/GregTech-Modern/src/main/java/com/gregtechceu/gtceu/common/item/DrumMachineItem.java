package com.gregtechceu.gtceu.common.item;

import com.gregtechceu.gtceu.api.block.MetaMachineBlock;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.FluidPipeProperties;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.item.MetaMachineItem;
import com.gregtechceu.gtceu.api.misc.forge.ThermalFluidHandlerItemStack;
import com.gregtechceu.gtceu.common.data.item.GTDataComponents;
import com.gregtechceu.gtceu.common.data.machines.GTMachineUtils;

import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.transfer.fluid.ItemAccessFluidHandler;

import org.jetbrains.annotations.NotNull;

public class DrumMachineItem extends MetaMachineItem {

    @NotNull
    private final Material mat;

    protected DrumMachineItem(MetaMachineBlock block, Properties properties, @NotNull Material mat) {
        super(block, properties);
        this.mat = mat;
    }

    public static DrumMachineItem create(MetaMachineBlock block, Properties properties, @NotNull Material mat) {
        return new DrumMachineItem(block, properties, mat);
    }

    public void attachCapabilities(RegisterCapabilitiesEvent event) {
        if (mat.hasProperty(PropertyKey.FLUID_PIPE)) {
            FluidPipeProperties property = mat.getProperty(PropertyKey.FLUID_PIPE);
            event.registerItem(Capabilities.Fluid.ITEM,
                    (stack, access) -> new ThermalFluidHandlerItemStack(access,
                            GTMachineUtils.DRUM_CAPACITY.getInt(getDefinition()),
                            property.getMaxFluidTemperature(), property.isGasProof(), property.isAcidProof(),
                            property.isCryoProof(),
                            property.isPlasmaProof()),
                    this);
        } else {
            event.registerItem(Capabilities.Fluid.ITEM,
                    (stack, access) -> new ItemAccessFluidHandler(access,GTDataComponents.FLUID_CONTENT.get(),
                            GTMachineUtils.DRUM_CAPACITY.getInt(getDefinition())),
                    this);
        }
    }
}
