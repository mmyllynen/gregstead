package com.gregtechceu.gtceu.api.data.worldgen;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
/** Restored original vanilla Nether carver target group, retained in the mod's native tag resources. */
public final class NativeNetherOreTargets {
    private NativeNetherOreTargets() {}
    public static final TagKey<Block> TAG=TagKey.create(Registries.BLOCK,Identifier.withDefaultNamespace("nether_carver_replaceables"));
}
