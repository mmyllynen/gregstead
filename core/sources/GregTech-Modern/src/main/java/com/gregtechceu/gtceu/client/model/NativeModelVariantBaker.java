package com.gregtechceu.gtceu.client.model;

import com.gregtechceu.gtceu.client.model.pipe.UnbakedPipeModel;

import com.google.gson.*;
import com.mojang.datafixers.util.Either;
import com.mojang.math.Transformation;
import com.mojang.serialization.JsonOps;
import net.minecraft.client.renderer.block.dispatch.*;
import net.minecraft.client.resources.model.*;
import net.minecraft.client.resources.model.cuboid.CuboidModel;
import net.minecraft.core.BlockMath;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.random.Weighted;
import net.minecraft.util.random.WeightedList;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import java.util.*;

/** Parses and bakes weighted named/inline model variants with native model infrastructure. */
public final class NativeModelVariantBaker {
    private record Entry(Either<Identifier,UnbakedModel> model,Variant.SimpleModelState state,int weight) {}
    private final List<Entry> entries;
    private NativeModelVariantBaker(List<Entry> entries){this.entries=List.copyOf(entries);}
    public static NativeModelVariantBaker read(JsonElement json) {
        var entries=new ArrayList<Entry>();
        if(json.isJsonArray()) {
            if(json.getAsJsonArray().isEmpty())throw new JsonParseException("Empty variant array");
            for(var value:json.getAsJsonArray())entries.add(readEntry(value));
        }else entries.add(readEntry(json));
        return new NativeModelVariantBaker(entries);
    }
    private static Entry readEntry(JsonElement json) {
        if(!json.isJsonObject())throw new JsonParseException("Variant must be an object");
        var object=json.getAsJsonObject();var model=object.get("model");
        Either<Identifier,UnbakedModel> source;
        if(model!=null && model.isJsonPrimitive() && model.getAsJsonPrimitive().isString())source=Either.left(Identifier.parse(model.getAsString()));
        else if(model!=null && model.isJsonObject())source=Either.right(CuboidModel.GSON.fromJson(model,UnbakedModel.class));
        else throw new JsonParseException("Variant requires a named or inline model");
        int weight=GsonHelper.getAsInt(object,"weight",1);
        if(weight<1)throw new JsonParseException("Invalid variant weight");
        var state=Variant.SimpleModelState.MAP_CODEC.codec().parse(JsonOps.INSTANCE,object)
                .getOrThrow(message -> new JsonParseException(message));
        return new Entry(source,state,weight);
    }
    public void resolveDependencies(ResolvableModel.Resolver resolver) {
        for(var entry:entries)entry.model().ifLeft(resolver::markDependency).ifRight(model -> {
            if(model.parent()!=null)resolver.markDependency(model.parent());
            model.resolveDependencies(resolver);
        });
    }
    public BlockStateModel bake(ModelBaker baker,ModelState outer,ModelDebugName debugName) {
        var weighted=new ArrayList<Weighted<BlockStateModel>>();
        for(var entry:entries) {
            var resolved=entry.model().map(baker::getModel,model -> baker.resolveInlineModel(model,debugName));
            Transformation rotation=outer.transformation().compose(entry.state().asModelState().transformation());
            ModelState actual=new ModelState() {
                public Transformation transformation(){return rotation;}
                public Matrix4fc faceTransformation(Direction face){return entry.state().uvLock()
                        ? BlockMath.getFaceTransformation(rotation,face).getMatrix():NO_TRANSFORM;}
                public Matrix4fc inverseFaceTransformation(Direction face){return entry.state().uvLock()
                        ? faceTransformation(face).invertAffine(new Matrix4f()):NO_TRANSFORM;}
            };
            var pipe=UnbakedPipeModel.find(resolved);
            BlockStateModel model=pipe!=null?pipe.bakeDynamic(baker,actual,resolved):new SingleVariant(SimpleModelWrapper.bake(baker,resolved,actual));
            weighted.add(new Weighted<>(model,entry.weight()));
        }
        return weighted.size()==1?weighted.getFirst().value():new WeightedVariants(WeightedList.of(weighted));
    }
}
