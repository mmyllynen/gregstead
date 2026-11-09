package fi.gregstead.ae2fixes.mixin;

import appeng.menu.slot.CraftingTermSlot;
import java.util.List;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(value = CraftingTermSlot.class, remap = false)
public abstract class CraftingSubstitutionMixin {
    @ModifyArgs(method = "craftItem", at = @At(value = "INVOKE",
        target = "Lappeng/menu/slot/CraftingTermSlot;extractItemsByRecipe(Lappeng/api/networking/energy/IEnergySource;Lappeng/api/networking/security/IActionSource;Lappeng/api/storage/MEStorage;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/item/crafting/Recipe;Lnet/minecraft/world/item/ItemStack;IILjava/util/List;Lnet/minecraft/world/item/ItemStack;ILappeng/api/stacks/KeyCounter;Lappeng/util/prioritylist/IPartitionList;)Lnet/minecraft/world/item/ItemStack;"), require = 1)
    private void gregstead$preserveFullPatternDimensions(Args args) {
        if (((List<?>) args.get(8)).size() != 9) throw new IllegalStateException("Unsupported AE2 crafting pattern size");
        args.set(6, 3);
        args.set(7, 3);
    }
}
