package dev.emi.emi.jemi.runtime;

import dev.emi.emi.api.EmiApi;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.jemi.JemiUtil;
import dev.emi.emi.screen.RecipeScreen;
import java.util.List;
import java.util.Optional;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.types.IRecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.runtime.IRecipesGui;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

public class JemiRecipesGui implements IRecipesGui {
   public void show(List<IFocus<?>> focuses) {
      for (IFocus<?> focus : focuses) {
         EmiStack stack = JemiUtil.getStack(focus.getTypedValue());
         if (!stack.isEmpty()) {
            RecipeIngredientRole role = focus.getRole();
            if (role == RecipeIngredientRole.OUTPUT) {
               EmiApi.displayRecipes(stack);
            } else {
               EmiApi.displayUses(stack);
            }
         }
      }
   }

   public void showTypes(List<IRecipeType<?>> recipeTypes) {
      for (IRecipeType<?> type : recipeTypes) {
         for (EmiRecipeCategory category : EmiApi.getRecipeManager().getCategories()) {
            if (category.getId().equals(type.getUid())) {
               EmiApi.displayRecipeCategory(category);
            }
         }
      }
   }

   public <T> void showRecipes(IRecipeCategory<T> recipeCategory, List<T> recipes, List<IFocus<?>> focuses) {
      this.showTypes(List.of(recipeCategory.getRecipeType()));
   }

   public <T> Optional<T> getIngredientUnderMouse(IIngredientType<T> ingredientType) {
      Minecraft client = Minecraft.getInstance();
      if (client.gui.screen() instanceof RecipeScreen screen) {
         EmiIngredient stack = screen.getHoveredStack();
         if (!stack.isEmpty()) {
            Optional<ITypedIngredient<?>> opt = JemiUtil.getTyped((EmiStack)stack.getEmiStacks().get(0));
            if (opt.isPresent()) {
               return opt.get().getIngredient(ingredientType);
            }
         }
      }

      return Optional.empty();
   }

   public Optional<Screen> getParentScreen() {
      return Optional.empty();
   }
}
