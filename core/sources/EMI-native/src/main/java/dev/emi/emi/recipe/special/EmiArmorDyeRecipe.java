package dev.emi.emi.recipe.special;

import java.util.List;
import java.util.Random;
import dev.emi.emi.api.recipe.EmiPatternCraftingRecipe;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.GeneratedSlotWidget;
import dev.emi.emi.api.widget.SlotWidget;
import dev.emi.emi.recipe.NativeDyeCrafting;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.DyeRecipe;

public class EmiArmorDyeRecipe extends EmiPatternCraftingRecipe {
    private final DyeRecipe recipe;
    private final ItemStack target;
    public EmiArmorDyeRecipe(DyeRecipe recipe,ItemStack target,Identifier id) {
        super(List.of(EmiIngredient.of(recipe.placementInfo().ingredients().get(1)),EmiStack.of(target)),
            EmiStack.of(NativeDyeCrafting.sample(recipe,target,new Random(0)).output()),id);
        this.recipe=recipe; this.target=target.copy();
    }
    @Override public SlotWidget getInputWidget(int slot,int x,int y) {
        return new GeneratedSlotWidget(random -> EmiStack.of(NativeDyeCrafting.sample(recipe,target,random).inputs().get(slot)),unique,x,y);
    }
    @Override public SlotWidget getOutputWidget(int x,int y) {
        return new GeneratedSlotWidget(random -> EmiStack.of(NativeDyeCrafting.sample(recipe,target,random).output()),unique,x,y);
    }
}
