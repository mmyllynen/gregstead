package com.lowdragmc.lowdraglib.gui.texture;

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
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.io.File;


/**
 * @author KilaBash
 * @date 2022/9/14
 * @implNote AnimationTexture
 */
@LDLRegisterClient(name = "animation_texture", registry = "ldlib:gui_texture")
public class AnimationTexture extends TransformTexture {

    @Configurable(name = "ldlib.gui.editor.name.resource")
    public Identifier imageLocation;

    @Configurable(tips = "ldlib.gui.editor.tips.cell_size")
    @NumberRange(range = {1, Integer.MAX_VALUE})
    @Getter
    protected int cellSize;

    @Configurable(tips = "ldlib.gui.editor.tips.cell_from")
    @NumberRange(range = {0, Integer.MAX_VALUE})
    @Getter
    protected int from;

    @Configurable(tips = "ldlib.gui.editor.tips.cell_to")
    @NumberRange(range = {0, Integer.MAX_VALUE})
    @Getter
    protected int to;

    @Configurable(tips = "ldlib.gui.editor.tips.cell_animation")
    @NumberRange(range = {0, Integer.MAX_VALUE})
    @Getter
    protected int animation;

    @Configurable
    @NumberColor
    @Getter
    protected int color = -1;

    protected int currentFrame;

    protected int currentTime;
    private long lastTick;

    public AnimationTexture() {
        this("ldlib:textures/gui/particles.png");
        setCellSize(8).setAnimation(32,  44).setAnimation(1);
    }

    public AnimationTexture(String imageLocation) {
        this.imageLocation = Identifier.parse(imageLocation);
    }

    public AnimationTexture(Identifier imageLocation) {
        this.imageLocation = imageLocation;
    }

    public AnimationTexture copy() {
        return new AnimationTexture(imageLocation).setCellSize(cellSize).setAnimation(from, to).setAnimation(animation).setColor(color);
    }

    public AnimationTexture setTexture(String imageLocation) {
        this.imageLocation = Identifier.parse(imageLocation);
        return this;
    }

    public AnimationTexture setCellSize(int cellSize) {
        this.cellSize = cellSize;
        return this;
    }

    public AnimationTexture setAnimation(int from, int to) {
        this.currentFrame = from;
        this.from = from;
        this.to = to;
        return this;
    }

    public AnimationTexture setAnimation(int animation) {
        this.animation = animation;
        return this;
    }

    @Override
    public AnimationTexture setColor(int color) {
        this.color = color;
        return this;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void updateTick() {
        if (Minecraft.getInstance().level != null) {
            long tick = Minecraft.getInstance().level.getGameTime();
            if (tick == lastTick) return;
            lastTick = tick;
        }
        if (currentTime >= animation) {
            currentTime = 0;
            currentFrame += 1;
        } else {
            currentTime++;
        }
        if (currentFrame > to) {
            currentFrame = from;
        } else if (currentFrame < from) {
            currentFrame = from;
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    protected void drawInternal(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float x, float y, int width, int height) {
        updateTick();
        NativeAnimationFrame.draw(graphics, imageLocation, cellSize, currentFrame, x, y, width, height, color);
    }

    @Override
    public void createPreview(ConfiguratorGroup father) {
        super.createPreview(father);
        WrapperConfigurator base = new WrapperConfigurator("ldlib.gui.editor.group.base_image", wrapper -> {
            WidgetGroup widgetGroup = new WidgetGroup(0, 0, 100, 100);
            ImageWidget imageWidget;
            widgetGroup.addWidget(imageWidget = new ImageWidget(0, 0, 100, 100, new GuiTextureGroup(new ResourceTexture(imageLocation.toString()), this::drawGuides)).setBorder(2, ColorPattern.T_WHITE.color));
            widgetGroup.addWidget(new ButtonWidget(0, 0, 100, 100, IGuiTexture.EMPTY, cd -> {
                if (Editor.INSTANCE == null) return;
                File path = new File(Editor.INSTANCE.getWorkSpace(), "textures");
                DialogWidget.showFileDialog(Editor.INSTANCE, "ldlib.gui.editor.tips.select_image", path, true,
                        DialogWidget.suffixFilter(".png"), r -> {
                            if (r != null && r.isFile()) {
                                imageLocation = getTextureFromFile(path, r);
                                cellSize = 1;
                                from = 0;
                                to = 0;
                                animation = 0;
                                imageWidget.setImage(new GuiTextureGroup(new ResourceTexture(imageLocation.toString()), this::drawGuides));
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
        float cell = 1f / this.cellSize;
        int X = from % cellSize;
        int Y = from / cellSize;

        float imageU = X * cell;
        float imageV = Y * cell;

        new ColorBorderTexture(-1, 0xff00ff00).draw(graphics, 0, 0,
                x + width * imageU, y + height * imageV,
                (int) (width * (cell)), (int) (height * (cell)));

        X = to % cellSize;
        Y = to / cellSize;

        imageU = X * cell;
        imageV = Y * cell;

        new ColorBorderTexture(-1, 0xffff0000).draw(graphics, 0, 0,
                x + width * imageU, y + height * imageV,
                (int) (width * (cell)), (int) (height * (cell)));
    }
}
