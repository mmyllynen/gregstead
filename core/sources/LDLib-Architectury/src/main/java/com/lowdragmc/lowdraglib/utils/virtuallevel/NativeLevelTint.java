package com.lowdragmc.lowdraglib.utils.virtuallevel;
import net.minecraft.core.BlockPos;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ColorResolver;
/** Native tint views retain client blending; non-render levels resolve their own biome instead of a global client level. */
public final class NativeLevelTint {
    private NativeLevelTint() {}
    public static int color(Level world,BlockPos pos,ColorResolver resolver) {
        return world instanceof BlockAndTintGetter tint?tint.getBlockTint(pos,resolver)
                :resolver.getColor(world.getBiome(pos).value(),pos.getX(),pos.getZ());
    }
}
