package com.gregtechceu.gtceu.client.renderer;

import com.lowdragmc.lowdraglib.client.scene.NativeSceneVertexConsumer;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.core.BlockPos;

/** Scale only block geometry about its own absolute center; fluids use their normal native vertices. */
public final class NativePreviewVertices extends NativeSceneVertexConsumer {
    private final float x,y,z;
    public NativePreviewVertices(VertexConsumer output,BlockPos pos) {
        super(output);x=pos.getX()+0.5f;y=pos.getY()+0.5f;z=pos.getZ()+0.5f;
    }
    @Override public VertexConsumer addVertex(float x,float y,float z) {
        builder.addVertex(this.x+(x-this.x)*0.8f,this.y+(y-this.y)*0.8f,this.z+(z-this.z)*0.8f);
        return this;
    }
}
