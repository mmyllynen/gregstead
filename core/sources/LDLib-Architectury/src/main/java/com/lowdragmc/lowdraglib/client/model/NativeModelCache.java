package com.lowdragmc.lowdraglib.client.model;

import net.minecraft.client.renderer.block.dispatch.ModelState;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.client.resources.model.cuboid.ItemTransforms;
import net.minecraft.client.resources.model.geometry.QuadCollection;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.resources.Identifier;
import java.util.*;

/** Real baked geometry caches bounded to the current resource-reload model bakery. */
public final class NativeModelCache {
    public record Item(QuadCollection geometry,ItemTransforms transforms,UnbakedModel.GuiLight guiLight,
            Material.Baked particle,boolean ambientOcclusion,boolean gui3d) {}
    private final Identifier location;
    private ModelBakery generation;
    private final Map<ModelState,BlockStateModelPart> blocks=new HashMap<>();
    private Item item;
    public NativeModelCache(Identifier location){this.location=location;}
    private void refresh() {
        var current=ModelFactory.getModelBakery();
        if(current!=generation){generation=current;blocks.clear();item=null;}
    }
    public synchronized BlockStateModelPart block(ModelState state) {
        refresh();return blocks.computeIfAbsent(state,value->ModelFactory.bake(location,value));
    }
    public synchronized Item item() {
        refresh();
        if(item==null) {
            var model=ModelFactory.getResolvedModel(location);var baker=ModelFactory.getModelBaker();
            var slots=model.getTopTextureSlots();
            item=new Item(ModelFactory.bakeGeometry(location,net.minecraft.client.renderer.block.dispatch.BlockModelRotation.IDENTITY),
                    model.getTopTransforms(),model.getTopGuiLight(),model.resolveParticleMaterial(slots,baker),model.getTopAmbientOcclusion(),gui3d(model));
        }
        return item;
    }
    public static boolean gui3d(net.minecraft.client.resources.model.ResolvedModel model) {
        while (model != null) {
            if (model.wrapped().geometry() != null) {
                return !(model.wrapped() instanceof net.minecraft.client.resources.model.cuboid.ItemModelGenerator);
            }
            model = model.parent();
        }
        return true;
    }
    public synchronized void clear(){generation=null;blocks.clear();item=null;}
}
