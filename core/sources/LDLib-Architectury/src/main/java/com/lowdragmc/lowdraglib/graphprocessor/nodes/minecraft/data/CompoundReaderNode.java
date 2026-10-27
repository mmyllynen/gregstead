package com.lowdragmc.lowdraglib.graphprocessor.nodes.minecraft.data;

import com.lowdragmc.lowdraglib.editor.annotation.ConfigSetter;
import com.lowdragmc.lowdraglib.editor.annotation.Configurable;
import com.lowdragmc.lowdraglib.registry.annotation.LDLRegister;
import com.lowdragmc.lowdraglib.editor.configurator.ConfiguratorGroup;
import com.lowdragmc.lowdraglib.editor.configurator.StringConfigurator;
import com.lowdragmc.lowdraglib.graphprocessor.annotation.CustomPortBehavior;
import com.lowdragmc.lowdraglib.graphprocessor.annotation.CustomPortOutput;
import com.lowdragmc.lowdraglib.graphprocessor.annotation.InputPort;
import com.lowdragmc.lowdraglib.graphprocessor.annotation.OutputPort;
import com.lowdragmc.lowdraglib.graphprocessor.data.BaseNode;
import com.lowdragmc.lowdraglib.graphprocessor.data.NodePort;
import com.lowdragmc.lowdraglib.graphprocessor.data.PortData;
import com.lowdragmc.lowdraglib.graphprocessor.data.PortEdge;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import net.minecraft.nbt.*;

import java.util.List;

@LDLRegister(name = "compound reader", group = "graph_processor.node.minecraft.data", registry = "ldlib:graph_node")
public class CompoundReaderNode extends BaseNode {
    public enum Type {
        STRING,
        NUMBER,
        BOOL,
        COMPOUND,
        LIST,

    }
    @InputPort
    public CompoundTag tag;
    @InputPort
    public String key;
    @OutputPort
    public Object out;
    @Configurable(showName = false)
    public Type type = Type.STRING;
    @Persisted
    public String internalKey = "";

    @Override
    public void buildConfigurator(ConfiguratorGroup father) {
        super.buildConfigurator(father);
        for (var port : getInputPorts()) {
            if (port.fieldName.equals("key")) {
                if (port.getEdges().isEmpty()) {
                    var stringConfigurator = new StringConfigurator("key", () -> internalKey, s -> internalKey = s, "", true);
                    father.addConfigurators(stringConfigurator);
                }
            }
        }
    }

    @Override
    public int getMinWidth() {
        return 120;
    }

    @Override
    public void process() {
        if (tag == null) {
            out = null;
            return;
        }
        var realKey = key;
        if (realKey == null) {
            realKey = internalKey;
        }
        if (type == Type.STRING) {
            out = tag.getStringOr(realKey, "");
        } else if (type == Type.NUMBER) {
            out = tag.getFloatOr(realKey, 0.0F);
        } else if (type == Type.BOOL) {
            out = tag.getBooleanOr(realKey, false);
        } else if (type == Type.COMPOUND) {
            out = tag.getCompoundOrEmpty(realKey);
        } else if (type == Type.LIST) {
            if (tag.get(realKey) instanceof ListTag listTag) {
                out = com.lowdragmc.lowdraglib.transfer.persistence.NbtCompatibility.graphListValues(listTag);
            }
        }
    }

    public Class<?> getDisplayType() {
        return switch (type) {
            case STRING -> String.class;
            case NUMBER -> Float.class;
            case BOOL -> Boolean.class;
            case COMPOUND -> CompoundTag.class;
            case LIST -> List.class;
        };
    }

    @ConfigSetter(field = "type")
    public void setType(Type type) {
        this.type = type;
        for (var outputPort : getOutputPorts()) {
            outputPort.portData.displayType = getDisplayType();
        }
    }

    @CustomPortBehavior(field = "out")
    public List<PortData> modifyOutPort(List<PortEdge> edges) {
        return List.of(new PortData()
                .displayName("out")
                .identifier("out")
                .acceptMultipleEdges(true)
                .displayType(getDisplayType()));
    }

    @CustomPortOutput(field = "out")
    public void pushOut(List<PortEdge> outputEdges, NodePort outputPort) {
        for (var edge : outputEdges) {
            edge.passThroughBuffer = out;
        }
    }



}
