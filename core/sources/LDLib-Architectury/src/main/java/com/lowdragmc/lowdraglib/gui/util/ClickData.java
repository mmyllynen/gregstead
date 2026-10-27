package com.lowdragmc.lowdraglib.gui.util;

import com.lowdragmc.lowdraglib.core.mixins.accessor.MouseHandlerAccessor;
import com.mojang.blaze3d.platform.InputConstants;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.network.FriendlyByteBuf;

public class ClickData {
    public final int button;
    public final boolean isShiftClick;
    public final boolean isCtrlClick;
    public final boolean isRemote;

    private ClickData(int button, boolean isShiftClick, boolean isCtrlClick, boolean isRemote) {
        this.button = button;
        this.isShiftClick = isShiftClick;
        this.isCtrlClick = isCtrlClick;
        this.isRemote = isRemote;
    }

    @OnlyIn(Dist.CLIENT)
    public ClickData() {
        MouseHandler mouseHelper = Minecraft.getInstance().mouseHandler;

        var active = ((MouseHandlerAccessor)mouseHelper).getActiveButton();
        this.button = active == null ? -1 : com.lowdragmc.lowdraglib.transfer.input.GuiInput.widgetButton(active.button());
        this.isShiftClick = active != null ? active.hasShiftDown() : InputConstants.isKeyDown(InputConstants.KEY_LSHIFT) || InputConstants.isKeyDown(InputConstants.KEY_RSHIFT);
        this.isCtrlClick = active != null ? active.hasControlDown() : InputConstants.isKeyDown(InputConstants.KEY_LCONTROL) || InputConstants.isKeyDown(InputConstants.KEY_RCONTROL);
        this.isRemote = true;
    }

    @OnlyIn(Dist.CLIENT)
    public void writeToBuf(FriendlyByteBuf buf) {
        buf.writeVarInt(button);
        buf.writeBoolean(isShiftClick);
        buf.writeBoolean(isCtrlClick);
    }

    public static ClickData readFromBuf(FriendlyByteBuf buf) {
        int button = buf.readVarInt();
        boolean shiftClick = buf.readBoolean();
        boolean ctrlClick = buf.readBoolean();
        return new ClickData(button, shiftClick, ctrlClick, false);
    }
}
