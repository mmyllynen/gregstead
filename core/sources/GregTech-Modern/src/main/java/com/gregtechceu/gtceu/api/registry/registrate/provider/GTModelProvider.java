package com.gregtechceu.gtceu.api.registry.registrate.provider;

import com.tterrag.registrate.AbstractRegistrate;
import com.tterrag.registrate.providers.generators.RegistrateModelProvider;
import com.tterrag.registrate.providers.generators.RegistrateItemModelGenerator;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.data.PackOutput;

public class GTModelProvider extends RegistrateModelProvider {
    private final AbstractRegistrate<?> parent;
    private final net.minecraft.server.packs.resources.ResourceManager resources;
    public GTModelProvider(AbstractRegistrate<?> parent, PackOutput output,
            net.minecraft.server.packs.resources.ResourceManager resources) {
        super(parent, output); this.parent=parent; this.resources=resources;
    }
    @Override protected void registerModels(BlockModelGenerators blocks, ItemModelGenerators items) {
        var provider = new GTBlockstateProvider(parent, blocks.blockStateOutput, blocks.itemModelOutput, blocks.modelOutput);
        provider.setTextureResources(new com.gregtechceu.gtceu.data.model.NativeTextureResources(resources));
        provider.run();
        new RegistrateItemModelGenerator(parent, items.itemModelOutput, items.modelOutput).run();
    }
}
