package com.gregtechceu.gtceu.api.gui.widget;

import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.fancy.IFancyTooltip;
import com.gregtechceu.gtceu.api.gui.fancy.TooltipsPanel;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.function.BooleanSupplier;

/** Uses the standard machine error panel, with state supplied only by the server. */
public class SteamVentWarningWidget extends TooltipsPanel {

    private final BooleanSupplier blockedSupplier;
    private boolean blocked;

    public SteamVentWarningWidget(BooleanSupplier blockedSupplier) {
        this.blockedSupplier = blockedSupplier;
        setSelfPosition(178, 2);
        attachTooltips(new IFancyTooltip.Basic(() -> GuiTextures.INSUFFICIENT_INPUT,
                () -> List.of(blockedReason(), Component.translatable("gtceu.machine.steam.exhaust_blocked_help")),
                () -> blocked, () -> null));
    }

    public static Component blockedReason() {
        return Component.translatable("gtceu.machine.steam.exhaust_blocked");
    }

    @Override
    public void writeInitialData(RegistryFriendlyByteBuf buffer) {
        super.writeInitialData(buffer);
        blocked = blockedSupplier.getAsBoolean();
        buffer.writeBoolean(blocked);
    }

    @Override
    public void readInitialData(RegistryFriendlyByteBuf buffer) {
        super.readInitialData(buffer);
        blocked = buffer.readBoolean();
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        boolean current = blockedSupplier.getAsBoolean();
        if (blocked != current) {
            blocked = current;
            writeUpdateInfo(1, buffer -> buffer.writeBoolean(current));
        }
    }

    @Override
    public void readUpdateInfo(int id, RegistryFriendlyByteBuf buffer) {
        if (id == 1) {
            blocked = buffer.readBoolean();
        } else {
            super.readUpdateInfo(id, buffer);
        }
    }
}
