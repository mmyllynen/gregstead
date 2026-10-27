package com.lowdragmc.lowdraglib.core.mixins.accessor;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** Remaining native quick-craft state. Carried item rendering delegates to the native screen. */
@Mixin(AbstractContainerScreen.class)
public interface AbstractContainerScreenAccessor {
    @Accessor int getQuickCraftingRemainder();
}
