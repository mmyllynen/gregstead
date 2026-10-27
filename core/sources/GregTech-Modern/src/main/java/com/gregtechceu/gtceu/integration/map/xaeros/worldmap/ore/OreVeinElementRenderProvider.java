package com.gregtechceu.gtceu.integration.map.xaeros.worldmap.ore;

import com.gregtechceu.gtceu.integration.map.xaeros.XaerosRenderer;

import xaero.map.WorldMap;
import xaero.map.element.render.ElementRenderProvider;

import java.util.Iterator;

public class OreVeinElementRenderProvider extends ElementRenderProvider<OreVeinElement, OreVeinElementContext> {

    private Iterator<OreVeinElement> iterator;

    public OreVeinElementRenderProvider() {}

    public void begin(xaero.map.element.render.ElementRenderLocation location, OreVeinElementContext context) {
        if (WorldMap.INSTANCE.getConfigs().getClientConfigManager().getEffective(xaero.map.common.config.option.WorldMapProfiledConfigOptions.WAYPOINTS)) {
            this.iterator = XaerosRenderer.oreElements.values()
                    .stream()
                    .map(element -> new OreVeinElement(element.getVein(), element.getName()))
                    .iterator();
            context.worldmapWaypointsScale = WorldMap.INSTANCE.getConfigs().getClientConfigManager().getEffective(xaero.map.common.config.option.WorldMapProfiledConfigOptions.WAYPOINT_SCALE).floatValue();
        } else {
            this.iterator = null;
        }
    }

    public boolean hasNext(xaero.map.element.render.ElementRenderLocation location, OreVeinElementContext context) {
        return this.iterator != null && this.iterator.hasNext();
    }

    public OreVeinElement getNext(xaero.map.element.render.ElementRenderLocation location, OreVeinElementContext context) {
        return this.iterator.next();
    }

    public void end(xaero.map.element.render.ElementRenderLocation location, OreVeinElementContext context) {}
}
