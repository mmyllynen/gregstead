package com.lowdragmc.lowdraglib.gui.widget;

import com.lowdragmc.lowdraglib.editor.ColorPattern;
import com.lowdragmc.lowdraglib.editor.Icons;
import com.lowdragmc.lowdraglib.gui.util.TreeBuilder;
import com.lowdragmc.lowdraglib.utils.ColorUtils;
import com.lowdragmc.lowdraglib.math.GradientColor;
import lombok.Setter;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec2;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.function.Consumer;

/**
 * @author KilaBash
 * @date 2023/5/30
 * @implNote GradientColorWidget
 */
public class GradientColorWidget extends WidgetGroup {
    protected final GradientColor gradientColor;
    protected final HsbColorWidget hsbColorWidget;
    protected int selectedAlphaPoint = -1, selectedRGBPoint = 0;
    @Setter
    protected Consumer<GradientColor> onUpdate;
    // runtime
    private boolean isDraggingAlphaPoint, isDraggingRGBPoint;
    private long lastClickTick;

    public GradientColorWidget(int x, int y, int width, GradientColor gradientColor) {
        super(x, y, width, width + 15 + 20 + 3);
        this.gradientColor = gradientColor;
        addWidget(new ImageWidget(3, 3 + 10, width - 6, 15, ColorPattern.WHITE.borderTexture(1)));

        addWidget(hsbColorWidget = new HsbColorWidget(3, 15 + 20 + 3, width - 6, width - 6)
                .setColorSupplier(() -> {
                    var t = selectedAlphaPoint >= 0 ? gradientColor.getAP().get(selectedAlphaPoint).x :
                            selectedRGBPoint >= 0 ? gradientColor.getRP().get(selectedRGBPoint).x :
                            0;
                    return gradientColor.getColor(t);
                })
                .setOnChanged(color -> {
                    if (selectedAlphaPoint >= 0) {
                        gradientColor.getAP().set(selectedAlphaPoint, new Vec2(gradientColor.getAP().get(selectedAlphaPoint).x, ColorUtils.alpha(color)));
                        notifyChanged();
                    }
                    if (selectedRGBPoint >= 0) {
                        gradientColor.getRP().set(selectedRGBPoint, new Vec2(gradientColor.getRP().get(selectedRGBPoint).x, ColorUtils.red(color)));
                        gradientColor.getGP().set(selectedRGBPoint, new Vec2(gradientColor.getGP().get(selectedRGBPoint).x, ColorUtils.green(color)));
                        gradientColor.getBP().set(selectedRGBPoint, new Vec2(gradientColor.getBP().get(selectedRGBPoint).x, ColorUtils.blue(color)));
                        notifyChanged();
                    }
                }));
        hsbColorWidget.setShowAlpha(false);
        hsbColorWidget.setShowRGB(true);
    }

    protected void notifyChanged() {
        if (onUpdate != null) onUpdate.accept(gradientColor);
    }

    private float getXPosition(float t) {
        var size = getSize();
        var pos = getPosition();
        return pos.x + 3 + (size.width - 6) * t;
    }

    private float getXCoord(float x) {
        var size = getSize();
        var position = getPosition();
        return (x - position.x - 3) / (size.width - 6);
    }

