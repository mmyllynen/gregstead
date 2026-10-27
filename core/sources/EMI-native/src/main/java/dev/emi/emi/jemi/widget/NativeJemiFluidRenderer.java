package dev.emi.emi.jemi.widget;

import mezz.jei.api.gui.drawable.TilingDirection;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.common.platform.IPlatformFluidHelperInternal;
import mezz.jei.common.platform.Services;
import mezz.jei.library.render.FluidTankRenderer;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;
import dev.emi.emi.jemi.impl.JemiTooltipBuilder;

/** Uses the pinned native JEI renderer so tiling and capacity tooltip semantics match JEI. */
public record NativeJemiFluidRenderer<T>(IIngredientType<T> type, IIngredientRenderer<T> renderer) {
    public static NativeJemiFluidRenderer<?> create(long capacity, boolean showCapacity, int width, int height, TilingDirection direction) {
        return create(Services.PLATFORM.getFluidHelper(), capacity, showCapacity, width, height, direction);
    }
    private static <T> NativeJemiFluidRenderer<T> create(IPlatformFluidHelperInternal<T> helper,
            long capacity, boolean showCapacity, int width, int height, TilingDirection direction) {
        return new NativeJemiFluidRenderer<>(helper.getFluidIngredientType(),
            new FluidTankRenderer<>(helper, helper.getFluidIngredientType(), capacity, showCapacity, width, height, direction));
    }
    public void render(GuiGraphicsExtractor graphics, ITypedIngredient<?> ingredient, int x, int y) {
        ingredient.getIngredient(type).ifPresent(value -> renderer.render(graphics, value, x, y));
    }
    public void tooltip(JemiTooltipBuilder tooltip, ITypedIngredient<?> ingredient) {
        var client = Minecraft.getInstance();
        ingredient.getIngredient(type).ifPresent(value -> renderer.getTooltip(tooltip, value,
            Item.TooltipContext.of(client.level), client.player,
            client.options.advancedItemTooltips ? TooltipFlag.ADVANCED : TooltipFlag.NORMAL));
    }
}
