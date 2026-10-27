package com.gregtechceu.gtceu.common.fluid.potion;
import net.minecraft.world.item.*;
import net.neoforged.neoforge.transfer.ItemAccessResourceHandler;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
/** Native potion extraction swaps the complete container into a glass bottle atomically. */
public class PotionItemFluidHandler extends ItemAccessResourceHandler<FluidResource> {
    private final Item potion;
    public PotionItemFluidHandler(ItemAccess access){super(access,1);potion=access.getResource().getItem();}
    protected FluidResource getResourceFrom(ItemResource item,int index){return item.is(potion)?FluidResource.of(PotionFluidHelper.getFluidFromPotionItem(item.toStack(1),PotionFluidHelper.BOTTLE_AMOUNT)):FluidResource.EMPTY;}
    protected int getAmountFrom(ItemResource item,int index){return item.is(potion)?PotionFluidHelper.BOTTLE_AMOUNT:0;}
    protected int getCapacity(int index,FluidResource resource){return PotionFluidHelper.BOTTLE_AMOUNT;}
    public boolean isValid(int index,FluidResource resource){return false;}
    protected ItemResource update(ItemResource item,int index,FluidResource resource,int amount){return amount==0?ItemResource.of(Items.GLASS_BOTTLE):ItemResource.EMPTY;}
}