    protected void openMenu(int mouseX, int mouseY) {
        if ((selectedAlphaPoint >= 0 && gradientColor.getAP().size() > 1) || (selectedRGBPoint >= 0 && gradientColor.getRP().size() > 1)) {
            var menu = TreeBuilder.Menu.start().leaf("Remove", () -> {
                if (selectedAlphaPoint >= 0) {
                    gradientColor.getAP().remove(selectedAlphaPoint);
                    selectedAlphaPoint = -1;
                    notifyChanged();
                } else if (selectedRGBPoint >= 0) {
                    gradientColor.getRP().remove(selectedRGBPoint);
                    gradientColor.getGP().remove(selectedRGBPoint);
                    gradientColor.getBP().remove(selectedRGBPoint);
                    selectedRGBPoint = -1;
                    notifyChanged();
                }
            });
            var widget = new MenuWidget<>(mouseX - getPosition().x, mouseY - getPosition().y, 14, menu.build())
                    .setNodeTexture(MenuWidget.NODE_TEXTURE)
                    .setLeafTexture(MenuWidget.LEAF_TEXTURE)
                    .setNodeHoverTexture(MenuWidget.NODE_HOVER_TEXTURE)
                    .setCrossLinePredicate(TreeBuilder.Menu::isCrossLine)
                    .setKeyIconSupplier(TreeBuilder.Menu::getIcon)
                    .setKeyNameSupplier(TreeBuilder.Menu::getName)
                    .setOnNodeClicked(TreeBuilder.Menu::handle);
            waitToAdded(widget.setBackground(MenuWidget.BACKGROUND));
        }

    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (super.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        if (isMouseOverElement(mouseX, mouseY)) {
            if (button == 1) {
                openMenu((int) mouseX, (int) mouseY);
            } else {
                var clickTick = gui.getTickCount();
                var size = getSize();
                var pos = getPosition();
                // click alpha point
                for (int i = 0; i < gradientColor.getAP().size(); i++) {
                    if (isMouseOver((int) (getXPosition(gradientColor.getAP().get(i).x) - 5), pos.y + 3, 10, 10, mouseX, mouseY)) {
                        selectedAlphaPoint = i;
                        selectedRGBPoint = -1;
                        isDraggingAlphaPoint = true;
                        hsbColorWidget.setShowAlpha(true);
                        hsbColorWidget.setShowRGB(false);
                        return true;
                    }
                }
                // click rgb point
                for (int i = 0; i < gradientColor.getRP().size(); i++) {
                    if (isMouseOver((int) (getXPosition(gradientColor.getRP().get(i).x) - 5), pos.y + 3 + 10 + 15, 10, 10, mouseX, mouseY)) {
                        selectedRGBPoint = i;
                        selectedAlphaPoint = -1;
                        isDraggingRGBPoint = true;
                        hsbColorWidget.setShowAlpha(false);
                        hsbColorWidget.setShowRGB(true);
                        return true;
                    }
                }
                // double click
                if (clickTick - lastClickTick < 12) {
                    // create alpha point
                    if (isMouseOver(pos.x + 3, pos.y + 3, size.width - 6, 10, mouseX, mouseY)) {
                        int color = gradientColor.getColor(getXCoord((float)mouseX));
                        gradientColor.addAlpha(getXCoord((float)mouseX), ColorUtils.alpha(color));
                        notifyChanged();
                        return true;
                    }
                    // create rgb point
                    if (isMouseOver(pos.x + 3, pos.y + 3 + 10 + 15, size.width - 6, 10, mouseX, mouseY)) {
                        int color = gradientColor.getColor(getXCoord((float)mouseX));
                        gradientColor.addRGB(getXCoord((float) mouseX), ColorUtils.red(color), ColorUtils.green(color), ColorUtils.blue(color));
                        notifyChanged();
                        return true;
                    }
                } else {
                    lastClickTick = clickTick;
                }
            }
        }
        return false;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        this.isDraggingRGBPoint = false;
        this.isDraggingAlphaPoint = false;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        var size = getSize();
        var pos = getPosition();
        if (mouseX >= pos.x + 3 && mouseX <= pos.x + size.width - 6) {
            if (selectedAlphaPoint >= 0 && this.isDraggingAlphaPoint) {
                var t = getXCoord((float) Mth.clamp(mouseX, pos.x + 3, pos.x + size.width - 6));
                selectedAlphaPoint = gradientColor.addAlpha(t,  gradientColor.getAP().remove(selectedAlphaPoint).y);
                notifyChanged();
                return true;
            }
            if (selectedRGBPoint >= 0 && this.isDraggingRGBPoint) {
                var t = getXCoord((float) Mth.clamp(mouseX, pos.x + 3, pos.x + size.width - 6));
                selectedRGBPoint = gradientColor.addRGB(t,
                        gradientColor.getRP().remove(selectedRGBPoint).y,
                        gradientColor.getGP().remove(selectedRGBPoint).y,
                        gradientColor.getBP().remove(selectedRGBPoint).y);
                notifyChanged();
                return true;
            }
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void drawInBackground(@NotNull @Nonnull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
        // render background
        drawBackgroundTexture(graphics, mouseX, mouseY);

        var size = getSize();
        var pos = getPosition();

        // render point
        for (int i = 0; i < gradientColor.getAP().size(); i++) {
            Icons.DOWN.copy().setColor(i == selectedAlphaPoint ? ColorPattern.GREEN.color : -1).draw(graphics, mouseX, mouseY, getXPosition(gradientColor.getAP().get(i).x) - 5, pos.y + 3, 10, 10);
        }

        for (int i = 0; i < gradientColor.getRP().size(); i++) {
            Icons.UP.copy().setColor(i == selectedRGBPoint ? ColorPattern.GREEN.color : -1).draw(graphics, mouseX, mouseY, getXPosition(gradientColor.getRP().get(i).x) - 5, pos.y + 3 + 10 + 15, 10, 10);
        }
        // render grid
        var width = size.width - 6;
        com.lowdragmc.lowdraglib.client.scene.NativeGradientBar.draw(graphics,pos.x+3,pos.y+13,width,
                ColorPattern.GRAY.color,t->gradientColor.getColor((float)t));

        // render children
        drawWidgetsBackground(graphics, mouseX, mouseY, partialTicks);
    }
}
