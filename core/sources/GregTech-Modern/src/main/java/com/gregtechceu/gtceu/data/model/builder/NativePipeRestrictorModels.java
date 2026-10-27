package com.gregtechceu.gtceu.data.model.builder;

import com.gregtechceu.gtceu.api.registry.registrate.provider.GTBlockstateProvider;
import com.gregtechceu.gtceu.utils.GTMath;
import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import java.util.*;

/** Native restrictor models, cached by datagen provider and original pipe thickness. */
public final class NativePipeRestrictorModels {
    private static final Map<GTBlockstateProvider, Map<Float, Identifier[]>> CACHE = new WeakHashMap<>();
    private NativePipeRestrictorModels() {}
    public static synchronized Identifier[] get(GTBlockstateProvider provider, float thickness) {
        if (!(thickness>0 && thickness<=16)) throw new IllegalArgumentException("Pipe thickness must be in (0,16]");
        return CACHE.computeIfAbsent(provider, ignored -> new HashMap<>())
                .computeIfAbsent(thickness, value -> build(provider,value)).clone();
    }
    public static synchronized void clear() { CACHE.clear(); }
    private static Identifier[] build(GTBlockstateProvider provider,float thickness) {
        var models=new Identifier[Direction.values().length];
        float min=(16-thickness)/2-0.003f, max=min+thickness+0.006f;
        var slot=TextureSlot.create("restrictor");
        for(var direction:Direction.values()) {
            var bounds=GTMath.getCoordinates(direction,min,max);
            var from=bounds.getFirst(); var to=bounds.getSecond();
            var id=Identifier.fromNamespaceAndPath("gtceu","block/pipe/restrictor/"+direction.getSerializedName()+"/thickness_"+thickness);
            models[direction.ordinal()]=id;
            if(provider.hasGeneratedModel(id)) continue;
            provider.getBuilder().texture(TextureSlot.PARTICLE,new Material(
                    Identifier.parse("gtceu:block/pipe/blocked/pipe_blocked"))).texture(slot,new Material(
                    Identifier.parse("gtceu:block/pipe/blocked/pipe_blocked")))
                    .transformTemplate(template -> template.element(element -> {
                        element.from(from.x,from.y,from.z).to(to.x,to.y,to.z);
                        for(var face:Direction.values()) if(face.getAxis()!=direction.getAxis())
                            element.face(face,builder -> builder.texture(slot));
                    })).build(id);
        }
        return models;
    }
}
