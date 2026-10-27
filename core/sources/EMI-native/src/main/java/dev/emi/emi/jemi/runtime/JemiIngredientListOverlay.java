package dev.emi.emi.jemi.runtime;

import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.stack.EmiStackInteraction;
import dev.emi.emi.config.SidebarType;
import dev.emi.emi.jemi.JemiUtil;
import dev.emi.emi.screen.EmiScreenManager;
import dev.emi.emi.screen.EmiScreenManager.SidebarEmiStackInteraction;
import dev.emi.emi.screen.EmiScreenManager.SidebarPanel;
import java.util.List;
import java.util.Optional;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.runtime.IIngredientListOverlay;
import org.jetbrains.annotations.Nullable;

public class JemiIngredientListOverlay implements IIngredientListOverlay {
   public Optional<ITypedIngredient<?>> getIngredientUnderMouse() {
      EmiStackInteraction stack = EmiScreenManager.getHoveredStack(EmiScreenManager.lastMouseX, EmiScreenManager.lastMouseY, false);
      return stack instanceof SidebarEmiStackInteraction sesi && sesi.getType() == SidebarType.INDEX && sesi.getStack().getEmiStacks().size() == 1
         ? JemiUtil.getTyped((EmiStack)stack.getStack().getEmiStacks().get(0))
         : Optional.empty();
   }

   @Nullable
   public <T> T getIngredientUnderMouse(IIngredientType<T> ingredientType) {
      Optional<ITypedIngredient<?>> opt = this.getIngredientUnderMouse();
      return opt.isPresent() ? opt.get().getIngredient(ingredientType).orElseGet(() -> null) : null;
   }

   public boolean isListDisplayed() {
      return !EmiScreenManager.isDisabled() && EmiScreenManager.hasSidebarAvailable(SidebarType.INDEX);
   }

   public boolean hasKeyboardFocus() {
      return EmiScreenManager.search.isFocused();
   }

   public <T> List<T> getVisibleIngredients(IIngredientType<T> ingredientType) {
      SidebarPanel panel = EmiScreenManager.getPanelFor(SidebarType.INDEX);
      return panel != null
         ? panel.space
            .getPage(panel.page)
            .stream()
            .map(i -> JemiUtil.getTyped((EmiStack)i.getEmiStacks().get(0)))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .map(i -> i.getIngredient(ingredientType))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .toList()
         : List.of();
   }
}
