package com.lowdragmc.lowdraglib.gui.texture;

import com.lowdragmc.lowdraglib.LDLib;
import com.lowdragmc.lowdraglib.editor.ColorPattern;
import com.lowdragmc.lowdraglib.editor.annotation.Configurable;
import com.lowdragmc.lowdraglib.editor.annotation.NumberColor;
import com.lowdragmc.lowdraglib.editor.annotation.NumberRange;
import com.lowdragmc.lowdraglib.editor.configurator.ConfiguratorGroup;
import com.lowdragmc.lowdraglib.editor.configurator.WrapperConfigurator;
import com.lowdragmc.lowdraglib.editor.ui.Editor;
import com.lowdragmc.lowdraglib.gui.widget.ButtonWidget;
import com.lowdragmc.lowdraglib.gui.widget.DialogWidget;
import com.lowdragmc.lowdraglib.gui.widget.ImageWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.registry.annotation.LDLRegisterClient;
import lombok.NoArgsConstructor;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.Identifier;

import java.io.File;
import java.util.function.IntSupplier;


@LDLRegisterClient(name = "resource_texture", registry = "ldlib:gui_texture")
@NoArgsConstructor
public class ResourceTexture extends TransformTexture {

    @Configurable(name = "ldlib.gui.editor.name.resource", forceUpdate = false)
    public Identifier imageLocation = LDLib.id("textures/gui/icon.png");

    @Configurable
    @NumberRange(range = {-Float.MAX_VALUE, Float.MAX_VALUE}, wheel = 0.02)
    public float offsetX = 0;

    @Configurable
    @NumberRange(range = {-Float.MAX_VALUE, Float.MAX_VALUE}, wheel = 0.02)
    public float offsetY = 0;

    @Configurable
    @NumberRange(range = {-Float.MAX_VALUE, Float.MAX_VALUE}, wheel = 0.02)
    public float imageWidth = 1;
    @Configurable
    @NumberRange(range = {-Float.MAX_VALUE, Float.MAX_VALUE}, wheel = 0.02)
    public float imageHeight = 1;

    @Configurable
    @NumberColor
    protected int color = -1;
    protected IntSupplier dynamicColor = () -> color;

    public ResourceTexture(Identifier imageLocation, float offsetX, float offsetY, float width, float height) {
        this.imageLocation = imageLocation;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.imageWidth = width;
        this.imageHeight = height;
    }

    public ResourceTexture(String imageLocation) {
        this(Identifier.parse(imageLocation));
    }

    public ResourceTexture(Identifier imageLocation) {
        this(imageLocation, 0, 0, 1, 1);
    }

    public ResourceTexture getSubTexture(float offsetX, float offsetY, float width, float height) {
        return new ResourceTexture(imageLocation,
                this.offsetX + (imageWidth * offsetX),
                this.offsetY + (imageHeight * offsetY),
                this.imageWidth * width,
                this.imageHeight * height);
    }

    public ResourceTexture getSubTexture(double offsetX, double offsetY, double width, double height) {
        return new ResourceTexture(imageLocation,
                this.offsetX + (float)(imageWidth * offsetX),
                this.offsetY + (float)(imageHeight * offsetY),
                this.imageWidth * (float) width,
                this.imageHeight * (float)height);
    }

    public ResourceTexture copy() {
        return getSubTexture(0, 0, 1, 1);
    }

    public ResourceTexture setColor(int color) {
        this.color = color;
        return this;
    }

    public ResourceTexture setDynamicColor(IntSupplier color) {
        this.dynamicColor = color;
        return this;
    }

    public static ResourceTexture fromSpirit(Identifier texture) {
        if (LDLib.isClient()) {
            var sprite = Minecraft.getInstance().getAtlasManager().get(
                    new net.minecraft.client.resources.model.sprite.SpriteId(net.minecraft.client.renderer.texture.TextureAtlas.LOCATION_BLOCKS, texture));
            return new ResourceTexture(TextureAtlas.LOCATION_BLOCKS, sprite.getU0(), sprite.getV0(), sprite.getU1() - sprite.getU0(), sprite.getV1() - sprite.getV0());
        } else {
            return new ResourceTexture("");
        }
    }

    @OnlyIn(Dist.CLIENT)
    protected void drawInternal(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float x, float y, int width, int height) {
        drawSubArea(graphics, x, y, width, height, 0, 0, 1, 1);
    }
    
    @OnlyIn(Dist.CLIENT)
    protected void drawSubAreaInternal(GuiGraphicsExtractor graphics, float x, float y, float width, float height, float drawnU, float drawnV, float drawnWidth, float drawnHeight) {
        //sub area is just different width and height
        float imageU = this.offsetX + (this.imageWidth * drawnU);
        float imageV = this.offsetY + (this.imageHeight * drawnV);
        float imageWidth = this.imageWidth * drawnWidth;
        float imageHeight = this.imageHeight * drawnHeight;
        com.lowdragmc.lowdraglib.transfer.client.GuiPrimitives.textured(graphics, imageLocation,
                x, y, x + width, y + height, imageU, imageV, imageU + imageWidth, imageV + imageHeight,
                0, dynamicColor.getAsInt());
    }

    @Override
    public void createPreview(ConfiguratorGroup father) {
        super.createPreview(father);
        WrapperConfigurator base = new WrapperConfigurator("ldlib.gui.editor.group.base_image", wrapper -> {
            WidgetGroup widgetGroup = new WidgetGroup(0, 0, 100, 100);
            widgetGroup.addWidget(new ImageWidget(0, 0, 100, 100, () -> new GuiTextureGroup(new ResourceTexture(imageLocation.toString()), this::drawGuides)).setBorder(2, ColorPattern.T_WHITE.color));
            widgetGroup.addWidget(new ButtonWidget(0, 0, 100, 100, IGuiTexture.EMPTY, cd -> {
                if (Editor.INSTANCE == null) return;
                File path = new File(Editor.INSTANCE.getWorkSpace(), "textures");
                DialogWidget.showFileDialog(Editor.INSTANCE, "ldlib.gui.editor.tips.select_image", path, true,
                        DialogWidget.suffixFilter(".png"), r -> {
                            if (r != null && r.isFile()) {
                                imageLocation = getTextureFromFile(path, r);
                                offsetX = 0;
                                offsetY = 0;
                                imageWidth = 1;
                                imageHeight = 1;
                                wrapper.notifyChanges();
                            }
                        });
            }));
            return widgetGroup;
        });
        base.setTips("ldlib.gui.editor.tips.click_select_image");
        father.addConfigurators(base);
    }

    private Identifier getTextureFromFile(File path, File r){
        var id = path.getPath().replace('\\', '/').split("assets/")[1].split("/")[0];
        return Identifier.fromNamespaceAndPath(id, r.getPath().replace(path.getPath(), "textures").replace('\\', '/'));
    }

    @OnlyIn(Dist.CLIENT)
    protected void drawGuides(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float x, float y, int width, int height) {
        new ColorBorderTexture(-1, 0xffff0000).draw(graphics, 0, 0,
                x + width * offsetX, y + height * offsetY,
                (int) (width * imageWidth), (int) (height * imageHeight));
    }
}
