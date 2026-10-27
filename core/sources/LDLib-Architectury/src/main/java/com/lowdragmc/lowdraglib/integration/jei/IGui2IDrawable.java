package com.lowdragmc.lowdraglib.integration.jei;

import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import mezz.jei.api.gui.drawable.IDrawable;
import net.minecraft.client.gui.GuiGraphicsExtractor;

import javax.annotation.Nonnull;

/**
 * @author KilaBash
 * @date 2022/04/30
 * @implNote IGui2IDrawable
 */
public interface IGui2IDrawable {
    static IDrawable toDrawable(IGuiTexture guiTexture, final int width, final int height) {
        return new IDrawable() {
            @Override
            public int getWidth() {
                return width;
            }

            @Override
            public int getHeight() {
                return height;
            }

            @Override
            public void draw(@Nonnull GuiGraphicsExtractor graphics, int x, int y) {
                if (guiTexture == null) return;
                guiTexture.draw(graphics, 0, 0, x, y, width, height);
            }
        };
    }
}
