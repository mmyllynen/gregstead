package dev.emi.emi.mixin.accessor;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import net.minecraft.world.item.crafting.BrewingRecipe;
import net.minecraft.world.item.crafting.PotionIngredient;
import net.minecraft.world.item.ItemStackTemplate;

/** Actual native data-driven brewing fields, replacing the removed global mix registry. */
@Mixin(BrewingRecipe.class)
public interface BrewingRecipeRegistryAccessor {
    @Accessor("input") PotionIngredient getInput();
    @Accessor("reagent") PotionIngredient getReagent();
    @Accessor("output") ItemStackTemplate getOutput();
}
