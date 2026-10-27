package com.lowdragmc.lowdraglib.transfer.fluid;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.fluids.FluidStack;
import java.util.Optional;
/** Direct component access for tanks whose operations keep the underlying item unchanged. */
public final class ComponentFluidAccess {
    private ComponentFluidAccess() {}
    public static Optional<IFluidHandler> handler(ItemStack stack) {
        if(stack.isEmpty())return Optional.empty();
        var nativeHandler=ItemAccess.forStack(stack).getCapability(Capabilities.Fluid.ITEM);
        return nativeHandler==null?Optional.empty():Optional.of(ResourceFluidHandler.of(nativeHandler));
    }
    public static Optional<FluidStack> contained(ItemStack stack) {
        return handler(stack).map(handler->handler.drain(Integer.MAX_VALUE,IFluidHandler.FluidAction.SIMULATE)).filter(fluid->!fluid.isEmpty());
    }
}
