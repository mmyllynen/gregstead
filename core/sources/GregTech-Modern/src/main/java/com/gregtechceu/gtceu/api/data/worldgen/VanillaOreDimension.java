package com.gregtechceu.gtceu.api.data.worldgen;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;

/** Gregstead's isolated Overworld uses the Overworld's ore-vein selection rules. */
public final class VanillaOreDimension {

    private static final String NAMESPACE = "gregstead_mushroom";
    public static final Identifier ID = Identifier.fromNamespaceAndPath(NAMESPACE, "vanilla");

    private VanillaOreDimension() {}

    public static boolean matches(ResourceKey<Level> dimension) {
        return dimension.identifier().equals(ID);
    }

    public static ResourceKey<Level> veinDimension(ResourceKey<Level> dimension) {
        return matches(dimension) ? Level.OVERWORLD : dimension;
    }

    public static Holder<Biome> veinBiome(ServerLevel level, Holder<Biome> biome) {
        if (!matches(level.dimension())) return biome;
        return biome.unwrapKey()
                .filter(key -> key.identifier().getNamespace().equals(NAMESPACE))
                .flatMap(key -> level.registryAccess().lookupOrThrow(Registries.BIOME).get(ResourceKey.create(
                        Registries.BIOME, Identifier.withDefaultNamespace(key.identifier().getPath()))))
                .<Holder<Biome>>map(holder -> holder)
                .orElse(biome);
    }
}
