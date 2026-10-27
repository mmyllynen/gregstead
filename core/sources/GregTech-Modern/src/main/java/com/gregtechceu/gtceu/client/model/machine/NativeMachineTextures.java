package com.gregtechceu.gtceu.client.model.machine;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import java.util.*;

/** Original machine casing texture remapping shared by native and legacy rendering during migration. */
public final class NativeMachineTextures {
    private NativeMachineTextures() {}
    public static <T> Map<String,T> remap(Set<String> replaceable, Map<String,T> overrides) {
        Map<String,T> result = new LinkedHashMap<>();
        overrides.forEach((key,value) -> {
            List<String> targets = replaceable.contains(key) ? List.of(key) : switch(key) {
                case "side", "top", "bottom" -> List.of("all");
                case "all" -> List.of("side", "top", "bottom");
                default -> List.of();
            };
            targets.forEach(target -> result.putIfAbsent(target,value));
        });
        return Collections.unmodifiableMap(result);
    }
    public interface Metadata {
        Set<String> replaceableTextures();
        Map<String,TextureAtlasSprite> textureOverrides();
    }
}
