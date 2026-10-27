package com.gregtechceu.gtceu.api.machine.feature;

import com.gregtechceu.gtceu.api.capability.IControllable;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.fancy.*;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.fancyconfigurator.CombinedDirectionalFancyConfigurator;
import com.gregtechceu.gtceu.api.machine.fancyconfigurator.MachineModeFancyConfigurator;

import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.texture.ItemStackTexture;
import com.lowdragmc.lowdraglib.gui.widget.ImageWidget;
import com.lowdragmc.lowdraglib.gui.widget.SceneWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.utils.data.BlockInfo;
import com.lowdragmc.lowdraglib.utils.virtuallevel.TrackedDummyWorld;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

final class ClientFancyMachinePreview {
    private ClientFancyMachinePreview() {}
    static void add(IFancyUIMachine machine, WidgetGroup group) {
            group.addWidget(new ImageWidget((100 - 48) / 2, 60, 48, 16, GuiTextures.SCENE));
            TrackedDummyWorld world = new TrackedDummyWorld();
            world.addBlock(BlockPos.ZERO, BlockInfo.fromBlockState(machine.self().getBlockState()));
            SceneWidget sceneWidget = new SceneWidget(0, 0, 100, 100, world) {

                @Override
                @OnlyIn(Dist.CLIENT)
                public void drawInBackground(@NotNull GuiGraphicsExtractor graphics, int mouseX, int mouseY,
                                             float partialTicks) {
                    // AUTO ROTATION
                    if (renderer != null) {
                        this.rotationPitch = (partialTicks + getGui().getTickCount()) * 2;
                        renderer.setCameraLookAt(this.center, 0.1f, Math.toRadians(this.rotationPitch),
                                Math.toRadians(this.rotationYaw));
                    }
                    super.drawInBackground(graphics, mouseX, mouseY, partialTicks);
                }
            };
            sceneWidget.useOrtho(true)
                    .setOrthoRange(0.5f)
                    .setScalable(false)
                    .setDraggable(false)
                    .setRenderFacing(false)
                    .setRenderSelect(false);
            sceneWidget.getRenderer().setFov(30);
            group.addWidget(sceneWidget);
            sceneWidget.setRenderedCore(List.of(BlockPos.ZERO), null);
    }
}
