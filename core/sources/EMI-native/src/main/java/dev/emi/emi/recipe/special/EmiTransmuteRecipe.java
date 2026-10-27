package dev.emi.emi.recipe.special;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import dev.emi.emi.api.recipe.EmiPatternCraftingRecipe;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.GeneratedSlotWidget;
import dev.emi.emi.api.widget.SlotWidget;
import dev.emi.emi.recipe.NativeTransmuteCrafting;
import net.minecraft.resources.Identifier;

public class EmiTransmuteRecipe extends EmiPatternCraftingRecipe {
    private final NativeTransmuteCrafting.Plan plan;
    public EmiTransmuteRecipe(NativeTransmuteCrafting.Plan plan,Identifier id) {
        super(inputs(plan),EmiStack.of(NativeTransmuteCrafting.sample(plan,plan.choices().getFirst()).output()),id);
        this.plan=plan;
    }
    private static List<EmiIngredient> inputs(NativeTransmuteCrafting.Plan plan) {
        var input=new ArrayList<EmiIngredient>();input.add(EmiIngredient.of(plan.source()));
        int count=plan.choices().stream().mapToInt(NativeTransmuteCrafting.Choice::count).min().orElseThrow();
        for(int i=0;i<count;i++) input.add(EmiIngredient.of(plan.material()));
        return List.copyOf(input);
    }
    public SlotWidget getInputWidget(int slot,int x,int y) {
        return new GeneratedSlotWidget(random -> EmiStack.of(NativeTransmuteCrafting.sample(plan,random).inputs().get(slot)),unique,x,y);
    }
    public SlotWidget getOutputWidget(int x,int y) {
        return new GeneratedSlotWidget(random -> EmiStack.of(NativeTransmuteCrafting.sample(plan,random).output()),unique,x,y);
    }
}
