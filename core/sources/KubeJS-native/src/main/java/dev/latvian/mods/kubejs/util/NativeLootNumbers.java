package dev.latvian.mods.kubejs.util;

import net.minecraft.util.Mth;
import net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProvider;
import net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProviders;

/** Original quantity providers use integer sampling; float bounds retain the original floor conversion. */
public final class NativeLootNumbers {
    private NativeLootNumbers() {}
    public static ContextIntProvider exactly(float value) {
        return ContextIntProviders.exactly(Mth.floor(value)).value();
    }
    public static ContextIntProvider between(float min, float max) {
        return ContextIntProviders.between(Mth.floor(min), Mth.floor(max)).value();
    }
    public static ContextIntProvider binomial(int n, float probability) {
        return ContextIntProviders.binomial(n, probability).value();
    }
}
