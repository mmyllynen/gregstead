package dev.emi.emi.jemi.runtime;

import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.stack.EmiStackInteraction;
import dev.emi.emi.config.SidebarType;
import dev.emi.emi.jemi.JemiUtil;
import dev.emi.emi.screen.EmiScreenManager;
import dev.emi.emi.screen.EmiScreenManager.SidebarEmiStackInteraction;
import java.util.Optional;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.runtime.IBookmarkOverlay;
import org.jetbrains.annotations.Nullable;

public class JemiBookmarkOverlay implements IBookmarkOverlay {
   public Optional<ITypedIngredient<?>> getIngredientUnderMouse() {
      EmiStackInteraction stack = EmiScreenManager.getHoveredStack(EmiScreenManager.lastMouseX, EmiScreenManager.lastMouseY, false);
      return stack instanceof SidebarEmiStackInteraction sesi && sesi.getType() == SidebarType.FAVORITES && sesi.getStack().getEmiStacks().size() == 1
         ? JemiUtil.getTyped((EmiStack)stack.getStack().getEmiStacks().get(0))
         : Optional.empty();
   }

   @Nullable
   public <T> T getIngredientUnderMouse(IIngredientType<T> ingredientType) {
      Optional<ITypedIngredient<?>> opt = this.getIngredientUnderMouse();
      return opt.isPresent() ? opt.get().getIngredient(ingredientType).orElseGet(() -> null) : null;
   }
}
