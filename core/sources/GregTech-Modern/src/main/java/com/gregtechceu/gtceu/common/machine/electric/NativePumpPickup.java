package com.gregtechceu.gtceu.common.machine.electric;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.fluids.*;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.fluid.*;
/** Source inspection is read-only; native pickup owns the actual block mutation and tank transaction. */
public final class NativePumpPickup {
    private NativePumpPickup() {}
    public static FluidStack candidate(Level level,BlockPos pos) {
        var state=level.getBlockState(pos);var fluid=state.getFluidState();
        return state.getBlock() instanceof LiquidBlock && fluid.isSource() && fluid.getType().getBucket()!=Items.AIR
                ?new FluidStack(fluid.getType(),FluidType.BUCKET_VOLUME):FluidStack.EMPTY;
    }
    public static FluidStack pickup(Level level,BlockPos pos,ResourceHandler<FluidResource> destination) {
        return FluidUtil.tryPickupFluid(destination,null,level,pos,null);
    }
}
