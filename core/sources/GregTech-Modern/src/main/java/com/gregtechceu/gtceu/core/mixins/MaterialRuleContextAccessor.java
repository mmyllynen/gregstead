package com.gregtechceu.gtceu.core.mixins;

import net.minecraft.world.level.levelgen.WorldGenerationContext;
import net.minecraft.world.level.levelgen.material.MaterialRuleContext;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(MaterialRuleContext.class)
public interface MaterialRuleContextAccessor {

    @Accessor("context")
    WorldGenerationContext gtceu$worldContext();
}
