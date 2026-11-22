package com.gregtechceu.gtceu.core.mixins;

import com.gregtechceu.gtceu.core.interop.OreFeatureTargets;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;

import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.feature.BlockReplacement;
import net.minecraft.world.level.levelgen.feature.OreFeature;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;

@Mixin(OreFeature.class)
abstract class OreFeatureMixin {

    @ModifyExpressionValue(method = "doPlace", at = @At(value = "FIELD", opcode = 180,
            target = "Lnet/minecraft/world/level/levelgen/feature/OreFeature;targetStates:Ljava/util/List;"))
    private List<BlockReplacement> gtceu(List<BlockReplacement> targets, @Local(argsOnly = true) WorldGenLevel level) {
        return ((OreFeatureTargets) this).gtceu$targetsFor(level, targets);
    }
}
