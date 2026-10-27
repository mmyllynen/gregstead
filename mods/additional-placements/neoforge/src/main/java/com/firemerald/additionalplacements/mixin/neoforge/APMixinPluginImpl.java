package com.firemerald.additionalplacements.mixin.neoforge;

import java.util.List;

public class APMixinPluginImpl {
    public static List<String> getPlatformMixins() {
        return List.of(
                "neoforge.MixinMappedRegistry"
        );
    }
}
