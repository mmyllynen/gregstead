package dev.emi.emi.mixin;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dev.emi.emi.api.EmiApi;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.runtime.EmiSidebars;

@Mixin(ResultSlot.class)
public class CraftingResultSlotMixin {
	@Shadow @Final
	private CraftingContainer craftSlots;
	@Shadow @Final
	private Player player;
	
	@Shadow private int removeCount;
	
	@Inject(at = @At("HEAD"), method = "checkTakeAchievements(Lnet/minecraft/world/item/ItemStack;)V")
	private void onCrafted(ItemStack stack, CallbackInfo info) {
		Level world = player.level();
		if (world.isClientSide() && removeCount > 0) {
            dev.emi.emi.recipe.NativeCraftingHistory.recipe(craftSlots.asPositionedCraftInput().input(), stack, world)
                .ifPresent(id -> {
                    EmiRecipe recipe = EmiApi.getRecipeManager().getRecipe(id);
                    if (recipe != null) EmiSidebars.craft(recipe);
                });
        }
	}
}
