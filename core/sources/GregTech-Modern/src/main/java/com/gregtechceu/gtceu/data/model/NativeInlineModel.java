package com.gregtechceu.gtceu.data.model;

import com.google.gson.JsonElement;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.model.generators.template.ExtendedModelTemplateBuilder;

/** Native template and texture mapping serialized inline for a machine model variant. */
public final class NativeInlineModel {
    private final ExtendedModelTemplateBuilder template = ExtendedModelTemplateBuilder.builder();
    private final TextureMapping textures = new TextureMapping();
    public NativeInlineModel parent(Identifier parent) {
        template.parent(parent);
        return this;
    }
    public NativeInlineModel texture(String name, Identifier texture) {
        return texture(name,texture,false);
    }
    public NativeInlineModel texture(String name, Identifier texture, boolean forceTranslucent) {
        var slot = TextureSlot.create(name);
        template.requiredTextureSlot(slot);
        textures.put(slot, new Material(texture,forceTranslucent));
        return this;
    }
    public Identifier emit(Identifier id, java.util.function.BiConsumer<Identifier,
            net.minecraft.client.data.models.model.ModelInstance> output) {
        return template.build().create(id, textures, output);
    }
    public JsonElement toJson() {
        JsonElement[] json = new JsonElement[1];
        template.build().create(Identifier.parse("gtceu:inline_machine"), textures,
                (id, instance) -> json[0] = instance.get());
        return json[0];
    }
}
