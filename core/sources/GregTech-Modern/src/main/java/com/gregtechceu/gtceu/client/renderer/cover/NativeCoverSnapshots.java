package com.gregtechceu.gtceu.client.renderer.cover;

import com.gregtechceu.gtceu.api.capability.ICoverable;
import com.gregtechceu.gtceu.client.model.pipe.NativeCoverSnapshot;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.model.data.ModelData;
import java.util.List;

/** Captures the existing complete cover/plate renderer before chunk meshing accesses its snapshot. */
public final class NativeCoverSnapshots {
    private NativeCoverSnapshots() {}
    private static final ICoverableRenderer RENDERER=new ICoverableRenderer() {};
    public static List<NativeCoverSnapshot> capture(ICoverable covers,Level level,BlockPos pos,ModelData data) {
        if(!(level instanceof BlockAndTintGetter view))throw new IllegalArgumentException("Cover capture requires client world");
        long seed=level.getBlockState(pos).getSeed(pos);
        return NativeCoverSnapshot.capture((side,quads)->RENDERER.renderCovers(quads,covers,pos,view,side,
                RandomSource.create(seed),data,null)).map(List::of).orElseGet(List::of);
    }
}
