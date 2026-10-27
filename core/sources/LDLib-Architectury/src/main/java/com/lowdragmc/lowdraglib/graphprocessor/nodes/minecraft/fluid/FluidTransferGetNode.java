package com.lowdragmc.lowdraglib.graphprocessor.nodes.minecraft.fluid;

import com.lowdragmc.lowdraglib.registry.annotation.LDLRegister;
import com.lowdragmc.lowdraglib.graphprocessor.annotation.InputPort;
import com.lowdragmc.lowdraglib.graphprocessor.annotation.OutputPort;
import com.lowdragmc.lowdraglib.graphprocessor.data.trigger.LinearTriggerNode;
import com.lowdragmc.lowdraglib.utils.Vector3fHelper;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.Capabilities;
import com.lowdragmc.lowdraglib.transfer.fluid.IFluidHandler;
import org.joml.Vector3f;

@LDLRegister(name = "fluid transfer get", group = "graph_processor.node.minecraft.fluid", registry = "ldlib:graph_node")
public class FluidTransferGetNode extends LinearTriggerNode {
    @InputPort
    public Level level;
    @InputPort
    public Vector3f xyz;
    @InputPort(name = "direction")
    public Direction direction;
    @OutputPort(name = "fluid transfer")
    public IFluidHandler fluidTransfer;

    @Override
    public void process() {
        if (level != null && xyz != null) {
            var pos = Vector3fHelper.toBlockPos(xyz);
            var nativeHandler = level.getCapability(Capabilities.Fluid.BLOCK, pos, direction);
            fluidTransfer = nativeHandler == null ? null : com.lowdragmc.lowdraglib.transfer.fluid.ResourceFluidHandler.of(nativeHandler);
        }
    }
}
