package com.gregtechceu.gtceu.client.renderer.cover;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.Identifier;
import java.util.function.Function;
import java.util.function.Predicate;

/** Reloadable immutable base/glow palette; implicit glow is cleared when absent in a new atlas. */
public final class NativeCoverTextures {
    public record Palette(TextureAtlasSprite base,TextureAtlasSprite emissive) {}
    private final Identifier base;
    private final Identifier explicitGlow;
    private volatile Palette palette=new Palette(null,null);
    public NativeCoverTextures(Identifier base,Identifier explicitGlow){this.base=base;this.explicitGlow=explicitGlow;}
    public void reload(Function<Identifier,TextureAtlasSprite> sprites,Predicate<Identifier> present) {
        var implicit=base.withSuffix("_emissive");
        var glow=explicitGlow!=null?sprites.apply(explicitGlow):present.test(implicit)?sprites.apply(implicit):null;
        palette=new Palette(sprites.apply(base),glow);
    }
    public Palette palette(){return palette;}
}
