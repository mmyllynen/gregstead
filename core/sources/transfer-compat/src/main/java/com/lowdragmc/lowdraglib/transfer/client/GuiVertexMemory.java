package com.lowdragmc.lowdraglib.transfer.client;

import com.mojang.renderpearl.api.vertex.VertexFormat;
import com.mojang.renderpearl.api.vertex.VertexFormatElement;
import org.lwjgl.system.MemoryUtil;

/** Raw custom attributes for the native BufferBuilder, with exact layout/size validation. */
public final class GuiVertexMemory {
    private GuiVertexMemory() {}
    public static void write(long pointer, VertexFormat format, VertexFormatElement element, int[] data) {
        if (pointer <= 0) throw new IllegalStateException("No active vertex");
        if (!element.equals(format.getElement(element.name()))) throw new IllegalArgumentException("Attribute not in vertex format");
        if ((long) data.length * Integer.BYTES != element.format().blockSize()
                || element.offset() < 0 || element.offset() + element.format().blockSize() > format.getVertexSize()) {
            throw new IllegalArgumentException("Invalid raw attribute size/offset");
        }
        for (int i = 0; i < data.length; i++) MemoryUtil.memPutInt(pointer + element.offset() + (long) i * Integer.BYTES, data[i]);
    }
}
