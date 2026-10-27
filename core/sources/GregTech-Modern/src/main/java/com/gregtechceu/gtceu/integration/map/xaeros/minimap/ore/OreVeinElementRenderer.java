package com.gregtechceu.gtceu.integration.map.xaeros.minimap.ore;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconType;
import com.gregtechceu.gtceu.api.data.worldgen.ores.GeneratedVeinMetadata;
import com.gregtechceu.gtceu.client.util.DrawUtil;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.integration.map.GroupingMapRenderer;

import net.minecraft.client.Minecraft;
import xaero.hud.minimap.element.render.MinimapElementGraphics;
import xaero.lib.client.graphics.XaeroBufferProvider;
import net.minecraft.resources.Identifier;
import net.minecraft.world.inventory.InventoryMenu;

import xaero.common.graphics.renderer.multitexture.MultiTextureRenderTypeRendererProvider;
import xaero.hud.minimap.element.render.MinimapElementRenderInfo;
import xaero.hud.minimap.element.render.MinimapElementRenderLocation;
import xaero.hud.minimap.element.render.MinimapElementRenderer;

public class OreVeinElementRenderer extends MinimapElementRenderer<OreVeinElement, OreVeinElementContext> {

    protected static final Identifier STONE = Identifier.withDefaultNamespace("block/stone");

    private OreVeinElementRenderer(OreVeinElementReader elementReader,
                                   OreVeinElementRenderProvider provider,
                                   OreVeinElementContext context) {
        super(elementReader, provider, context);
    }

    @Override
    public void preRender(MinimapElementRenderInfo renderInfo, XaeroBufferProvider renderTypeBuffers,
                          MultiTextureRenderTypeRendererProvider multiTextureRenderTypeRenderers) {}

    @Override
    public boolean renderElement(OreVeinElement element,
                                 boolean highlit,
                                 boolean outOfBounds,
                                 double optionalDepth, float optionalScale, double partialX, double partialY,
                                 MinimapElementRenderInfo renderInfo,
                                 MinimapElementGraphics graphics, XaeroBufferProvider renderTypeBuffers) {
        GeneratedVeinMetadata vein = element.getVein();
        int iconSize = ConfigHolder.INSTANCE.compat.minimap.oreIconSize;

        Material firstMaterial = vein.definition().value().veinGenerator().getAllMaterials(java.util.Objects.requireNonNull(net.minecraft.client.Minecraft.getInstance().level, "Vein preview requires a loaded world")).getFirst();
        int materialARGB = firstMaterial.getMaterialARGB();

        Identifier oreTexture = MaterialIconType.rawOre.getItemTexturePath(firstMaterial.getMaterialIconSet(),
                true);
        if (oreTexture != null) {
            var oreSprite = Minecraft.getInstance().getAtlasManager().get(new net.minecraft.client.resources.model.sprite.SpriteId(net.minecraft.client.renderer.texture.TextureAtlas.LOCATION_BLOCKS, oreTexture));
            graphics.flush();
            com.gregtechceu.gtceu.integration.map.xaeros.NativeXaeroIcon.draw(graphics.pose(),oreSprite,
                    -iconSize / 2,-iconSize / 2,iconSize,materialARGB | 0xFF000000);
        }
        // FIXME drawing the 2nd layer makes xaero's minimap transparent. so we won't. for now.
        // oreTexture = MaterialIconType.rawOre.getItemTexturePath(firstMaterial.getMaterialIconSet(), "secondary",
        // true);
        // if (oreTexture != null) {
        // int materialSecondaryARGB = firstMaterial.getMaterialSecondaryARGB();
        // colors = DrawUtil.floats(materialSecondaryARGB);
        // var oreSprite = Minecraft.getInstance()
        // .getTextureAtlas(net.minecraft.client.renderer.texture.TextureAtlas.LOCATION_BLOCKS)
        // .apply(oreTexture);
        // graphics.blit(-iconSize / 2, -iconSize / 2, 0, iconSize, iconSize,
        // oreSprite, colors[0], colors[1], colors[2], 1);
        // }

        int borderColor = ConfigHolder.INSTANCE.compat.minimap.getBorderColor(materialARGB | 0xFF000000);
        if ((borderColor & 0xFF000000) != 0) {
            int thickness = iconSize / 16;
            graphics.fill(-iconSize / 2, -iconSize / 2, iconSize, thickness, borderColor);
            graphics.fill(-iconSize / 2, -iconSize / 2 - thickness, iconSize, thickness, borderColor);
            graphics.fill(-iconSize / 2, -iconSize / 2, thickness, iconSize, borderColor);
            graphics.fill(-iconSize / 2 - thickness, -iconSize / 2, thickness, iconSize, borderColor);
        }
        return true;
    }

    @Override
    public void postRender(MinimapElementRenderInfo renderInfo, XaeroBufferProvider renderTypeBuffers,
                           MultiTextureRenderTypeRendererProvider multiTextureRenderTypeRenderers) {}

    @Override
    public boolean shouldRender(MinimapElementRenderLocation location) {
        return GroupingMapRenderer.getInstance().doShowLayer("ore_veins") &&
                location == MinimapElementRenderLocation.IN_MINIMAP;
    }

    public static final class Builder {

        private Builder() {}

        public OreVeinElementRenderer build() {
            return new OreVeinElementRenderer(new OreVeinElementReader(), new OreVeinElementRenderProvider(),
                    new OreVeinElementContext());
        }

        public static OreVeinElementRenderer.Builder begin() {
            return new OreVeinElementRenderer.Builder();
        }
    }
}
