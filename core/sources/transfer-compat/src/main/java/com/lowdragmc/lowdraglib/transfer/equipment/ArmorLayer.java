package com.lowdragmc.lowdraglib.transfer.equipment;

import net.minecraft.resources.Identifier;

/** Legacy texture/layer metadata retained while equipment assets are emitted for the current renderer. */
public record ArmorLayer(Identifier assetName, String suffix, boolean dyeable) {
    public ArmorLayer(Identifier assetName) { this(assetName, "", false); }
    public Identifier texture(boolean inner) {
        return Identifier.fromNamespaceAndPath(assetName.getNamespace(), "textures/models/armor/" +
                assetName.getPath() + "_layer_" + (inner ? 2 : 1) + suffix + ".png");
    }
    public Identifier innerTexture() { return texture(true); }
    public Identifier outerTexture() { return texture(false); }
}
