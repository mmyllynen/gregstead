package dev.emi.emi.jemi.impl;

import java.util.List;
import mezz.jei.api.gui.ingredient.IRecipeSlotDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotDrawablesView;
import org.jetbrains.annotations.Unmodifiable;

public class JemiRecipeSlotDrawablesView implements IRecipeSlotDrawablesView {
   private final List<IRecipeSlotDrawable> slots;
   public JemiRecipeSlotDrawablesView(List<? extends IRecipeSlotDrawable> slots) { this.slots=List.copyOf(slots); }
   public @Unmodifiable List<IRecipeSlotDrawable> getSlots() { return slots; }
}
