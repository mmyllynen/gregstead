package com.gregtechceu.gtceu.data.tags;

import com.gregtechceu.gtceu.data.recipe.CustomTags;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;

import com.tterrag.registrate.providers.RegistrateTagsProvider;

public class EntityTypeTagLoader {

    public static void init(RegistrateTagsProvider.Impl<EntityType<?>> provider) {
        tag(provider, CustomTags.HEAT_IMMUNE)
                .add(EntityTypes.BLAZE, EntityTypes.MAGMA_CUBE)
                .add(EntityTypes.WITHER_SKELETON, EntityTypes.WITHER);
        tag(provider, CustomTags.CHEMICAL_IMMUNE)
                .add(EntityTypes.SKELETON, EntityTypes.STRAY, EntityTypes.BOGGED);
        tag(provider, CustomTags.IRON_GOLEMS).add(EntityTypes.IRON_GOLEM);
        tag(provider, CustomTags.SPIDERS)
                .add(EntityTypes.SPIDER, EntityTypes.CAVE_SPIDER);
    }
    private static NativeTagValues<EntityType<?>> tag(RegistrateTagsProvider.Impl<EntityType<?>> provider, net.minecraft.tags.TagKey<EntityType<?>> key) {
        return new NativeTagValues<>(net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE, provider.tag(key));
    }
}
