package com.gregtechceu.gtceu.integration.jade;

import com.gregtechceu.gtceu.integration.jade.provider.GTJadeTooltipProvider;
import net.minecraft.resources.Identifier;
import snownee.jade.api.*;
import snownee.jade.api.config.IPluginConfig;

/** Separate native client provider forwarding the original UID, priority and tooltip implementation. */
public record ClientGTJadeComponent(GTJadeTooltipProvider delegate) implements IBlockComponentProvider {
    @Override public Identifier getUid() { return delegate.getUid(); }
    @Override public int getDefaultPriority() { return delegate.getDefaultPriority(); }
    @Override public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        delegate.appendTooltip(tooltip, accessor, config);
    }
}
