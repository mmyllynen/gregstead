package com.lowdragmc.lowdraglib.gui.widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvents;

final class ClientWidgetFeedback {
    private ClientWidgetFeedback() {}
    static void click() { Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F)); }
}
