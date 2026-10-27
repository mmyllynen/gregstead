package dev.emi.emi.jemi.impl;

import com.google.common.collect.Lists;
import java.util.List;
import mezz.jei.api.gui.builder.IIngredientAcceptor;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.recipe.RecipeIngredientRole;

public class JemiRecipeLayoutBuilder implements IRecipeLayoutBuilder {
   public final List<JemiIngredientAcceptor> ingredients = Lists.newArrayList();
   public final List<JemiRecipeSlotBuilder> slots = Lists.newArrayList();
   public final List<List<JemiRecipeSlotBuilder>> focusLinks = new java.util.ArrayList<>();
   public boolean shapeless = false;

   public IRecipeSlotBuilder addSlot(RecipeIngredientRole role) {
      return this.addSlot(role, 0, 0);
   }

   public IRecipeSlotBuilder addSlot(RecipeIngredientRole recipeIngredientRole, int x, int y) {
      JemiRecipeSlotBuilder builder = new JemiRecipeSlotBuilder(recipeIngredientRole, x, y);
      this.ingredients.add(builder.acceptor);
      this.slots.add(builder);
      return builder;
   }

   public IIngredientAcceptor<?> addInvisibleIngredients(RecipeIngredientRole recipeIngredientRole) {
      JemiIngredientAcceptor acceptor = new JemiIngredientAcceptor(recipeIngredientRole);
      this.ingredients.add(acceptor);
      return acceptor;
   }

   public void moveRecipeTransferButton(int posX, int posY) {
   }

   public void setShapeless() {
      this.shapeless = true;
   }

   public void setShapeless(int posX, int posY) {
      this.shapeless = true;
   }

   public void createFocusLink(IIngredientAcceptor<?>... slots) {
      createFocusLink(java.util.Arrays.asList(slots));
   }

   public void createFocusLink(java.util.Collection<? extends IIngredientAcceptor<?>> linked) {
      var group = new java.util.ArrayList<JemiRecipeSlotBuilder>();
      int count = -1;
      for (var acceptor : linked) {
         if (!(acceptor instanceof JemiRecipeSlotBuilder slot) || !slots.contains(slot))
            throw new IllegalArgumentException("Focus links require slots from this layout");
         int size = slot.nativeData().getAllIngredients().size();
         if (count >= 0 && count != size)
            throw new IllegalArgumentException("Focus-linked slots must contain the same number of ingredients");
         count = size;
         group.add(slot);
      }
      focusLinks.add(List.copyOf(group));
   }


   public boolean applyFocusLinks(mezz.jei.api.recipe.IFocusGroup focus) {
      var runtime = dev.emi.emi.jemi.JemiPlugin.runtime;
      var manager = (mezz.jei.library.ingredients.IIngredientManagerInternal)runtime.getIngredientManager();
      var visibility = runtime.getJeiHelpers().getIngredientVisibility();
      for (var group : focusLinks) {
         var link = new mezz.jei.library.ingredients.RecipeIngredientSupplier.FocusLink(group.stream()
            .map(slot -> new mezz.jei.library.ingredients.RecipeIngredientSupplier.FocusLink.Slot(
               slot.acceptor.role, slot.nativeData().getAllSlotIngredients())).toList());
         var indexes = link.getVisibleIngredientIndexes(focus, manager,
            ingredient -> visibility.isIngredientVisible(ingredient, mezz.jei.api.ingredients.subtypes.UidContext.Recipe));
         if (indexes == null) return false;
         group.forEach(slot -> slot.linkedIndexes = indexes);
      }
      return true;
   }

}
