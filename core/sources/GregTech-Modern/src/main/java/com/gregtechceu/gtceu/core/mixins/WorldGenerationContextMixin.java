package com.gregtechceu.gtceu.core.mixins;

import com.gregtechceu.gtceu.core.interop.VanillaOreRuleContext;
import com.gregtechceu.gtceu.api.data.worldgen.VanillaOreDimension;

import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.WorldGenerationContext;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldGenerationContext.class)
abstract class WorldGenerationContextMixin implements VanillaOreRuleContext {

    @Unique
    private boolean gtceu$keepVanillaOres;

    @Inject(method = "<init>(Lnet/minecraft/world/level/chunk/ChunkGenerator;Lnet/minecraft/world/level/LevelHeightAccessor;)V",
            at = @At("RETURN"))
    private void gtceu$identifyNoiseSettings(ChunkGenerator generator, LevelHeightAccessor heights, CallbackInfo ci) {
        gtceu$keepVanillaOres = generator instanceof NoiseBasedChunkGenerator noise
                && noise.generatorSettings().unwrapKey().filter(key -> key.identifier().equals(VanillaOreDimension.ID)).isPresent();
    }

    @Override
    public boolean gtceu$keepVanillaOres() {
        return gtceu$keepVanillaOres;
    }
}
