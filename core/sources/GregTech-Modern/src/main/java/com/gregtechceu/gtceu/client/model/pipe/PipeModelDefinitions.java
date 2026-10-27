package com.gregtechceu.gtceu.client.model.pipe;

import com.google.gson.*;
import net.minecraft.core.Direction;
import net.minecraft.util.GsonHelper;
import java.util.*;

/** Immutable loader payloads shared by the pipe geometry and native blockstate-model port. */
public final class PipeModelDefinitions {
    private static final Set<String> CENTERS=Set.of("center","core","null","none");
    private final Map<Direction,JsonElement> parts;
    private final Map<Direction,JsonElement> restrictors;
    private PipeModelDefinitions(Map<Direction,JsonElement> parts,Map<Direction,JsonElement> restrictors) {
        this.parts=parts;this.restrictors=restrictors;
    }
    public static PipeModelDefinitions read(JsonObject json) {
        return new PipeModelDefinitions(readMap(json,"parts",true),readMap(json,"restrictors",false));
    }
    private static Map<Direction,JsonElement> readMap(JsonObject json,String key,boolean centers) {
        var result=new IdentityHashMap<Direction,JsonElement>();
        if(!json.has(key))return result;
        if(!json.get(key).isJsonObject())throw new JsonParseException("Pipe "+key+" must be an object");
        for(var entry:json.getAsJsonObject(key).entrySet()) {
            var direction=Direction.byName(entry.getKey());
            if(direction==null && !(centers && CENTERS.contains(entry.getKey().toLowerCase(Locale.ROOT))))
                throw new JsonParseException("Invalid pipe model part specifier "+entry.getKey());
            if(result.containsKey(direction))throw new JsonParseException("Cannot specify more than one center model for a pipe model");
            var value=entry.getValue();
            if(value.isJsonArray()) {
                if(value.getAsJsonArray().isEmpty())throw new JsonParseException("Empty variant array");
                for(var variant:value.getAsJsonArray())validateVariant(variant);
            } else validateVariant(value);
            result.put(direction,value.deepCopy());
        }
        return result;
    }
    private static void validateVariant(JsonElement json) {
        if(!json.isJsonObject())throw new JsonParseException("Pipe variant must be an object");
        var object=json.getAsJsonObject();var model=object.get("model");
        if(model==null || !(model.isJsonObject() || model.isJsonPrimitive() && model.getAsJsonPrimitive().isString()))
            throw new JsonParseException("Pipe variant requires a named or inline model");
        if(object.has("weight") && GsonHelper.getAsInt(object,"weight")<1)throw new JsonParseException("Invalid weight");
        for(String key:new String[]{"x","y","gtceu:z"})if(object.has(key)) {
            int angle=GsonHelper.getAsInt(object,key);
            if(angle<0 || angle>=360 || angle%90!=0)throw new JsonParseException("Invalid rotation "+angle);
        }
    }
    private static Map<Direction,JsonElement> copy(Map<Direction,JsonElement> source) {
        var result=new IdentityHashMap<Direction,JsonElement>();source.forEach((direction,value)->result.put(direction,value.deepCopy()));
        return Collections.unmodifiableMap(result);
    }
    public Map<Direction,JsonElement> parts(){return copy(parts);}
    public Map<Direction,JsonElement> restrictors(){return copy(restrictors);}
}
