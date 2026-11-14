package io.github.mmyllynen.sleepwear;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;

public final class ColourRecipe extends CustomRecipe {
  public static final ColourRecipe INSTANCE = new ColourRecipe();

  public boolean matches(CraftingInput in, Level level) {
    return !assemble(in).isEmpty();
  }

  public ItemStack assemble(CraftingInput in) {
    int garment = -1, first = -1, second = -1;
    for (int i = 0; i < in.size(); i++) {
      ItemStack s = in.getItem(i);
      if (s.isEmpty()) continue;
      if (Sleepwear.isClothing(s)) {
        if (garment != -1) return ItemStack.EMPTY;
        garment = i;
      } else if (s.has(DataComponents.DYE)) {
        if (first < 0) first = i;
        else if (second < 0) second = i;
        else return ItemStack.EMPTY;
      } else return ItemStack.EMPTY;
    }
    if (garment < 0 || first < 0) return ItemStack.EMPTY;
    DyeColor a = in.getItem(first).get(DataComponents.DYE);
    if (second < 0) return Sleepwear.colour(in.getItem(garment), "solid", a, a);
    int w = in.width();
    DyeColor b = in.getItem(second).get(DataComponents.DYE);
    if (first == garment - w
        && second == garment + w
        && first % w == garment % w
        && second % w == garment % w) return Sleepwear.colour(in.getItem(garment), "split", a, b);
    if (first == garment - 1
        && second == garment + 1
        && first / w == garment / w
        && second / w == garment / w) return Sleepwear.colour(in.getItem(garment), "stripes", a, b);
    return ItemStack.EMPTY;
  }

  public RecipeSerializer<ColourRecipe> getSerializer() {
    return Sleepwear.COLOUR.get();
  }
}
