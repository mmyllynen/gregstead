package com.lowdragmc.lowdraglib.core.mixins;

import com.llamalad7.mixinextras.sugar.Local;
import com.lowdragmc.lowdraglib.client.model.NativeAtlasSources;
import com.lowdragmc.lowdraglib.client.model.custommodel.LDLMetadataSection;
import net.minecraft.client.renderer.texture.atlas.SpriteSource;
import net.minecraft.client.renderer.texture.atlas.SpriteSourceList;
import net.minecraft.client.renderer.texture.atlas.sources.SingleFile;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.metadata.MetadataSectionType;
import net.minecraft.server.packs.resources.ResourceManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Mixin(SpriteSourceList.class)
public class SpriteSourceListMixin {
    @Inject(method = "list(Lnet/minecraft/server/packs/resources/ResourceManager;Ljava/util/Set;)Ljava/util/List;",
            at = @At(value = "INVOKE", target = "Lcom/google/common/collect/ImmutableList;builder()Lcom/google/common/collect/ImmutableList$Builder;"), require = 1)
    private void ldlib$connectionSprites(ResourceManager resourceManager, Set<MetadataSectionType<?>> metadata,
            CallbackInfoReturnable<List<SpriteSource.Loader>> cir,
            @Local Map<Identifier, SpriteSource.DiscardableLoader> sprites, @Local SpriteSource.Output output) {
        for (Identifier spriteName : List.copyOf(sprites.keySet())) {
            // Use this reload's manager rather than the previous client resource cache.
            var resource = resourceManager.getResource(LDLMetadataSection.spriteToAbsolute(spriteName));
            if (resource.isEmpty()) continue;
            LDLMetadataSection data;
            try {
                data = resource.get().metadata().getSection(LDLMetadataSection.TYPE).orElse(LDLMetadataSection.MISSING);
            } catch (Exception ignored) {
                // Original malformed metadata policy: no extra connection sprite.
                continue;
            }
            if (data.connection != null) {
                new SingleFile(data.connection, Optional.empty()).run(resourceManager, output, metadata);
            }
        }
    }

    @Inject(method = "load", at = @At("RETURN"), require = 1)
    private static void ldlib$rendererSprites(ResourceManager resourceManager, Identifier location,
            CallbackInfoReturnable<SpriteSourceList> cir, @Local List<SpriteSource> sources) {
        Identifier atlas = location.withPath("textures/atlas/%s.png"::formatted);
        for (Identifier sprite : NativeAtlasSources.prepare(atlas)) {
            sources.add(new SingleFile(sprite, Optional.empty()));
        }
    }
}
