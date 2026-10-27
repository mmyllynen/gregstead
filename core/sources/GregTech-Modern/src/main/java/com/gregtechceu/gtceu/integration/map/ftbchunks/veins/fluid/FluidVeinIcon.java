package com.gregtechceu.gtceu.integration.map.ftbchunks.veins.fluid;

import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.gui.misc.ProspectorMode;
import com.gregtechceu.gtceu.integration.map.ftbchunks.FTBChunksOptions;
import com.gregtechceu.gtceu.integration.map.layer.builtin.FluidRenderLayer;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.Vec3;

import dev.ftb.mods.ftbchunks.FTBChunks;
import dev.ftb.mods.ftbchunks.api.FTBChunksAPI;
import dev.ftb.mods.ftbchunks.api.client.icon.MapIcon;
import dev.ftb.mods.ftbchunks.api.client.icon.MapType;
import dev.ftb.mods.ftbchunks.config.FTBChunksClientConfig;
import dev.ftb.mods.ftbchunks.client.map.MapDimension;
import dev.ftb.mods.ftbchunks.client.map.WaypointImpl;
import dev.ftb.mods.ftbchunks.client.map.WaypointType;
import dev.ftb.mods.ftblibrary.icon.Color4I;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.client.gui.widget.BaseScreen;
import dev.ftb.mods.ftblibrary.client.gui.input.Key;
import dev.ftb.mods.ftblibrary.client.gui.input.MouseButton;
import dev.ftb.mods.ftblibrary.util.TooltipList;
import lombok.Getter;
import lombok.Setter;

public class FluidVeinIcon implements MapIcon {

    public static double MAX_DISTANCE = FTBChunks.TILE_SIZE * FTBChunks.TILE_SIZE;
    @Getter
    private final ChunkPos chunkPos;
    @Getter
    private final ProspectorMode.FluidInfo fluidInfo;
    @Setter
    private int size;
    private Icon icon;

    public FluidVeinIcon(ChunkPos chunkPos, ProspectorMode.FluidInfo fluidInfo) {
        this.chunkPos = chunkPos;
        this.fluidInfo = fluidInfo;
        this.size = 1;
    }

    public boolean isEnabled() {
        return FTBChunksOptions.showLayer("bedrock_fluids") &&
                (fluidInfo.left() > 0 || !FTBChunksOptions.hideDepleted());
    }

    public String getName() {
        return FluidRenderLayer.getName(fluidInfo).getString();
    }

    public int getColor() {
        var color = com.lowdragmc.lowdraglib.transfer.client.FluidVisuals.color(fluidInfo.fluid());
        var material = ChemicalHelper.getMaterial(fluidInfo.fluid());
        if (!material.isNull()) {
            color = material.getMaterialARGB();
        }
        return color;
    }

    @Override
    public double getIconScale(MapType mapType) {
        return mapType.isMinimap() ? FTBChunksClientConfig.MINIMAP_ZOOM.get() : size;
    }

    @Override
    public boolean isZoomDependant(MapType mapType) {
        return false;
    }

    @Override
    public boolean isVisible(MapType mapType, double distanceToPlayer, boolean outsideVisibleArea) {
        return !outsideVisibleArea || distanceToPlayer <= MAX_DISTANCE;
    }

    @Override
    public Vec3 getPos(float v) {
        return net.minecraft.world.phys.Vec3.atCenterOf(getMiddleBlock());
    }

    public BlockPos getMiddleBlock() {
        return chunkPos.getBlockAt(7, 70, 7);
    }

    @Override
    public boolean onMousePressed(BaseScreen baseScreen, MouseButton mouseButton) {
        if (!isEnabled()) {
            return false;
        }

        MapDimension.getCurrent()
                .ifPresent(mapDimension -> FTBChunksAPI.clientApi()
                        .getWaypointManager(mapDimension.dimension)
                        .ifPresent(waypointManager -> {
                            var pos = getMiddleBlock();
                            var waypoint = new WaypointImpl(WaypointType.DEFAULT, mapDimension, pos);
                            if (!waypointManager.getAllWaypoints().contains(waypoint)) {
                                waypointManager.addWaypointAt(pos, getName())
                                        .setColor(getColor())
                                        .setHidden(false);
                                baseScreen.refreshWidgets();
                            }
                        }));
        return true;
    }

    @Override
    public void addTooltip(TooltipList list) {
        FluidRenderLayer.getTooltip(fluidInfo).forEach(list::add);
    }

    @Override
    public boolean onKeyPressed(BaseScreen baseScreen, Key key) {
        return false;
    }

    public Icon getIcon(int alpha, boolean mouseOver) {
        var color = getColor();
        var fluidIcon = Icon.getIcon(com.lowdragmc.lowdraglib.transfer.client.FluidVisuals.stillTexture(fluidInfo.fluid()))
                .withColor(Color4I.rgba(color).withAlpha(alpha));
        if (mouseOver) {
            fluidIcon = fluidIcon.withBorder(Color4I.rgba(color), false);
        }
        return fluidIcon;
    }

    @Override
    public void draw(MapType mapType, GuiGraphicsExtractor graphics, int x, int y, int w, int h, boolean outsideVisibleArea,
                     int iconAlpha) {
        if (!mapType.isMinimap() || !isEnabled()) {
            return;
        }
        if (icon == null) {
            icon = getIcon(200, false);
        }
        dev.ftb.mods.ftblibrary.client.icon.IconHelper.renderIcon(icon, graphics, x, y, w, h);
    }
}
