package com.lowdragmc.lowdraglib.client.model;

import com.google.common.collect.*;
import net.minecraft.client.resources.model.ModelDebugName;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.resources.Identifier;

/** Per-reload native model material queries, including cache hits and forced-translucency variants. */
public final class NativeMaterialUsage {
    private NativeMaterialUsage() {}
    public static final Multimap<Identifier,Material> MATERIALS=Multimaps.synchronizedMultimap(HashMultimap.create());
    public static void record(Material material,ModelDebugName model) {
        var id=Identifier.tryParse(model.debugName());
        if(id!=null)MATERIALS.put(id,material);
    }
    public static void clear(){MATERIALS.clear();}
}
