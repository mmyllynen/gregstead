package com.lowdragmc.lowdraglib.transfer.item;

import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;

/** Original GT chest loot component/damage and amount behavior. */
public final class LootStackRanges {
    private LootStackRanges() {}
    public static ItemStack apply(ItemStack prototype,ItemStack output,int min,int max,RandomSource random) {
        if(prototype.getDamageValue()!=0) output.setDamageValue(prototype.getDamageValue());
        if(!prototype.isComponentsPatchEmpty()) output.applyComponents(prototype.getComponentsPatch());
        output.setCount(min==max ? min : Math.min(min+random.nextInt(max-min+1),prototype.getMaxStackSize()));
        return output;
    }
}
