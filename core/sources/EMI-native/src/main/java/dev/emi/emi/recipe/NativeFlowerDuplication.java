package dev.emi.emi.recipe;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.TallFlowerBlock;
import net.minecraft.world.level.block.BonemealSource;

/** Bonemeal eligibility evaluated with real native arguments, including interaction source. */
public final class NativeFlowerDuplication {
    private NativeFlowerDuplication() {}
    public static boolean canDuplicate(TallFlowerBlock flower, Level level, BlockPos position) {
        var state = flower.defaultBlockState();
        return flower.isValidBonemealTarget(level, position, state, BonemealSource.INTERACTION)
            && flower.isBonemealSuccess(level, RandomSource.create(0), position, state, BonemealSource.INTERACTION);
    }
}
