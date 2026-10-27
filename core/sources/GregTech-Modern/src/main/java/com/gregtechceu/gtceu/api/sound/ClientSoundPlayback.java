package com.gregtechceu.gtceu.api.sound;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import java.util.function.BooleanSupplier;

final class ClientSoundPlayback {
    private ClientSoundPlayback() {}
    static AutoReleasedSound play(SoundEntry entry, BooleanSupplier predicate, BlockPos pos,
                                  boolean loop, int delay, float volume, float pitch) {
        var sound = new AutoReleasedSound(entry, predicate, pos, loop, delay, volume, pitch);
        Minecraft.getInstance().getSoundManager().play(sound);
        return sound;
    }
}
