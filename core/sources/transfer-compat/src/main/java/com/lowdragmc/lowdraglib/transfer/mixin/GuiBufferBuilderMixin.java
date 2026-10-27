package com.lowdragmc.lowdraglib.transfer.mixin;

import com.lowdragmc.lowdraglib.transfer.client.GuiVertexMemory;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.renderpearl.api.vertex.VertexFormat;
import com.mojang.renderpearl.api.vertex.VertexFormatElement;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(BufferBuilder.class)
public abstract class GuiBufferBuilderMixin implements VertexConsumer {
    @Shadow @Final private VertexFormat format;
    @Shadow private long vertexPointer;
    @Shadow private boolean building;
    @Override public VertexConsumer misc(VertexFormatElement element, int... data) {
        if (!building) throw new IllegalStateException("BufferBuilder is not building");
        GuiVertexMemory.write(vertexPointer, format, element, data);
        return (VertexConsumer) (Object) this;
    }
}
