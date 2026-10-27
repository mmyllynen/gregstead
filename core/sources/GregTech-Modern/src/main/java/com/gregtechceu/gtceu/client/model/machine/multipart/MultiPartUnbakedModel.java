package com.gregtechceu.gtceu.client.model.machine.multipart;

import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.client.model.machine.MachineModelLoader;
import com.gregtechceu.gtceu.client.model.machine.MachineRenderState;
import com.gregtechceu.gtceu.client.model.machine.variant.MultiVariantModel;

import net.minecraft.client.resources.model.*;
import net.minecraft.client.renderer.block.dispatch.ModelState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.state.StateDefinition;

import com.google.gson.*;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public record MultiPartUnbakedModel(StateDefinition<MachineDefinition, MachineRenderState> definition,
                                    List<MultiPartSelector> selectors)
        implements ResolvableModel {

    public Set<MultiVariantModel> getModels() {
        Set<MultiVariantModel> set = new HashSet<>();

        for (MultiPartSelector selector : this.selectors()) {
            set.add(selector.getVariant());
        }
        return set;
    }

    @Override
    public void resolveDependencies(Resolver resolver) {
        selectors.forEach(selector -> selector.getVariant().resolveDependencies(resolver));
    }

    public NativeMachineParts<MachineRenderState> bake(ModelBaker baker, ModelState state, ModelDebugName debugName) {
        var parts = new ArrayList<NativeMachineParts.Selector<MachineRenderState>>();
        for (MultiPartSelector selector : selectors) {
            parts.add(new NativeMachineParts.Selector<>(selector.getPredicate(definition),
                    selector.getVariant().bake(baker, state, debugName)));
        }
        return new NativeMachineParts<>(parts);
    }

    public static MultiPartUnbakedModel deserialize(MachineDefinition definition, JsonArray elements) {
        return new MultiPartUnbakedModel(definition.getStateDefinition(), getSelectors(elements));
    }

    private static List<MultiPartSelector> getSelectors(JsonArray elements) {
        List<MultiPartSelector> list = new ArrayList<>();

        for (JsonElement e : elements) {
            list.add(MachineModelLoader.GSON.fromJson(e, MultiPartSelector.class));
        }
        return list;
    }
}
