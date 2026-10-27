package com.gregtechceu.gtceu.core.mixins.xaeroworldmap;

import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.integration.map.xaeros.worldmap.ore.OreVeinElementRenderer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import xaero.map.element.MapElementRenderHandler;
import xaero.map.element.render.ElementRenderer;

import java.util.List;

@Mixin(value = MapElementRenderHandler.Builder.class, remap = false)
public class MapElementRenderHandlerBuilderMixin {

    @ModifyArg(method = "build", at = @At(value = "INVOKE",
            target = "Lxaero/map/element/MapElementRenderHandler;<init>(Ljava/util/List;Lxaero/map/element/render/ElementRenderLocation;Lxaero/map/element/MapElementGraphics;)V"), index = 0)
    private List<ElementRenderer<?, ?, ?>> gtceu$addOreRenderer(List<ElementRenderer<?, ?, ?>> value) {
        if (ConfigHolder.INSTANCE.compat.minimap.toggle.xaerosMapIntegration) {
            value.add(OreVeinElementRenderer.Builder.begin().build());
        }
        return value;
    }
}
