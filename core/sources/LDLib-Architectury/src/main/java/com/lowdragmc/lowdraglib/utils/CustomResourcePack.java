package com.lowdragmc.lowdraglib.utils;

import com.lowdragmc.lowdraglib.transfer.annotation.MethodsReturnNonnullByDefault;
import net.minecraft.SharedConstants;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.PathPackResources;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.resources.IoSupplier;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.io.*;
import java.util.Optional;

/**
 * @author KilaBash
 * @date 2022/05/13
 * @implNote CustomResourcePack
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class CustomResourcePack extends PathPackResources {
    private final PackType type;
    private final String namespace;

    public CustomResourcePack(File location, String namespace, PackType type) {
        super(new PackLocationInfo(namespace, Component.literal(namespace), PackSource.DEFAULT, Optional.empty()), location.toPath());
        new File(location, namespace).mkdirs();
        this.namespace = namespace;
        this.type = type;
    }

    @Nullable
    @Override
    public IoSupplier<InputStream> getRootResource(String... pathSegments) {
        String fileName = String.join("/", pathSegments);
        if ("pack.mcmeta".equals(fileName)) {
            var format = SharedConstants.getCurrentVersion().packVersion(type);
            var metadata = new net.minecraft.server.packs.metadata.pack.PackMetadataSection(
                    Component.translatableWithFallback("Generated resources for " + namespace, "Mod resources."),
                    new net.minecraft.util.InclusiveRange<>(format));
            var section = net.minecraft.server.packs.metadata.pack.PackMetadataSection.forPackType(type);
            var json = new com.google.gson.JsonObject();
            json.add(section.name(), section.codec().encodeStart(com.mojang.serialization.JsonOps.INSTANCE, metadata).getOrThrow());
            return () -> new ByteArrayInputStream(json.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8));
        }
        return super.getRootResource(pathSegments);
    }

}
