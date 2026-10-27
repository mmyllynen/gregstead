package com.gregtechceu.gtceu.client.model.pipe;

import net.minecraft.client.renderer.block.dispatch.ModelState;
import net.minecraft.client.resources.model.*;
import net.neoforged.neoforge.client.model.AbstractUnbakedModel;
import net.neoforged.neoforge.client.model.StandardModelParameters;
import net.neoforged.neoforge.client.model.NeoForgeModelProperties;
import net.neoforged.neoforge.client.model.UnbakedElementsHelper;
import com.mojang.math.Transformation;

/** Native pipe definition; dynamic block rendering is supplied through its blockstate bake hook. */
public class UnbakedPipeModel extends AbstractUnbakedModel {
    private final NativePipeModelBaker models;
    public UnbakedPipeModel(StandardModelParameters parameters,PipeModelDefinitions definitions) {
        super(parameters);models=new NativePipeModelBaker(definitions);
    }
    @Override public void resolveDependencies(ResolvableModel.Resolver resolver) {
        if(parent()!=null)resolver.markDependency(parent());
        models.resolveDependencies(resolver);
    }
    public NativePipeBlockStateModel bakeDynamic(ModelBaker baker,ModelState state,ModelDebugName name) {
        Transformation root=name instanceof ResolvedModel resolved
                ? resolved.getTopAdditionalProperties().getOrDefault(NeoForgeModelProperties.TRANSFORM,Transformation.IDENTITY)
                : parameters.rootTransform()==null?Transformation.IDENTITY:parameters.rootTransform();
        return models.bake(baker,UnbakedElementsHelper.composeRootTransformIntoModelState(state,root),name);
    }
    public static UnbakedPipeModel find(ResolvedModel resolved) {
        while(resolved!=null) {
            if(resolved.wrapped() instanceof UnbakedPipeModel pipe)return pipe;
            if(resolved.wrapped().geometry()!=null)return null;
            resolved=resolved.parent();
        }
        return null;
    }
}
