package com.gregtechceu.gtceu.common.fluid.potion;
import com.gregtechceu.gtceu.api.misc.forge.FilteredFluidHandlerItemStackSimple;
import com.gregtechceu.gtceu.common.data.item.GTDataComponents;
import com.gregtechceu.gtceu.data.recipe.CustomTags;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.*;
import net.minecraft.world.item.alchemy.PotionContents;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
public class BottleItemFluidHandler extends FilteredFluidHandlerItemStackSimple {
    public BottleItemFluidHandler(ItemAccess access){super(GTDataComponents.FLUID_CONTENT,access,PotionFluidHelper.BOTTLE_AMOUNT,s->s.is(CustomTags.POTION_FLUIDS));}
    @Override protected ItemResource update(ItemResource container,int index,FluidResource resource,int amount){
        if(amount!=capacity || resource.isEmpty())return ItemResource.EMPTY;
        var potion=new ItemStack(Items.POTION);
        potion.set(DataComponents.POTION_CONTENTS,resource.toStack(amount).getOrDefault(DataComponents.POTION_CONTENTS,PotionContents.EMPTY));
        return ItemResource.of(potion);
    }
}
