package fi.gregstead.mushroom;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeResolver;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;
import net.minecraft.world.level.biome.MultiNoiseBiomeSourceParameterList;

import java.util.List;
import java.util.stream.Stream;

import com.mojang.datafixers.util.Pair;

final class VanillaBiomeSource extends BiomeSource {
    static final MapCodec<VanillaBiomeSource> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            RegistryOps.<Biome, VanillaBiomeSource>retrieveGetter(Registries.BIOME)
    ).apply(instance, VanillaBiomeSource::new));
    private final MultiNoiseBiomeSource vanilla;

    private VanillaBiomeSource(HolderGetter<Biome> biomes) {
        List<Pair<Climate.ParameterPoint, Holder<Biome>>> entries = MultiNoiseBiomeSourceParameterList.knownPresets()
                .get(MultiNoiseBiomeSourceParameterList.Preset.OVERWORLD).values().stream()
                .map(entry -> entry.<Holder<Biome>>mapSecond(key -> biomes.getOrThrow(
                        ResourceKey.create(Registries.BIOME, MushroomDimension.id(key.identifier().getPath())))))
                .toList();
        vanilla = MultiNoiseBiomeSource.createFromList(new Climate.ParameterList<>(entries));
    }

    @Override
    protected MapCodec<? extends BiomeSource> codec() {
        return CODEC;
    }

    @Override
    protected Stream<Holder<Biome>> collectPossibleBiomes() {
        return vanilla.possibleBiomes().stream();
    }

    @Override
    public BiomeResolver createResolver(Climate.Sampler sampler) {
        return vanilla.createResolver(sampler);
    }

    @Override
    public BiomeResolver createResolverForChunk(Climate.Sampler sampler, int x, int y, int z, int width, int height, int depth) {
        return vanilla.createResolverForChunk(sampler, x, y, z, width, height, depth);
    }
}
