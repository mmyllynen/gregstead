package com.gregtechceu.gtceu.core.mixins;

import net.minecraft.core.RegistryAccess;
import net.minecraft.server.ReloadableServerResources;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** Native reload registry access includes freshly loaded world registries. */
@Mixin(ReloadableServerResources.class)
public interface ReloadableServerResourcesAccessor {
    @Accessor("registryAccess")
    RegistryAccess gtceu$registryAccess();
}
