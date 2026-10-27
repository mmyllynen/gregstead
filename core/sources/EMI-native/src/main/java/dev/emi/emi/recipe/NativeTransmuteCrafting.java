package dev.emi.emi.recipe;

import com.mojang.serialization.JsonOps;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import net.minecraft.advancements.predicates.MinMaxBounds;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.TransmuteRecipe;
import net.minecraft.world.level.Level;

/** Transmute examples retain the actual count bounds and are accepted by the native recipe. */
public final class NativeTransmuteCrafting {
    private NativeTransmuteCrafting() {}
    public record Choice(ItemStack source,ItemStack material,int count) {}
    public record Plan(TransmuteRecipe recipe,Ingredient source,Ingredient material,List<Choice> choices,Level level) {}
    public record Sample(List<ItemStack> inputs,ItemStack output) {}
    public static Plan plan(TransmuteRecipe recipe,Level level) {
        var ingredients=recipe.placementInfo().ingredients();
        Ingredient source=ingredients.getFirst(),material=ingredients.get(1);
        var ops=RegistryOps.create(JsonOps.INSTANCE,level.registryAccess());
        var data=TransmuteRecipe.MAP_CODEC.codec().encodeStart(ops,recipe).getOrThrow().getAsJsonObject();
        var bounds=data.has("material_count") ? MinMaxBounds.Ints.CODEC.parse(ops,data.get("material_count")).getOrThrow()
            : TransmuteRecipe.DEFAULT_MATERIAL_COUNT;
        var choices=new ArrayList<Choice>();
        for(var target:NativeRecipeData.items(source)) for(var added:NativeRecipeData.items(material)) {
            for(int count=1;count<=8;count++) if(bounds.matches(count)) {
                var choice=new Choice(target.copyWithCount(1),added.copyWithCount(1),count);
                var input=CraftingInput.of(3,3,input(choice));
                if(recipe.matches(input,level)) choices.add(choice);
            }
        }
        if(choices.isEmpty()) throw new IllegalArgumentException("Transmute recipe has no accepted representative inputs");
        return new Plan(recipe,source,material,List.copyOf(choices),level);
    }
    private static List<ItemStack> input(Choice choice) {
        var slots=new ArrayList<ItemStack>();slots.add(choice.source().copyWithCount(1));
        for(int i=0;i<choice.count();i++) slots.add(choice.material().copyWithCount(1));
        while(slots.size()<9) slots.add(ItemStack.EMPTY);
        return List.copyOf(slots);
    }
    public static Sample sample(Plan plan,Random random) { return sample(plan,plan.choices().get(random.nextInt(plan.choices().size()))); }
    public static Sample sample(Plan plan,Choice choice) {
        var input=input(choice);
        var crafting=CraftingInput.of(3,3,input);
        if(!plan.recipe().matches(crafting,plan.level())) throw new IllegalArgumentException("Transmute sample no longer matches native recipe");
        return new Sample(input,plan.recipe().assemble(crafting));
    }
}
