package com.gregtechceu.gtceu.client.renderer.machine.impl;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
/** Original boiler layer selection works along the controller's rotated/flipped down axis. */
public final class NativeBoilerPartState {
    private NativeBoilerPartState() {}
    public static BlockState select(BlockPos controller,BlockPos part,Direction down,boolean working,
                                     BlockState idle,BlockState active,BlockState casing) {
        return controller.relative(down).get(down.getAxis()) == part.get(down.getAxis())
                ? working ? active : idle : casing;
    }
}
