package dev.emi.emi.jemi.impl;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import mezz.jei.api.gui.ingredient.IRecipeSlotView;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.RecipeIngredientRole;

public class JemiRecipeSlotsView implements IRecipeSlotsView {
   private List<IRecipeSlotView> slots = Lists.newArrayList();

   public JemiRecipeSlotsView(List<JemiRecipeSlot> slots) {
      this.slots.addAll(slots);
   }

   public List<IRecipeSlotView> getSlotViews() {
      return this.slots;
   }

   public List<IRecipeSlotView> getSlotViews(RecipeIngredientRole role) {
      return this.slots.stream().filter(s -> Objects.equals(s.getRole(), role)).toList();
   }

   public Optional<IRecipeSlotView> findSlotByName(String slotName) {
      for (IRecipeSlotView slot : this.slots) {
         if (Objects.equals(slot.getSlotName().orElseGet(() -> null), slotName)) {
            return Optional.of(slot);
         }
      }

      return Optional.empty();
   }
}
