package com.lowdragmc.lowdraglib.client.renderer;

import net.minecraft.client.renderer.block.dispatch.BlockModelRotation;
import net.minecraft.client.renderer.block.dispatch.ModelState;

final class ClientBlockRendererState {
    private ClientBlockRendererState() {}
    static ModelState identity() { return BlockModelRotation.IDENTITY; }
}
