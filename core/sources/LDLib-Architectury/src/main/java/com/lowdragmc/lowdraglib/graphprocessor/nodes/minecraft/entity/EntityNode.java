package com.lowdragmc.lowdraglib.graphprocessor.nodes.minecraft.entity;

import com.lowdragmc.lowdraglib.LDLib;
import com.lowdragmc.lowdraglib.registry.annotation.LDLRegister;
import com.lowdragmc.lowdraglib.editor.configurator.ConfiguratorGroup;
import com.lowdragmc.lowdraglib.editor.configurator.SelectorConfigurator;
import com.lowdragmc.lowdraglib.graphprocessor.annotation.InputPort;
import com.lowdragmc.lowdraglib.graphprocessor.annotation.OutputPort;
import com.lowdragmc.lowdraglib.graphprocessor.data.BaseNode;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;

@LDLRegister(name = "entity type", group = "graph_processor.node.minecraft", registry = "ldlib:graph_node")
public class EntityNode extends BaseNode {
    @InputPort
    public Object in = null;
    @OutputPort(name = "entity type")
    public EntityType<?> out = null;

    public Identifier internalValue = Identifier.withDefaultNamespace("pig");

    @Override
    public int getMinWidth() {
        return 100;
    }

    @Override
    public void process() {
        if (in == null) {
            out = BuiltInRegistries.ENTITY_TYPE.getValue(internalValue);
        } else if (in instanceof EntityType<?> entityType) {
            out = entityType;
        } else {
            var name = in.toString();
            if (LDLib.isValidResourceLocation(name)) {
                out = BuiltInRegistries.ENTITY_TYPE.getValue(Identifier.parse(name));
            } else {
                out = null;
            }
        }
    }

    @Override
    public void buildConfigurator(ConfiguratorGroup father) {
        super.buildConfigurator(father);
        for (var port : getInputPorts()) {
            if (port.fieldName.equals("in")) {
                if (!port.getEdges().isEmpty()) return;
            }
        }
        var candidates = BuiltInRegistries.ENTITY_TYPE.keySet().stream().toList();
        father.addConfigurators(new SelectorConfigurator<>("",
                () -> internalValue,
                type -> internalValue = type,
                Identifier.withDefaultNamespace("pig"),
                true,
                candidates,
                Identifier::toString));
    }
}
