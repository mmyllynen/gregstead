package fi.gregstead.ae2fixes.mixin;

import appeng.menu.slot.AppEngCraftingSlot;
import com.llamalad7.mixinextras.sugar.Local;
import fi.gregstead.ae2fixes.CraftingCoordinates;
import net.minecraft.world.item.crafting.CraftingInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/** Local indices target the exact checksum-locked AE2 26.3.1-alpha implementation. */
@Mixin(value = AppEngCraftingSlot.class, remap = false)
public abstract class CraftingRemainderMixin {
    @ModifyVariable(method = "onTake", at = @At("STORE"), index = 10, require = 1)
    private int gregstead$positionRemainder(int original,
        @Local(index = 5) CraftingInput.Positioned positioned,
        @Local(index = 7) int y, @Local(index = 8) int x) {
        return CraftingCoordinates.remainderIndex(x, y, positioned.left(), positioned.top(),
            positioned.input().width(), positioned.input().height());
    }
}
