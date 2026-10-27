package com.gregtechceu.gtceu.api.gui.editor;

import net.minecraft.resources.Identifier;
import java.nio.file.*;
import java.io.IOException;

/** Editor writes the exact asset path consumed by machine and recipe UI loaders. */
public final class NativeUIProjectPaths {
    private NativeUIProjectPaths() {}
    public static Path target(Path assets, Identifier id, String category, String suffix) throws IOException {
        var folder = assets.toAbsolutePath().normalize().resolve(id.getNamespace()).resolve("ui").resolve(category).normalize();
        var target = folder.resolve(id.getPath() + "." + suffix).normalize();
        if (!target.startsWith(folder)) throw new IOException("UI identifier escapes its asset folder: " + id);
        Files.createDirectories(target.getParent());
        return target;
    }
}
