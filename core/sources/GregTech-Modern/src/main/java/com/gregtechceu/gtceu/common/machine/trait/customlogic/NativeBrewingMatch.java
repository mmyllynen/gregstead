package com.gregtechceu.gtceu.common.machine.trait.customlogic;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.component.DataComponents;
import java.util.*;
public final class NativeBrewingMatch {
    private NativeBrewingMatch() {}
    public static ItemStack output(BrewingRecipe recipe,ItemStack input,ItemStack reagent) {
        var brewing=new BrewingInput(input,reagent);return recipe.matches(brewing)?recipe.assemble(brewing):ItemStack.EMPTY;
    }
    public static List<ItemStack> choices(PotionIngredient ingredient,RegistryAccess registries) {
        var choices=new ArrayList<ItemStack>();
        for(var base:com.lowdragmc.lowdraglib.transfer.recipe.IngredientStacks.getItems(ingredient.ingredient())) {
            if(ingredient.test(base))choices.add(base.copy());
            if(ingredient.potions().isEmpty())continue;
            registries.lookupOrThrow(Registries.POTION).listElements().forEach(potion->{
                var stack=base.copy();stack.set(DataComponents.POTION_CONTENTS,new PotionContents(potion));
                if(ingredient.test(stack) && choices.stream().noneMatch(other->ItemStack.isSameItemSameComponents(other,stack)))choices.add(stack);
            });
        }
        return List.copyOf(choices);
    }
}
