package com.gregtechceu.gtceu.common.block;
import com.gregtechceu.gtceu.core.mixins.BlockPropertiesAccessor;
import net.minecraft.world.level.block.state.BlockBehaviour;
/** Shared native implementation of the original GregTech shallow property copy. */
public final class NativeBlockPropertiesCopy {
    private NativeBlockPropertiesCopy() {}
    public static BlockBehaviour.Properties copy(BlockBehaviour.Properties props, BlockBehaviour.Properties newProps) {
        if (props == null) {
            return newProps;
        }
        newProps.destroyTime(((BlockPropertiesAccessor) props).getDestroyTime());
        newProps.explosionResistance(((BlockPropertiesAccessor) props).getExplosionResistance());
        if (!((BlockPropertiesAccessor) props).isHasCollision()) newProps.noCollision();
        if (((BlockPropertiesAccessor) props).isIsRandomlyTicking()) newProps.randomTicks();
        newProps.lightLevel(((BlockPropertiesAccessor) props).getLightEmission());
        newProps.mapColor(((BlockPropertiesAccessor) props).getMapColor());
        newProps.sound(((BlockPropertiesAccessor) props).getSoundType());
        newProps.friction(((BlockPropertiesAccessor) props).getFriction());
        newProps.speedFactor(((BlockPropertiesAccessor) props).getSpeedFactor());
        if (((BlockPropertiesAccessor) props).isDynamicShape()) newProps.dynamicShape();
        if (!((BlockPropertiesAccessor) props).isCanOcclude()) newProps.noOcclusion();
        if (((BlockPropertiesAccessor) props).isIsAir()) newProps.air();
        if (((BlockPropertiesAccessor) props).isIgnitedByLava()) newProps.ignitedByLava();
        if (((BlockPropertiesAccessor) props).isLiquid()) newProps.liquid();
        if (((BlockPropertiesAccessor) props).isForceSolidOff()) newProps.forceSolidOff();
        if (((BlockPropertiesAccessor) props).isForceSolidOn()) newProps.forceSolidOn();
        newProps.pushReaction(((BlockPropertiesAccessor) props).getPushReaction());
        if (((BlockPropertiesAccessor) props).isRequiresCorrectToolForDrops()) newProps.requiresCorrectToolForDrops();
        ((BlockPropertiesAccessor) newProps).setOffsetFunction(((BlockPropertiesAccessor) props).getOffsetFunction());
        if (!((BlockPropertiesAccessor) props).isSpawnTerrainParticles()) newProps.noTerrainParticles();
        ((BlockPropertiesAccessor) newProps)
                .setRequiredFeatures(((BlockPropertiesAccessor) props).getRequiredFeatures());
        newProps.emissiveRendering(((BlockPropertiesAccessor) props).getEmissiveRendering());
        newProps.instrument(((BlockPropertiesAccessor) props).getInstrument());
        if (((BlockPropertiesAccessor) props).isReplaceable()) newProps.replaceable();
        return newProps;
    }
}
