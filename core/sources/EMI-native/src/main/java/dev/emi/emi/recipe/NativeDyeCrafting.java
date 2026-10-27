package dev.emi.emi.recipe;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.DyeRecipe;

/** Dye previews are assembled by the actual native recipe, preserving copied target components. */
public final class NativeDyeCrafting {
    private NativeDyeCrafting() {}
    public record Sample(List<ItemStack> inputs,ItemStack output) {}
    public static Sample sample(DyeRecipe recipe,ItemStack target,Random random) {
        var ingredients=recipe.placementInfo().ingredients();
        if(!ingredients.getFirst().test(target)) throw new IllegalArgumentException("Target is not accepted by dye recipe");
        var dyes=Arrays.stream(NativeRecipeData.items(ingredients.get(1)))
            .filter(stack -> stack.has(DataComponents.DYE) && !ingredients.getFirst().test(stack)).toList();
        if(dyes.isEmpty()) throw new IllegalArgumentException("Dye recipe has no representative dyes with a DYE component");
        var input=new ArrayList<ItemStack>();
        input.add(target.copyWithCount(1));
        int count=1+random.nextInt(8);
        for(int i=0;i<count;i++) input.add(dyes.get(random.nextInt(dyes.size())).copyWithCount(1));
        while(input.size()<9) input.add(ItemStack.EMPTY);
        return new Sample(List.copyOf(input),recipe.assemble(CraftingInput.of(3,3,input)));
    }
}
