package com.gregtechceu.gtceu.core.mixins;

import com.gregtechceu.gtceu.core.interop.OreFeatureTargets;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;

import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.feature.BlockReplacement;
import net.minecraft.world.level.levelgen.feature.ScatteredOreFeature;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;

@Mixin(ScatteredOreFeature.class)
abstract class ScatteredOreFeatureMixin {

    @ModifyExpressionValue(method = "place", at = @At(value = "FIELD", opcode = 180,
            target = "Lnet/minecraft/world/level/levelgen/feature/ScatteredOreFeature;targetStates:Ljava/util/List;"))
    private List<BlockReplacement> gtceu(List<BlockReplacement> targets, @Local(argsOnly = true) WorldGenLevel level) {
        return ((OreFeatureTargets) this).gtceu$targetsFor(level, targets);
    }
}
