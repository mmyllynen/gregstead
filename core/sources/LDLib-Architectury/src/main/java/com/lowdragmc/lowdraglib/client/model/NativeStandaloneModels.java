package com.lowdragmc.lowdraglib.client.model;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.dispatch.ModelState;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.resources.model.*;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.model.standalone.SimpleUnbakedStandaloneModel;
import net.neoforged.neoforge.client.model.standalone.StandaloneModelKey;
import java.util.concurrent.ConcurrentHashMap;

/** Stable identity keys for extra models and access to the current reload's resolved model/baker. */
public final class NativeStandaloneModels {
    public record Loaded(ResolvedModel model,ModelBaker baker) {
        public BlockStateModelPart bake(ModelState state){return SimpleModelWrapper.bake(baker,model,state);}
    }
    private static final ConcurrentHashMap<Identifier,StandaloneModelKey<Loaded>> KEYS=new ConcurrentHashMap<>();
    private NativeStandaloneModels() {}
    public static StandaloneModelKey<Loaded> key(Identifier id) {
        return KEYS.computeIfAbsent(id,value->new StandaloneModelKey<>(value::toString));
    }
    public static void register(ModelEvent.RegisterStandalone event,Identifier id) {
        event.register(key(id),new SimpleUnbakedStandaloneModel<>(id,(model,baker,name)->new Loaded(model,baker)));
    }
    public static Loaded get(Identifier id){return Minecraft.getInstance().getModelManager().getStandaloneModel(key(id));}
}
