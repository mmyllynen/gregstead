package com.gregtechceu.gtceu.core.mixins;

import com.gregtechceu.gtceu.api.recipe.FullCraftingGrid;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import java.util.List;

@Mixin(CraftingInput.class)
public abstract class CraftingInputMixin implements FullCraftingGrid {
    @Unique private int gtceu$gridWidth, gtceu$gridHeight, gtceu$gridLeft, gtceu$gridTop;
    public void gtceu$setGrid(int width,int height,int left,int top) {
        gtceu$gridWidth=width;gtceu$gridHeight=height;gtceu$gridLeft=left;gtceu$gridTop=top;
    }
    public int gtceu$width(){return gtceu$gridWidth;}
    public int gtceu$height(){return gtceu$gridHeight;}
    public int gtceu$left(){return gtceu$gridLeft;}
    public int gtceu$top(){return gtceu$gridTop;}
    @Inject(method="ofPositioned",at=@At("RETURN"))
    private static void gtceu$retainGrid(int width,int height,List<ItemStack> items,
            CallbackInfoReturnable<CraftingInput.Positioned> callback) {
        var positioned=callback.getReturnValue();
        if(positioned.input()!=CraftingInput.EMPTY)
            ((FullCraftingGrid)(Object)positioned.input()).gtceu$setGrid(width,height,positioned.left(),positioned.top());
    }
}
