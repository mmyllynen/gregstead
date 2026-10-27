package dev.emi.emi.jemi.impl;

import com.google.common.collect.Maps;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotRichTooltipCallback;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.Nullable;

public class JemiRecipeSlotBuilder extends NativeIngredientAcceptor<IRecipeSlotBuilder> implements IRecipeSlotBuilder {
   public java.util.Set<Integer> linkedIndexes;
   public final JemiIngredientAcceptor acceptor;
   public dev.emi.emi.jemi.widget.NativeHoverPadding hoverPadding = dev.emi.emi.jemi.widget.NativeHoverPadding.NONE;
   public boolean large = false;
   public boolean defaultBackground = false;
   public int x;
   public int y;
   public Optional<String> name = Optional.empty();
   public IRecipeSlotRichTooltipCallback richTooltipCallback;
   public JemiRecipeSlot.OffsetDrawable background;
   public JemiRecipeSlot.OffsetDrawable overlay;
   public Map<IIngredientType<?>, JemiRecipeSlot.IngredientRenderer<?>> renderers;
   public JemiRecipeSlot.TankInfo tankInfo;

   public JemiRecipeSlotBuilder(RecipeIngredientRole role, int x, int y) {
      this.acceptor = new JemiIngredientAcceptor(role);
      this.x = x;
      this.y = y;
   }

   @Override protected IRecipeSlotBuilder self() { return this; }
   @Override protected void replace(List<dev.emi.emi.api.stack.EmiStack> values) { acceptor.replace(values); }
   @Override public mezz.jei.library.ingredients.DisplayIngredientAcceptor nativeData() { return acceptor.nativeData(); }

   public <I> IRecipeSlotBuilder addIngredients(IIngredientType<I> ingredientType, List<@Nullable I> ingredients) {
      this.acceptor.addIngredients(ingredientType, ingredients);
      return this;
   }

   public <I> IRecipeSlotBuilder add(IIngredientType<I> ingredientType, I ingredient) {
      this.acceptor.add(ingredientType, ingredient);
      return this;
   }

   public IRecipeSlotBuilder addIngredientsUnsafe(List<?> ingredients) {
      this.acceptor.addIngredientsUnsafe(ingredients);
      return this;
   }

   public IRecipeSlotBuilder add(Fluid fluid, long amount) {
      this.acceptor.add(fluid, amount);
      return this;
   }

   public IRecipeSlotBuilder add(Fluid fluid, long amount, DataComponentPatch componentChanges) {
      this.acceptor.add(fluid, amount, componentChanges);
      return this;
   }

   public IRecipeSlotBuilder setSlotName(String slotName) {
      this.name = Optional.ofNullable(slotName);
      return this;
   }

   public IRecipeSlotBuilder setBackground(IDrawable background, int xOffset, int yOffset) {
      this.background = new JemiRecipeSlot.OffsetDrawable(background, xOffset, yOffset);
      return this;
   }

   @Override
   public IRecipeSlotBuilder setHoverPadding(int top, int bottom, int left, int right) {
      hoverPadding = new dev.emi.emi.jemi.widget.NativeHoverPadding(top,bottom,left,right);
      return this;
   }

   public IRecipeSlotBuilder setOverlay(IDrawable overlay, int xOffset, int yOffset) {
      this.overlay = new JemiRecipeSlot.OffsetDrawable(overlay, xOffset, yOffset);
      return this;
   }

   public IRecipeSlotBuilder setFluidRenderer(long capacity, boolean showCapacity, int width, int height) {
      return setFluidRenderer(capacity, showCapacity, width, height, mezz.jei.api.gui.drawable.TilingDirection.UP_RIGHT);
   }

   public IRecipeSlotBuilder setFluidRenderer(long capacity, boolean showCapacity, int width, int height,
           mezz.jei.api.gui.drawable.TilingDirection direction) {
      if (capacity <= 0 || width <= 0 || height <= 0) throw new IllegalArgumentException("Fluid renderer capacity and dimensions must be positive");
      this.tankInfo = new JemiRecipeSlot.TankInfo(width, height, capacity, showCapacity, java.util.Objects.requireNonNull(direction));
      return this;
   }

   public <T> IRecipeSlotBuilder setCustomRenderer(IIngredientType<T> ingredientType, IIngredientRenderer<T> ingredientRenderer) {
      if (this.renderers == null) {
         this.renderers = Maps.newHashMap();
      }

      this.renderers.put(ingredientType, new JemiRecipeSlot.IngredientRenderer(ingredientType, ingredientRenderer));
      return this;
   }

   public IRecipeSlotBuilder addTypedIngredients(List<ITypedIngredient<?>> ingredients) {
      this.acceptor.addTypedIngredients(ingredients);
      return this;
   }

   public IRecipeSlotBuilder addOptionalTypedIngredients(List<Optional<ITypedIngredient<?>>> ingredients) {
      this.acceptor.addOptionalTypedIngredients(ingredients);
      return this;
   }

   public IRecipeSlotBuilder addRichTooltipCallback(IRecipeSlotRichTooltipCallback tooltipCallback) {
      var previous = this.richTooltipCallback;
      this.richTooltipCallback = previous == null ? tooltipCallback : (slot, tooltip) -> {
          previous.onRichTooltip(slot, tooltip);
          tooltipCallback.onRichTooltip(slot, tooltip);
      };
      return this;
   }

   public IRecipeSlotBuilder add(Fluid fluid) {
      this.acceptor.add(fluid);
      return this;
   }

   public IRecipeSlotBuilder setPosition(int xPos, int yPos) {
      this.x = xPos;
      this.y = yPos;
      return this;
   }

   @Override
   public IRecipeSlotBuilder setPosition(int areaX, int areaY, int areaWidth, int areaHeight,
           mezz.jei.api.gui.placement.HorizontalAlignment horizontal,
           mezz.jei.api.gui.placement.VerticalAlignment vertical) {
      return setPosition(areaX + horizontal.getXPos(areaWidth, getWidth()),
          areaY + vertical.getYPos(areaHeight, getHeight()));
   }

   public int getWidth() {
      return this.large ? 26 : 18;
   }

   public int getHeight() {
      return this.large ? 26 : 18;
   }

   public IRecipeSlotBuilder setStandardSlotBackground() {
      this.defaultBackground = true;
      return setHoverPadding(1,1,1,1);
   }

   public IRecipeSlotBuilder setOutputSlotBackground() {
      this.defaultBackground = true;
      this.large = true;
      return this;
   }
}
