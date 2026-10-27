package com.gregtechceu.gtceu.client.model.machine;

import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.client.model.machine.multipart.MultiPartUnbakedModel;
import com.gregtechceu.gtceu.client.model.machine.variant.MultiVariantModel;
import com.gregtechceu.gtceu.client.renderer.machine.DynamicRender;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.*;
import net.minecraft.client.resources.model.*;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.model.AbstractUnbakedModel;
import net.neoforged.neoforge.client.model.StandardModelParameters;
import lombok.Getter;
import java.util.*;
import java.util.function.BiFunction;

/** Native machine definition retains variant/multipart/dynamic-render and casing metadata. */
public class UnbakedMachineModel extends AbstractUnbakedModel {
    @Getter private final MachineDefinition definition;
    @Getter private final Map<MachineRenderState, MultiVariantModel> models;
    @Getter private final MultiPartUnbakedModel multiPart;
    @Getter private final List<DynamicRender<?, ?>> dynamicRenders;
    @Getter private final Set<String> replaceableTextures;
    @Getter private final Map<String, Identifier> textureOverrides;
    public UnbakedMachineModel(StandardModelParameters parameters, MachineDefinition definition,
            Map<MachineRenderState, MultiVariantModel> models, MultiPartUnbakedModel multipart,
            List<DynamicRender<?, ?>> renders, Set<String> replaceableTextures, Map<String, Identifier> overrides) {
        super(parameters);
        this.definition=definition; this.models=Collections.unmodifiableMap(new IdentityHashMap<>(models));
        this.multiPart=multipart; this.dynamicRenders=List.copyOf(renders);
        this.dynamicRenders.forEach(render -> render.setDefinition(definition));
        this.replaceableTextures=Set.copyOf(replaceableTextures); this.textureOverrides=Map.copyOf(overrides);
    }
    @Override public void resolveDependencies(ResolvableModel.Resolver resolver) {
        if(parent()!=null) resolver.markDependency(parent());
        models.values().forEach(model -> { if (model != MachineModelLoader.MISSING_MARKER) model.resolveDependencies(resolver); });
        if(multiPart!=null) multiPart.resolveDependencies(resolver);
    }
    public BlockStateModel bakeDynamic(ModelBaker baker, ModelState state, ModelDebugName name) {
        var root = name instanceof ResolvedModel resolved
                ? resolved.getTopAdditionalProperties().getOrDefault(net.neoforged.neoforge.client.model.NeoForgeModelProperties.TRANSFORM,
                        com.mojang.math.Transformation.IDENTITY)
                : parameters.rootTransform() == null ? com.mojang.math.Transformation.IDENTITY : parameters.rootTransform();
        var base = bakeNativeBase(baker, net.neoforged.neoforge.client.model.UnbakedElementsHelper
                .composeRootTransformIntoModelState(state, root), name,
                (level, pos) -> level.getModelData(pos).get(MachineModelProperties.RENDER_STATE));
        var materials = baker.materials();
        Map<String, net.minecraft.client.renderer.texture.TextureAtlasSprite> sprites = new LinkedHashMap<>();
        textureOverrides.forEach((key,id) -> sprites.put(key, materials.get(new net.minecraft.client.resources.model.sprite.Material(id), name).sprite()));
        var textured = new NativeMachineTextureModel(base, replaceableTextures, sprites,
                (level,pos) -> level.getModelData(pos).get(MachineModelProperties.FORMED_TEXTURES));
        var controller = new NativeControllerGeometryModel(textured, replaceableTextures,
                materials.get(new net.minecraft.client.resources.model.sprite.Material(Identifier.parse("gtceu:block/void")),name).sprite(),
                (level,pos) -> level.getModelData(pos).get(MachineModelProperties.CONTROLLER_GEOMETRY));
        var output = new NativeMachineOutputModel(controller,
                materials.get(new net.minecraft.client.resources.model.sprite.Material(Identifier.parse("gtceu:block/overlay/machine/overlay_pipe")), name).sprite(),
                materials.get(new net.minecraft.client.resources.model.sprite.Material(Identifier.parse("gtceu:block/overlay/machine/overlay_item_output")), name).sprite(),
                materials.get(new net.minecraft.client.resources.model.sprite.Material(Identifier.parse("gtceu:block/overlay/machine/overlay_fluid_output")), name).sprite(),
                (level, pos) -> level.getModelData(pos).get(MachineModelProperties.OUTPUT));
        var covered = new com.gregtechceu.gtceu.client.model.NativeSnapshotPartsModel(output,
                (level, pos) -> level.getModelData(pos).get(MachineModelProperties.COVERS),
                net.minecraft.client.resources.model.geometry.BakedQuad.FLAG_ANIMATED |
                        net.minecraft.client.resources.model.geometry.BakedQuad.FLAG_TRANSLUCENT);
        return new NativeMachineRenderMetadata(new NativeMachineTextureModel(covered, replaceableTextures, sprites, (level,pos) -> Map.of()), dynamicRenders);
    }
    public static UnbakedMachineModel find(ResolvedModel resolved) {
        while (resolved != null) {
            if (resolved.wrapped() instanceof UnbakedMachineModel machine) return machine;
            if (resolved.wrapped().geometry() != null) return null;
            resolved = resolved.parent();
        }
        return null;
    }
    public NativeMachineBaseModel<MachineRenderState> bakeNativeBase(ModelBaker baker, ModelState state,
            ModelDebugName debugName, BiFunction<BlockAndTintGetter, BlockPos, MachineRenderState> stateLookup) {
        Map<MachineRenderState, BlockStateModel> baked=new IdentityHashMap<>();
        models.forEach((machineState,model) -> baked.put(machineState, model == MachineModelLoader.MISSING_MARKER
                ? new SingleVariant(baker.missingBlockModelPart()) : model.bake(baker,state,debugName)));
        var parts=multiPart==null?null:multiPart.bake(baker,state,debugName);
        return new NativeMachineBaseModel<>(baked,parts,definition.defaultRenderState(),
                new SingleVariant(baker.missingBlockModelPart()),stateLookup);
    }
}
