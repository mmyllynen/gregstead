package com.firemerald.additionalplacements.util.neoforge;

import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.fml.loading.FMLPaths;

import java.nio.file.Path;

public class PlatformUtilsImpl {
    public static boolean isClient() {
        return FMLEnvironment.getDist().isClient();
    }

    public static boolean isDedicatedServer() {
        return FMLEnvironment.getDist().isDedicatedServer();
    }

    public static Path getConfigFolder() {
        return FMLPaths.CONFIGDIR.get();
    }
}
