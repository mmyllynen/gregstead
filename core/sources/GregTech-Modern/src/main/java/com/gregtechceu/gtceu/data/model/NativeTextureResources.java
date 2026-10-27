package com.gregtechceu.gtceu.data.model;

import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import java.util.Objects;
import java.util.function.Predicate;

/** Texture sprite IDs resolve against the active resource packs, including pack overrides. */
public final class NativeTextureResources implements Predicate<Identifier> {
    private final ResourceManager resources;
    public NativeTextureResources(ResourceManager resources) { this.resources = Objects.requireNonNull(resources); }
    public static Identifier file(Identifier sprite) {
        return sprite.withPrefix("textures/").withSuffix(".png");
    }
    @Override public boolean test(Identifier sprite) { return resources.getResource(file(sprite)).isPresent(); }
}
