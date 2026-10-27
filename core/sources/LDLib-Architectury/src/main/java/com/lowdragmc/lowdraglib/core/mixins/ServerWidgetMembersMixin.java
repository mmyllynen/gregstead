package com.lowdragmc.lowdraglib.core.mixins;

import org.spongepowered.asm.mixin.Mixin;

/** Server-visible widget definitions retain data and actions, excluding annotated client members. */
@Mixin(targets={
        "com.lowdragmc.lowdraglib.gui.widget.Widget",
        "com.lowdragmc.lowdraglib.gui.widget.WidgetGroup",
        "com.lowdragmc.lowdraglib.gui.widget.ImageWidget",
        "com.lowdragmc.lowdraglib.gui.widget.ProgressWidget",
        "com.lowdragmc.lowdraglib.gui.widget.ButtonWidget"
})
public abstract class ServerWidgetMembersMixin {}
