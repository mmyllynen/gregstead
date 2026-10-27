package com.gregtechceu.gtceu.api.data.worldgen.generator.veins;
import com.mojang.datafixers.util.Either;
import java.util.List;
import java.util.Objects;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
/** All geode layers share the original seed-zero preview stream; materials remain unsampled. */
public final class NativeGeodePreview {
    private NativeGeodePreview() {}
    public static <M> List<Either<BlockState,M>> sample(LevelAccessor level,List<Either<BlockStateProvider,M>> layers) {
        Objects.requireNonNull(level,"Geode preview needs a real world");
        var random=new LegacyRandomSource(0);
        return layers.stream().map(layer->layer.mapLeft(provider->provider.getState(level,random,BlockPos.ZERO))).toList();
    }
}
