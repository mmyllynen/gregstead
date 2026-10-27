package com.lowdragmc.lowdraglib.editor.data;

import com.lowdragmc.lowdraglib.editor.data.resource.Resource;
import com.lowdragmc.lowdraglib.editor.data.resource.TexturesResource;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.texture.UIResourceTexture;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import java.util.function.Supplier;

/** Production UI loading keeps named textures while leaving editor resource scope intact. */
public final class NativeUIProjectLoader {
    private NativeUIProjectLoader() {}
    public static Supplier<WidgetGroup> creator(HolderLookup.Provider provider, CompoundTag tag) {
        var resources = Resources.fromNBT(tag.getCompoundOrEmpty("resources"), provider);
        var data = tag.getCompoundOrEmpty("root").copy();
        return () -> {
            var root = new WidgetGroup();
            load(root, data, resources, provider);
            return root;
        };
    }
    public static void load(WidgetGroup root, CompoundTag tag, HolderLookup.Provider provider) {
        load(root, tag.getCompoundOrEmpty("root"), Resources.fromNBT(tag.getCompoundOrEmpty("resources"), provider), provider);
    }
    @SuppressWarnings("unchecked")
    public static void load(WidgetGroup root, CompoundTag data, Resources resources, HolderLookup.Provider provider) {
        var previous = UIResourceTexture.RESOURCE.get();
        var previousResolve = UIResourceTexture.RESOLVE_PROJECT_TEXTURES.get();
        var textures = (Resource<IGuiTexture>) resources.resources.get(TexturesResource.RESOURCE_NAME);
        try {
            UIResourceTexture.RESOURCE.set(textures);
            UIResourceTexture.RESOLVE_PROJECT_TEXTURES.set(true);
            root.deserializeNBT(provider, data);
        } finally {
            if (previousResolve) UIResourceTexture.RESOLVE_PROJECT_TEXTURES.set(true);
            else UIResourceTexture.RESOLVE_PROJECT_TEXTURES.remove();
            if (previous == null) UIResourceTexture.RESOURCE.remove();
            else UIResourceTexture.RESOURCE.set(previous);
        }
    }
}
