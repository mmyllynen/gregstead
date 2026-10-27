package com.gregtechceu.gtceu.data.model.builder;

import com.gregtechceu.gtceu.api.registry.registrate.provider.GTBlockstateProvider;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.model.generators.template.CustomLoaderBuilder;
import com.google.gson.JsonObject;
import java.util.*;
import java.util.function.Supplier;

public class PipeModelBuilder extends CustomLoaderBuilder {
    private final GTBlockstateProvider provider;
    private final float thickness;
    private final Map<Direction,List<WeightedModelVariant>> parts=new IdentityHashMap<>();
    private final Identifier[] restrictors;

    public static Supplier<PipeModelBuilder> begin(float thickness,GTBlockstateProvider provider) {
        return () -> new PipeModelBuilder(thickness,provider);
    }
    public PipeModelBuilder(float thickness,GTBlockstateProvider provider) {
        super(Identifier.parse("gtceu:pipe"),true);
        this.thickness=thickness;this.provider=provider;
        restrictors=NativePipeRestrictorModels.get(provider,thickness);
    }
    public PipeModelBuilder allModels(WeightedModelVariant center,WeightedModelVariant... connections) {
        return centerModels(center).connectionModels(connections);
    }
    public PipeModelBuilder allModels(Identifier center,Identifier... connections) {
        return centerModels(center).connectionModels(connections);
    }
    public PipeModelBuilder centerModels(WeightedModelVariant... models){return modelsForDirection(null,models);}
    public PipeModelBuilder centerModels(Identifier... models){return modelsForDirection(null,models);}
    public PipeModelBuilder modelsForDirection(Direction direction,WeightedModelVariant... models) {
        parts.put(direction,List.of(models));return this;
    }
    public PipeModelBuilder modelsForDirection(Direction direction,Identifier... models) {
        return modelsForDirection(direction,Arrays.stream(models).map(WeightedModelVariant::new).toArray(WeightedModelVariant[]::new));
    }
    // Original pre-configured connection models are based on NORTH; named models are based on DOWN.
    public PipeModelBuilder connectionModels(WeightedModelVariant... models) {
        for(var direction:Direction.values()) {
            int x=direction==Direction.DOWN?90:direction==Direction.UP?270:0;
            int y=direction.getAxis().isVertical()?0:((int)direction.toYRot()+180)%360;
            modelsForDirection(direction,Arrays.stream(models).map(model -> model.rotate(x,y)).toArray(WeightedModelVariant[]::new));
        }
        return this;
    }
    public PipeModelBuilder connectionModels(Identifier... models) {
        for(var direction:Direction.values()) {
            int x=direction==Direction.DOWN?0:direction==Direction.UP?180:90;
            int y=direction.getAxis().isVertical()?0:(int)direction.toYRot();
            modelsForDirection(direction,Arrays.stream(models).map(model -> new WeightedModelVariant(model).rotate(x,y)).toArray(WeightedModelVariant[]::new));
        }
        return this;
    }
    public Map<Direction,List<WeightedModelVariant>> getParts(){return Collections.unmodifiableMap(parts);}
    @Override protected CustomLoaderBuilder copyInternal() {
        var copy=new PipeModelBuilder(thickness,provider);copy.parts.putAll(parts);return copy;
    }
    @Override public JsonObject toJson(JsonObject json) {
        json=super.toJson(json);
        if(!parts.isEmpty()) {
            var object=new JsonObject();
            parts.entrySet().stream().sorted(Map.Entry.comparingByKey(Comparator.nullsFirst(Direction::compareTo)))
                    .forEach(entry -> object.add(entry.getKey()==null?"center":entry.getKey().getSerializedName(),
                            WeightedModelVariant.toJson(entry.getValue())));
            json.add("parts",object);
        }
        var object=new JsonObject();
        for(var direction:Direction.values())object.add(direction.getSerializedName(),
                new WeightedModelVariant(restrictors[direction.ordinal()]).toJson(false));
        json.add("restrictors",object);return json;
    }
    public static void clearRestrictorModelCache(){NativePipeRestrictorModels.clear();}
}
