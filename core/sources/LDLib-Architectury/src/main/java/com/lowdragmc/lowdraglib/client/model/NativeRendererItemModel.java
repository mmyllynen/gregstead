package com.lowdragmc.lowdraglib.client.model;

import com.lowdragmc.lowdraglib.client.renderer.NativeBlockRenderer;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.*;
import net.minecraft.client.resources.model.geometry.QuadCollection;
import net.minecraft.client.resources.model.cuboid.ItemTransforms;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.model.data.ModelData;
import org.joml.Matrix4f;
import java.util.*;

/** Native counterpart of the old renderer-backed ItemModelShaper geometry. */
public final class NativeRendererItemModel implements ItemModel {
    private static final Map<NativeBlockRenderer, NativeRendererItemModel> MODELS = new HashMap<>();
    private final NativeBlockRenderer renderer;
    private java.util.List<net.minecraft.client.resources.model.geometry.BakedQuad> previousGeometry;
    private Material.Baked previousParticle;
    private boolean previousLight;
    private CuboidItemModelWrapper delegate;
    private NativeRendererItemModel(NativeBlockRenderer renderer) { this.renderer = renderer; }
    public static synchronized NativeRendererItemModel of(NativeBlockRenderer renderer) {
        return MODELS.computeIfAbsent(renderer, NativeRendererItemModel::new);
    }
    public void update(ItemStackRenderState output, ItemStack stack, ItemModelResolver resolver, ItemDisplayContext context,
            ClientLevel level, ItemOwner owner, int seed) {
        var builder = new QuadCollection.Builder();
        for (var quad : renderer.renderModel(null,null,null,null,RandomSource.create(seed),ModelData.EMPTY,null)) builder.addUnculledFace(quad);
        for (Direction side : Direction.values()) {
            for (var quad : renderer.renderModel(null,null,null,side,RandomSource.create(seed),ModelData.EMPTY,null)) builder.addCulledFace(side,quad);
        }
        var geometry = builder.build();
        if (geometry.getAll().isEmpty()) {
            // Preserve existing unsupported-renderer fallback until native special submission is available.
            boolean previous = com.lowdragmc.lowdraglib.client.renderer.NativeItemModelProvider.DISABLED.get();
            com.lowdragmc.lowdraglib.client.renderer.NativeItemModelProvider.DISABLED.set(true);
            try { resolver.appendItemLayers(output,stack,context,level,owner,seed); }
            finally { com.lowdragmc.lowdraglib.client.renderer.NativeItemModelProvider.DISABLED.set(previous); }
            return;
        }
        var particle = new Material.Baked(renderer.getParticleTexture(null,null,ModelData.EMPTY),false);
        boolean light = renderer.useBlockLight(stack);
        if (delegate == null || !geometry.getAll().equals(previousGeometry) || !particle.equals(previousParticle) || light != previousLight) {
            previousGeometry = geometry.getAll(); previousParticle = particle; previousLight = light;
            delegate = new CuboidItemModelWrapper(List.of(),geometry,
                    new ModelRenderProperties(light,particle,ItemTransforms.NO_TRANSFORMS),new Matrix4f());
        }
        delegate.update(output,stack,resolver,context,level,owner,seed);
    }
}
