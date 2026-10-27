package com.gregtechceu.gtceu.client.model.machine;

import com.gregtechceu.gtceu.client.model.pipe.NativeCoverSnapshot;
import com.gregtechceu.gtceu.client.util.StaticFaceBakery;
import com.lowdragmc.lowdraglib.client.bakedpipeline.FaceQuad;
import net.minecraft.client.renderer.block.dispatch.BlockModelRotation;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import java.util.Optional;

/** Immutable output directions/settings captured before chunk meshing. */
public record NativeMachineOutput(Direction itemFace, boolean autoItems, Direction fluidFace, boolean autoFluids) {
    public Optional<NativeCoverSnapshot> geometry(TextureAtlasSprite pipe, TextureAtlasSprite item, TextureAtlasSprite fluid) {
        return NativeCoverSnapshot.capture((side, quads) -> {
            if (side == null) return;
            if (side == itemFace) {
                quads.add(StaticFaceBakery.bakeFace(StaticFaceBakery.OUTPUT_OVERLAY, side, pipe));
                if (autoItems) quads.add(FaceQuad.bakeFace(StaticFaceBakery.AUTO_OUTPUT_OVERLAY, side, item,
                        BlockModelRotation.IDENTITY, -101, 15, true, true));
            }
            if (side == fluidFace) {
                quads.add(StaticFaceBakery.bakeFace(StaticFaceBakery.OUTPUT_OVERLAY, side, pipe));
                if (autoFluids) quads.add(FaceQuad.bakeFace(StaticFaceBakery.AUTO_OUTPUT_OVERLAY, side, fluid,
                        BlockModelRotation.IDENTITY, -101, 15, true, true));
            }
        });
    }
}
