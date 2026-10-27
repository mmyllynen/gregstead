package com.gregtechceu.gtceu.client.model.machine;
import net.neoforged.neoforge.model.data.ModelProperty;
public final class MachineModelProperties {
    private MachineModelProperties() {}
    public static final ModelProperty<java.util.List<com.gregtechceu.gtceu.client.model.pipe.NativeCoverSnapshot>> CONTROLLER_GEOMETRY = new ModelProperty<>();
    public static final ModelProperty<java.util.Map<String,net.minecraft.client.renderer.texture.TextureAtlasSprite>> FORMED_TEXTURES = new ModelProperty<>();
    public static final ModelProperty<java.util.List<com.gregtechceu.gtceu.client.model.pipe.NativeCoverSnapshot>> COVERS = new ModelProperty<>();
    public static final ModelProperty<NativeMachineOutput> OUTPUT = new ModelProperty<>();
    public static final ModelProperty<MachineRenderState> RENDER_STATE = new ModelProperty<>();
}
