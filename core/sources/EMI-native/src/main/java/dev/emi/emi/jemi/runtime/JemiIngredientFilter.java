package dev.emi.emi.jemi.runtime;

import dev.emi.emi.api.EmiApi;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.jemi.JemiUtil;
import dev.emi.emi.screen.EmiScreenManager;
import dev.emi.emi.screen.EmiScreenManager.SidebarPanel;
import java.util.List;
import java.util.Optional;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.runtime.IIngredientFilter;

public class JemiIngredientFilter implements IIngredientFilter {
   public void setFilterText(String filterText) {
      EmiApi.setSearchText(filterText);
   }

   public String getFilterText() {
      return EmiApi.getSearchText();
   }

   public <T> List<T> getFilteredIngredients(IIngredientType<T> ingredientType) {
      SidebarPanel search = EmiScreenManager.getSearchPanel();
      return search != null && search.space != null
         ? search.space
            .getStacks()
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
