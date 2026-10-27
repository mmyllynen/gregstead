package dev.emi.emi.jemi.runtime;

import dev.emi.emi.api.EmiDragDropHandler;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.jemi.JemiPlugin;
import dev.emi.emi.jemi.JemiUtil;
import dev.emi.emi.runtime.EmiDrawContext;
import mezz.jei.api.ingredients.ITypedIngredient;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;

public class JemiDragDropHandler implements EmiDragDropHandler<Screen> {
    @Override
    public boolean dropStack(Screen screen, EmiIngredient stack, int x, int y) {
        if (stack.getEmiStacks().isEmpty()) return false;
        try {
            return JemiUtil.getTyped(stack.getEmiStacks().getFirst()).map(typed -> drop(screen, typed, x, y)).orElse(false);
        } catch (Exception e) {
            dev.emi.emi.runtime.EmiLog.error("Error dropping JEI ghost ingredient", e);
            return false;
        }
    }

    @Override
    public void render(Screen screen, EmiIngredient dragged, GuiGraphicsExtractor raw, int mouseX, int mouseY, float delta) {
        if (dragged.getEmiStacks().isEmpty()) return;
        JemiUtil.getTyped(dragged.getEmiStacks().getFirst()).ifPresent(typed -> renderTargets(screen, EmiDrawContext.wrap(raw), typed));
    }

    private <T> boolean drop(Screen screen, ITypedIngredient<T> typed, int x, int y) {
        for (var handler : JemiPlugin.runtime.getScreenHelper().getGhostIngredientHandlers(screen)) {
            try {
                for (var target : handler.getTargetsTyped(screen, typed, true)) {
                    if (target.getArea().contains(x, y)) {
                        target.accept(typed.getIngredient());
                        return true;
                    }
                }
            } finally {
                handler.onComplete();
            }
        }
        return false;
    }

    private <T> void renderTargets(Screen screen, EmiDrawContext context, ITypedIngredient<T> typed) {
        for (var handler : JemiPlugin.runtime.getScreenHelper().getGhostIngredientHandlers(screen)) {
            if (!handler.shouldHighlightTargets()) continue;
            for (var target : handler.getTargetsTyped(screen, typed, false)) {
                var area = target.getArea();
                context.fill(area.getX(), area.getY(), area.getWidth(), area.getHeight(), -2010989773);
            }
        }
    }
}
