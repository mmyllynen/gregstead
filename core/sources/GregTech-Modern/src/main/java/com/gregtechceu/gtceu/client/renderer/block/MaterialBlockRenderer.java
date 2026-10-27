package com.gregtechceu.gtceu.client.renderer.block;

import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconSet;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconType;
import com.gregtechceu.gtceu.data.pack.GTDynamicResourcePack;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.MultiVariant;
import net.minecraft.client.renderer.block.dispatch.Variant;
import net.minecraft.util.random.WeightedList;
import net.minecraft.client.data.models.model.*;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;

import java.util.HashSet;
import java.util.Set;

public class MaterialBlockRenderer {

    private static final Set<MaterialBlockRenderer> MODELS = new HashSet<>();

    public static void create(Block block, MaterialIconType type, MaterialIconSet iconSet, Identifier layer) {
        MODELS.add(new MaterialBlockRenderer(block, type, iconSet, layer));
    }

    public static void reinitModels() {
        for (MaterialBlockRenderer model : MODELS) {
            Identifier blockId = BuiltInRegistries.BLOCK.getKey(model.block);
            Identifier parent = model.type.getBlockModelPath(model.iconSet, true);
            Identifier modelId = blockId.withPrefix("block/");
            GTDynamicResourcePack.addBlockModel(modelId, com.gregtechceu.gtceu.client.model.NativeMaterialModelDefinitions.parent(parent,model.layer,MaterialBlockRenderer::readModel));

            GTDynamicResourcePack.addBlockState(blockId, BlockModelGenerators.createSimpleBlock(model.block,
                    new MultiVariant(WeightedList.of(new Variant(modelId)))));
            GTDynamicResourcePack.addItemModel(BuiltInRegistries.ITEM.getKey(model.block.asItem()),
                    new DelegatedModel(modelId));
        }
    }

    private static com.google.gson.JsonObject readModel(Identifier id) {
        try(var reader=net.minecraft.client.Minecraft.getInstance().getResourceManager()
                .getResourceOrThrow(id.withPath(path -> "models/"+path+".json")).openAsReader()) {
            return com.google.gson.JsonParser.parseReader(reader).getAsJsonObject();
        } catch(java.io.IOException error) { throw new IllegalStateException("Cannot read material model "+id,error); }
    }

    private final Block block;
    private final MaterialIconType type;
    private final MaterialIconSet iconSet;
    private final Identifier layer;

    protected MaterialBlockRenderer(Block block, MaterialIconType type, MaterialIconSet iconSet, Identifier layer) {
        this.block = block;
        this.type = type;
        this.iconSet = iconSet;
        this.layer = layer;
    }
}
