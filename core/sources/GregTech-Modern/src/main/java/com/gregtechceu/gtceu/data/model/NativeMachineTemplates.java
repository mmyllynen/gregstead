package com.gregtechceu.gtceu.data.model;

import com.gregtechceu.gtceu.api.registry.registrate.provider.GTBlockstateProvider;
import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;

/** Common native templates retaining original casing and steam-vent model data. */
public final class NativeMachineTemplates {
    private NativeMachineTemplates() {}
    public static Identifier casing(GTBlockstateProvider provider, Identifier id, Identifier parent, Identifier texture) {
        var builder = provider.getBuilder().parent(parent);
        var directory = texture.getPath().endsWith("/") ? texture : texture.withSuffix("/");
        for (String name : new String[]{"bottom", "top", "side"})
            builder.texture(TextureSlot.create(name), new Material(directory.withSuffix(name)));
        return builder.build(id);
    }
    public static Identifier steamVent(GTBlockstateProvider provider, Identifier id) {
        var slot = TextureSlot.create("steam_vent");
        return provider.getBuilder().texture(TextureSlot.PARTICLE, new Material(id)).texture(slot, new Material(id))
                .transformTemplate(template -> template.element(element -> element.from(0,0,-0.002f).to(16,16,-0.002f)
                        .face(Direction.NORTH, face -> face.uvs(0,0,16,16).texture(slot).cullface(Direction.NORTH))))
                .build(id);
    }
}
