package com.gregtechceu.gtceu.client.util;

import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.Identifier;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/** Atlas-only lifecycle, independent of removed model-baking interfaces. */
public final class NativeAtlasListeners {
    private record Binding(boolean transientListener,Identifier atlas,Consumer<TextureAtlas> listener) {}
    private static final CopyOnWriteArrayList<Binding> BINDINGS=new CopyOnWriteArrayList<>();
    private NativeAtlasListeners() {}
    public static AutoCloseable register(boolean transientListener,Identifier atlas,Consumer<TextureAtlas> listener) {
        var binding=new Binding(transientListener,atlas,listener);BINDINGS.add(binding);return ()->BINDINGS.remove(binding);
    }
    public static void dispatch(TextureAtlas atlas) {
        for(var binding:BINDINGS)if(binding.atlas()==null || binding.atlas().equals(atlas.location()))binding.listener().accept(atlas);
    }
    public static void beginReload(){BINDINGS.removeIf(Binding::transientListener);}
}
