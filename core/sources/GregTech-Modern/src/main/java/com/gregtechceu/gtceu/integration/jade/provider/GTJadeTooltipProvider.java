package com.gregtechceu.gtceu.integration.jade.provider;

import snownee.jade.api.*;
import snownee.jade.api.config.IPluginConfig;

/** Common tooltip contract; server data providers are not Jade client components. */
public interface GTJadeTooltipProvider extends IJadeProvider {
    void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config);
}
