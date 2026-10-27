package com.gregtechceu.gtceu.integration.map.xaeros.worldmap.ore;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconType;
import com.gregtechceu.gtceu.api.data.worldgen.ores.GeneratedVeinMetadata;
import com.gregtechceu.gtceu.client.util.DrawUtil;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.integration.map.GroupingMapRenderer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import xaero.map.element.MapElementGraphics;
import xaero.lib.client.graphics.XaeroBufferProvider;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.Identifier;
import net.minecraft.world.inventory.InventoryMenu;

import xaero.map.element.render.ElementReader;
import xaero.map.element.render.ElementRenderProvider;
import xaero.map.element.render.ElementRenderer;
import xaero.map.graphics.renderer.multitexture.MultiTextureRenderTypeRendererProvider;

public class OreVeinElementRenderer extends
                                    ElementRenderer<OreVeinElement, OreVeinElementContext, OreVeinElementRenderer> {

    protected static final Identifier STONE = Identifier.withDefaultNamespace("block/stone");

    protected OreVeinElementRenderer(OreVeinElementContext context,
                                     ElementRenderProvider<OreVeinElement, OreVeinElementContext> provider,
                                     ElementReader<OreVeinElement, OreVeinElementContext, OreVeinElementRenderer> reader) {
        super(context, provider, reader);
    }

    @Override
    public boolean shouldBeDimScaled() {
        return false;
    }

    @Override
    public void preRender(xaero.map.element.render.ElementRenderInfo info,XaeroBufferProvider buffers,
                          MultiTextureRenderTypeRendererProvider provider,boolean pre) {}
    @Override
    public void postRender(xaero.map.element.render.ElementRenderInfo info,XaeroBufferProvider buffers,
                           MultiTextureRenderTypeRendererProvider provider,boolean pre) {}
    @Override
    public void renderElementShadow(OreVeinElement element,boolean hovered,float scale,double x,double y,
                                    xaero.map.element.render.ElementRenderInfo info,MapElementGraphics graphics,
                                    XaeroBufferProvider buffers,MultiTextureRenderTypeRendererProvider provider) {}
    @Override
    public boolean renderElement(OreVeinElement element,boolean hovered,double depth,float optionalScale,
                                 double partialX,double partialY,xaero.map.element.render.ElementRenderInfo info,
                                 MapElementGraphics graphics,XaeroBufferProvider buffers,
                                 MultiTextureRenderTypeRendererProvider provider) {
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
        oreTexture = MaterialIconType.rawOre.getItemTexturePath(firstMaterial.getMaterialIconSet(), "secondary", true);
        if (oreTexture != null) {
            int materialSecondaryARGB = firstMaterial.getMaterialSecondaryARGB();
            var oreSprite = Minecraft.getInstance().getAtlasManager().get(new net.minecraft.client.resources.model.sprite.SpriteId(net.minecraft.client.renderer.texture.TextureAtlas.LOCATION_BLOCKS, oreTexture));
            graphics.flush();
            com.gregtechceu.gtceu.integration.map.xaeros.NativeXaeroIcon.draw(graphics.pose(),oreSprite,
                    -iconSize / 2,-iconSize / 2,iconSize,materialSecondaryARGB | 0xFF000000);
        }

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
    public boolean shouldRender(xaero.map.element.render.ElementRenderLocation location, boolean pre) {
        return GroupingMapRenderer.getInstance().doShowLayer("ore_veins");
    }

    public static final class Builder {

        private Builder() {}

        public OreVeinElementRenderer build() {
            return new OreVeinElementRenderer(new OreVeinElementContext(), new OreVeinElementRenderProvider(),
                    new OreVeinElementReader());
        }

        public static OreVeinElementRenderer.Builder begin() {
            return new OreVeinElementRenderer.Builder();
        }
    }
}
