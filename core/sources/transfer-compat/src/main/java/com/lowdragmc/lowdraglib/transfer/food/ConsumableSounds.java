package com.lowdragmc.lowdraglib.transfer.food;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.component.Consumable;
/** Preserve the native consumption definition while replacing its legacy-selected sound. */
public final class ConsumableSounds {
    private ConsumableSounds() {}
    public static Consumable withSound(Consumable source,SoundEvent sound) {
        return new Consumable(source.consumeSeconds(),source.animation(),BuiltInRegistries.SOUND_EVENT.wrapAsHolder(sound),
                source.hasConsumeParticles(),source.onConsumeEffects());
    }
}
