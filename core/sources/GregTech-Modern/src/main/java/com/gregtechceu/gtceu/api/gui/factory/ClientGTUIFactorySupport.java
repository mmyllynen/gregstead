package com.gregtechceu.gtceu.api.gui.factory;

import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.cover.CoverBehavior;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.gui.editor.GTUIEditor;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Direction;
import net.minecraft.network.RegistryFriendlyByteBuf;

final class ClientGTUIFactorySupport {
    private ClientGTUIFactorySupport() {}
    static MetaMachine machine(RegistryFriendlyByteBuf data) {
        var level = Minecraft.getInstance().level;
        if (level == null) return null;
        return level.getBlockEntity(data.readBlockPos()) instanceof MetaMachine machine ? machine : null;
    }
    static CoverBehavior cover(RegistryFriendlyByteBuf data) {
        var level = Minecraft.getInstance().level;
        if (level == null) return null;
        var pos = data.readBlockPos(); var side = data.readEnum(Direction.class);
        var coverable = GTCapabilityHelper.getCoverable(level, pos, side);
        return coverable == null ? null : coverable.getCoverAtSide(side);
    }
    static WidgetGroup editor() { return new GTUIEditor(); }
}
