package com.lowdragmc.lowdraglib.graphprocessor.nodes.minecraft.fluid;

import com.lowdragmc.lowdraglib.Platform;
import com.lowdragmc.lowdraglib.editor.annotation.Configurable;
import com.lowdragmc.lowdraglib.registry.annotation.LDLRegister;
import com.lowdragmc.lowdraglib.editor.configurator.ConfiguratorGroup;
import com.lowdragmc.lowdraglib.graphprocessor.annotation.InputPort;
import com.lowdragmc.lowdraglib.graphprocessor.annotation.OutputPort;
import com.lowdragmc.lowdraglib.graphprocessor.data.BaseNode;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.neoforged.neoforge.fluids.FluidStack;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

@LDLRegister(name = "fluidstack", group = "graph_processor.node.minecraft.fluid", registry = "ldlib:graph_node")
public class FluidStackNode extends BaseNode {
    @InputPort
    public Object in;
    @InputPort
    public Fluid fluid;
    @InputPort
    public Integer amount;
    @InputPort
    public DataComponentPatch components;
    @OutputPort
    public FluidStack out;
    @Configurable(name = "fluidstack", canCollapse = false, collapse = false)
    public FluidStack internalValue = new FluidStack(Fluids.WATER, 1000);

    @Override
    public int getMinWidth() {
        return 100;
    }

    @Override
    public void process() {
        out = com.lowdragmc.lowdraglib.graphprocessor.nodes.minecraft.NativeGraphStacks.fluid(
                in, internalValue, fluid, amount, components, Platform.getFrozenRegistry());
    }

    @Override
    public void buildConfigurator(ConfiguratorGroup father) {
        for (var port : getInputPorts()) {
            if (port.fieldName.equals("in")) {
                if (!port.getEdges().isEmpty()) return;
            }
        }
        super.buildConfigurator(father);
    }
}
