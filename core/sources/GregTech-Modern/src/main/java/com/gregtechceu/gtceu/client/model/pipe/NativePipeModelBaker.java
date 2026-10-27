package com.gregtechceu.gtceu.client.model.pipe;
import com.gregtechceu.gtceu.client.model.NativeModelVariantBaker;

import net.minecraft.client.renderer.block.dispatch.*;
import net.minecraft.client.resources.model.*;
import net.minecraft.core.Direction;
import java.util.*;

/** Dependency-aware native bake pipeline for original pipe model definitions. */
public final class NativePipeModelBaker {
    private final Map<Direction,NativeModelVariantBaker> parts=new IdentityHashMap<>();
    private final Map<Direction,NativeModelVariantBaker> restrictors=new IdentityHashMap<>();
    public NativePipeModelBaker(PipeModelDefinitions definitions) {
        definitions.parts().forEach((direction,json)->parts.put(direction,NativeModelVariantBaker.read(json)));
        definitions.restrictors().forEach((direction,json)->restrictors.put(direction,NativeModelVariantBaker.read(json)));
    }
    public void resolveDependencies(ResolvableModel.Resolver resolver) {
        parts.values().forEach(model->model.resolveDependencies(resolver));
        restrictors.values().forEach(model->model.resolveDependencies(resolver));
    }
    public NativePipeBlockStateModel bake(ModelBaker baker,ModelState state,ModelDebugName name) {
        var bakedParts=new IdentityHashMap<Direction,BlockStateModel>();
        var bakedRestrictors=new IdentityHashMap<Direction,BlockStateModel>();
        parts.forEach((direction,model)->bakedParts.put(direction,model.bake(baker,state,name)));
        restrictors.forEach((direction,model)->bakedRestrictors.put(direction,model.bake(baker,state,name)));
        return new NativePipeBlockStateModel(bakedParts,bakedRestrictors,new SingleVariant(baker.missingBlockModelPart()));
    }
}
