package com.gregtechceu.gtceu.client.model.pipe;

import com.gregtechceu.gtceu.client.util.GTQuadTransformers;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.Direction;
import java.util.*;
import java.util.function.BiConsumer;

/** Prepared directional cover quads; contains no mutable cover behavior or block entity references. */
public final class NativeCoverSnapshot implements BlockStateModelPart {
    private final Map<Direction,List<BakedQuad>> faces;
    private final Material.Baked particle;
    private final int flags;
    private NativeCoverSnapshot(Map<Direction,List<BakedQuad>> faces,BakedQuad first,int flags) {
        this.faces=Collections.unmodifiableMap(faces);particle=new Material.Baked(first.materialInfo().sprite(),false);this.flags=flags;
    }
    public static Optional<NativeCoverSnapshot> capture(BiConsumer<Direction,List<BakedQuad>> renderer) {
        var faces=new IdentityHashMap<Direction,List<BakedQuad>>();BakedQuad first=null;int flags=0;
        var directions=new ArrayList<Direction>();directions.add(null);directions.addAll(List.of(Direction.values()));
        for(var side:directions) {
            var quads=new ArrayList<BakedQuad>();renderer.accept(side,quads);
            var copy=new ArrayList<BakedQuad>();
            for(var quad:quads){var frozen=GTQuadTransformers.copy(quad);copy.add(frozen);flags|=frozen.materialInfo().flags();if(first==null)first=frozen;}
            faces.put(side,List.copyOf(copy));
        }
        return first==null?Optional.empty():Optional.of(new NativeCoverSnapshot(faces,first,flags));
    }
    @Override public List<BakedQuad> getQuads(Direction side){return faces.getOrDefault(side,List.of());}
    @Override public boolean useAmbientOcclusion(){return true;}
    @Override public Material.Baked particleMaterial(){return particle;}
    @Override public int materialFlags(){return flags;}
}
