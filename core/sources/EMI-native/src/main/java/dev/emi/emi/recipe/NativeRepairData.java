package dev.emi.emi.recipe;

import java.util.Optional;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

/** Repair materials use the same stack component as native anvil repair validation. */
public final class NativeRepairData {
    private NativeRepairData() {}
    public static Optional<Ingredient> material(ItemStack stack) {
        if(stack.getOrDefault(DataComponents.MAX_DAMAGE,0)<=0) return Optional.empty();
        var repair=stack.get(DataComponents.REPAIRABLE);
        return repair==null || repair.items().size()==0 ? Optional.empty() : Optional.of(Ingredient.of(repair.items()));
    }
}
