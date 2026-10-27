package com.gregtechceu.gtceu.data.model.builder;

import com.google.gson.*;
import net.minecraft.resources.Identifier;
import java.util.List;

/** Loader-facing named or inline model variant, independent of removed NeoForge datagen types. */
public record WeightedModelVariant(JsonElement model,int x,int y,boolean uvLock,int weight) {
    public WeightedModelVariant {
        if(model==null || !(model.isJsonPrimitive() && model.getAsJsonPrimitive().isString() || model.isJsonObject()))
            throw new IllegalArgumentException("Model must be named or inline");
        if(weight<1 || x<0 || x>=360 || x%90!=0 || y<0 || y>=360 || y%90!=0)
            throw new IllegalArgumentException("Invalid model weight or rotation");
        model=model.deepCopy();
    }
    public WeightedModelVariant(Identifier model){this(new JsonPrimitive(model.toString()),0,0,false,1);}
    @Override public JsonElement model(){return model.deepCopy();}
    public WeightedModelVariant rotate(int x,int y){return new WeightedModelVariant(model,x,y,uvLock,weight);}
    public WeightedModelVariant withUvLock(boolean lock) {
        return new WeightedModelVariant(model, x, y, lock, weight);
    }
    public WeightedModelVariant withInlineParent(Identifier parent) {
        if (!model.isJsonObject()) throw new IllegalStateException("Parent replacement requires an inline model");
        JsonObject inline = model.getAsJsonObject().deepCopy();
        inline.addProperty("parent", parent.toString());
        return new WeightedModelVariant(inline, x, y, uvLock, weight);
    }
    public JsonObject toJson(boolean includeWeight) {
        var json=new JsonObject();json.add("model",model.deepCopy());
        if(x!=0)json.addProperty("x",x); if(y!=0)json.addProperty("y",y);
        if(uvLock)json.addProperty("uvlock",true);if(includeWeight && weight!=1)json.addProperty("weight",weight);
        return json;
    }
    public static JsonElement toJson(List<WeightedModelVariant> models) {
        if(models.size()==1)return models.getFirst().toJson(false);
        var json=new JsonArray();models.forEach(model -> json.add(model.toJson(true)));return json;
    }
}
