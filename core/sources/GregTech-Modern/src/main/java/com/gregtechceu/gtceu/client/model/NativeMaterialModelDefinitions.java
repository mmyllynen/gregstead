package com.gregtechceu.gtceu.client.model;

import com.google.gson.*;
import java.util.*;
import java.util.function.Function;
import net.minecraft.resources.Identifier;

/** Native translucent material overrides; solid/cutout are classified from sprite alpha by the engine. */
public final class NativeMaterialModelDefinitions {
    private NativeMaterialModelDefinitions() {}
    public static JsonObject parent(Identifier model,Identifier layer,Function<Identifier,JsonObject> resources) {
        var result=new JsonObject();result.addProperty("parent",model.toString());
        if(layer.equals(Identifier.withDefaultNamespace("translucent"))) {
            var slots=new LinkedHashMap<String,JsonElement>();collect(model,resources,slots,new HashSet<>());
            var textures=new JsonObject();
            for(var entry:slots.entrySet()) {
                var value=resolve(entry.getKey(),slots,new HashSet<>());
                if(value==null)throw new JsonParseException("Unresolved material texture: "+entry.getKey()+" in "+model);
                var material=value.isJsonObject()?value.getAsJsonObject().deepCopy():new JsonObject();
                if(!value.isJsonObject())material.add("sprite",value.deepCopy());
                material.addProperty("force_translucent",true);textures.add(entry.getKey(),material);
            }
            if(!textures.isEmpty())result.add("textures",textures);
        }
        return result;
    }
    private static void collect(Identifier id,Function<Identifier,JsonObject> resources,Map<String,JsonElement> slots,Set<Identifier> visiting) {
        if(!visiting.add(id))throw new JsonParseException("Cyclic material parent: "+id);
        var model=Objects.requireNonNull(resources.apply(id),"Missing material parent: "+id);
        if(model.has("parent"))collect(Identifier.parse(model.get("parent").getAsString()),resources,slots,visiting);
        if(model.has("textures"))model.getAsJsonObject("textures").asMap().forEach((key,value)->slots.put(key,value.deepCopy()));
        visiting.remove(id);
    }
    private static JsonElement resolve(String key,Map<String,JsonElement> slots,Set<String> visiting) {
        if(!visiting.add(key))throw new JsonParseException("Cyclic material texture: "+key);
        var value=slots.get(key);
        if(value!=null && value.isJsonPrimitive() && value.getAsString().startsWith("#"))return resolve(value.getAsString().substring(1),slots,visiting);
        return value;
    }
}
