package com.gregtechceu.gtceu.client.model.machine.variant;

import com.gregtechceu.gtceu.client.model.NativeModelVariantBaker;
import com.google.gson.*;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.ModelState;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelDebugName;
import net.minecraft.client.resources.model.ResolvableModel;
import java.lang.reflect.Type;

/** Machine variants use the same native weighted named/inline baking as pipe parts. */
public final class MultiVariantModel implements ResolvableModel {
    private final JsonElement source;
    private final NativeModelVariantBaker variants;
    public MultiVariantModel(JsonElement source) {
        this.source = source.deepCopy();
        this.variants = NativeModelVariantBaker.read(source);
    }
    @Override public void resolveDependencies(Resolver resolver) { variants.resolveDependencies(resolver); }
    public BlockStateModel bake(ModelBaker baker, ModelState state, ModelDebugName debugName) {
        return variants.bake(baker, state, debugName);
    }
    @Override public boolean equals(Object other) { return other instanceof MultiVariantModel model && source.equals(model.source); }
    @Override public int hashCode() { return source.hashCode(); }
    public static final class Deserializer implements JsonDeserializer<MultiVariantModel> {
        @Override public MultiVariantModel deserialize(JsonElement json, Type type, JsonDeserializationContext context) {
            return new MultiVariantModel(json);
        }
    }
}
