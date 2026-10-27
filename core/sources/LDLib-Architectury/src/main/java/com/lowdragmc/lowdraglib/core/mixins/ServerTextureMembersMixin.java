package com.lowdragmc.lowdraglib.core.mixins;

import org.spongepowered.asm.mixin.Mixin;

/** Gives the plugin a side-aware hook for client-only methods on common texture definitions. */
@Mixin(targets={
        "com.lowdragmc.lowdraglib.gui.texture.TransformTexture",
        "com.lowdragmc.lowdraglib.gui.texture.ResourceTexture"
})
public abstract class ServerTextureMembersMixin {}
