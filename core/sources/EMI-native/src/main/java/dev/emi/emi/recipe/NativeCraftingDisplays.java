package dev.emi.emi.recipe;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.display.*;
import net.minecraft.world.level.Level;

/** Native crafting display geometry maps to the actual three-column crafting table. */
public final class NativeCraftingDisplays {
    private NativeCraftingDisplays() {}
    public record View(List<List<ItemStack>> inputs,ItemStack output,boolean shapeless) {}
    public static List<View> views(CraftingRecipe recipe,Level level) {
        var result=new ArrayList<View>();
        for(var display:recipe.display()) result.addAll(views(display,level));
        return List.copyOf(result);
    }
    public static List<View> views(RecipeDisplay display,Level level) {
        var context=SlotDisplayContext.fromLevel(level);
        var slots=new ArrayList<List<ItemStack>>();
        for(int i=0;i<9;i++) slots.add(List.of());
        boolean shapeless;
        if(display instanceof ShapedCraftingRecipeDisplay shaped) {
            if(shaped.width()<1 || shaped.height()<1 || shaped.width()>3 || shaped.height()>3) return List.of();
            shapeless=false;
            for(int y=0;y<shaped.height();y++) for(int x=0;x<shaped.width();x++)
                slots.set(x+y*3,resolve(shaped.ingredients().get(x+y*shaped.width()),context));
        } else if(display instanceof ShapelessCraftingRecipeDisplay unshaped) {
            if(unshaped.ingredients().isEmpty() || unshaped.ingredients().size()>9) return List.of();
            shapeless=true;
            for(int i=0;i<unshaped.ingredients().size();i++) slots.set(i,resolve(unshaped.ingredients().get(i),context));
        } else return List.of();
        var input=List.copyOf(slots);
        return display.result().resolveForStacks(context).stream().map(output -> new View(input,output.copy(),shapeless)).toList();
    }
    private static List<ItemStack> resolve(SlotDisplay display,net.minecraft.util.context.ContextMap context) {
        var values=display.resolveForStacks(context).stream().map(ItemStack::copy).toList();
        if(values.isEmpty() && display!=SlotDisplay.Empty.INSTANCE)
            throw new IllegalArgumentException("Native crafting display has no representative ingredient: "+display);
        return values;
    }
}
