package dev.emi.emi.jemi.impl;

import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.SlotWidget;
import dev.emi.emi.jemi.JemiUtil;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotRichTooltipCallback;
import mezz.jei.api.gui.ingredient.IRecipeSlotView;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

public class JemiRecipeSlot implements IRecipeSlotView {
   public final RecipeIngredientRole role;
   public final dev.emi.emi.jemi.widget.NativeHoverPadding hoverPadding;
   public final boolean large;
   public final boolean defaultBackground;
   public final int x;
   public final int y;
   public final Optional<String> name;
   public final IRecipeSlotRichTooltipCallback richTooltipCallback;
   public final JemiRecipeSlot.OffsetDrawable background;
   public final JemiRecipeSlot.OffsetDrawable overlay;
   public final Map<IIngredientType<?>, JemiRecipeSlot.IngredientRenderer<?>> renderers;
   public final JemiRecipeSlot.TankInfo tankInfo;
   public final EmiIngredient stack;
   public SlotWidget widget;
   public int highlight = 0;
   public final NativeSlotChoices choices;

   public JemiRecipeSlot(JemiRecipeSlotBuilder builder) {
      this(builder, dev.emi.emi.jemi.JemiPlugin.runtime.getJeiHelpers().getFocusFactory().getEmptyFocusGroup());
   }
   public JemiRecipeSlot(JemiRecipeSlotBuilder builder, mezz.jei.api.recipe.IFocusGroup focus) {
      this.hoverPadding = builder.hoverPadding;
      this.role = builder.acceptor.role;
      this.large = builder.large;
      this.defaultBackground = builder.defaultBackground;
      this.x = builder.x;
      this.y = builder.y;
      this.name = builder.name;
      this.richTooltipCallback = builder.richTooltipCallback;
      this.background = builder.background;
      this.overlay = builder.overlay;
      this.renderers = builder.renderers;
      this.tankInfo = builder.tankInfo;
      this.stack = builder.acceptor.build();
      this.choices = new NativeSlotChoices(dev.emi.emi.jemi.JemiPlugin.runtime.getIngredientManager(),builder.nativeData(),role,
          focus,builder.linkedIndexes,() -> highlight=0);
   }

   public JemiRecipeSlot(RecipeIngredientRole role, EmiStack stack) {
      this.hoverPadding = dev.emi.emi.jemi.widget.NativeHoverPadding.NONE;
      this.role = role;
      this.large = false;
      this.defaultBackground = false;
      this.x = 0;
      this.y = 0;
      this.name = Optional.empty();
      this.richTooltipCallback = null;
      this.background = null;
      this.overlay = null;
      this.renderers = null;
      this.tankInfo = null;
      this.stack = stack;
      var source = new JemiIngredientAcceptor(role);
      JemiUtil.getTyped(stack).ifPresent(source::add);
      this.choices = new NativeSlotChoices(dev.emi.emi.jemi.JemiPlugin.runtime.getIngredientManager(),source.nativeData(),role,
          dev.emi.emi.jemi.JemiPlugin.runtime.getJeiHelpers().getFocusFactory().getEmptyFocusGroup(),() -> highlight=0);
   }

   public @Unmodifiable List<@Nullable ITypedIngredient<?>> getAllIngredientsList() { return choices.all(); }
   public Stream<ITypedIngredient<?>> getAllIngredients() { return choices.allValid(); }
   public Stream<ITypedIngredient<?>> getDisplayedIngredients() { return choices.displayedAll(System.currentTimeMillis()/1000); }
   public Optional<net.minecraft.tags.TagKey<?>> getTagKey() { return choices.tag(System.currentTimeMillis()/1000); }
   public Optional<ITypedIngredient<?>> getDisplayedIngredient() { return choices.displayed(System.currentTimeMillis()/1000); }

   public Optional<String> getSlotName() {
      return this.name;
   }

   public RecipeIngredientRole getRole() {
      return this.role;
   }

   public void drawHighlight(GuiGraphicsExtractor raw, int color) {
      this.highlight = color;
   }

   public record IngredientRenderer<T>(IIngredientType<T> type, IIngredientRenderer<T> renderer) {
   }

   public record OffsetDrawable(IDrawable drawable, int xOff, int yOff) {
   }

   public record TankInfo(int width, int height, long capacity, boolean showCapacity, mezz.jei.api.gui.drawable.TilingDirection direction) {
   }
}
