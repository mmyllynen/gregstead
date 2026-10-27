package com.gregtechceu.gtceu.client.renderer.entity;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.common.data.GTEntityTypes;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.object.boat.BoatModel;
import net.minecraft.client.renderer.entity.BoatRenderer;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

/** Native boat layers keep the original GregTech boat and chest-boat texture paths. */
public final class NativeBoatModels {
    private static final ModelLayerLocation RUBBER = layer("boat/rubber");
    private static final ModelLayerLocation TREATED = layer("boat/treated_wood");
    private static final ModelLayerLocation RUBBER_CHEST = layer("chest_boat/rubber");
    private static final ModelLayerLocation TREATED_CHEST = layer("chest_boat/treated_wood");
    private NativeBoatModels() {}
    private static ModelLayerLocation layer(String path) { return new ModelLayerLocation(GTCEu.id(path), "main"); }
    public static void layers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(RUBBER, BoatModel::createBoatModel);
        event.registerLayerDefinition(TREATED, BoatModel::createBoatModel);
        event.registerLayerDefinition(RUBBER_CHEST, BoatModel::createChestBoatModel);
        event.registerLayerDefinition(TREATED_CHEST, BoatModel::createChestBoatModel);
    }
    public static void renderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(GTEntityTypes.RUBBER_BOAT.get(), context -> new BoatRenderer(context, RUBBER));
        event.registerEntityRenderer(GTEntityTypes.TREATED_WOOD_BOAT.get(), context -> new BoatRenderer(context, TREATED));
        event.registerEntityRenderer(GTEntityTypes.RUBBER_CHEST_BOAT.get(), context -> new BoatRenderer(context, RUBBER_CHEST));
        event.registerEntityRenderer(GTEntityTypes.TREATED_WOOD_CHEST_BOAT.get(), context -> new BoatRenderer(context, TREATED_CHEST));
    }
}
