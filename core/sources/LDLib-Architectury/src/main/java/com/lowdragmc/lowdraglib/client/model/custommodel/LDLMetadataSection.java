package com.lowdragmc.lowdraglib.client.model.custommodel;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import com.lowdragmc.lowdraglib.client.model.ModelFactory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.metadata.MetadataSectionType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LDLMetadataSection {
    public static final String SECTION_NAME = "ldlib";
    private static final Map<Identifier, LDLMetadataSection> METADATA_CACHE = new ConcurrentHashMap<>();
    public static final LDLMetadataSection MISSING = new LDLMetadataSection(false, null);

    public final boolean emissive;
    public final Identifier connection;

    public LDLMetadataSection(boolean emissive, Identifier connection) {
        this.emissive = emissive;
        this.connection = connection;
    }

    public static void clearCache() {
        METADATA_CACHE.clear();
    }

    public boolean isMissing() {
        return this == MISSING;
    }

    @Nonnull
    public static LDLMetadataSection getMetadata(Identifier res) {
        if (METADATA_CACHE.containsKey(res)) {
            return METADATA_CACHE.get(res);
        }
        LDLMetadataSection ret = MISSING;
        var resourceOptional = Minecraft.getInstance().getResourceManager().getResource(res);
        if (resourceOptional.isPresent()) {
            var resource = resourceOptional.get();
            try {
                ret = resource.metadata().getSection(TYPE).orElse(MISSING);
            } catch (Exception ignored) {
            }
        }
        METADATA_CACHE.put(res, ret);
        return ret;
    }

    @Nonnull
    public static LDLMetadataSection getMetadata(TextureAtlasSprite sprite) {
        return getMetadata(spriteToAbsolute(sprite.contents().name()));
    }

    public static boolean isEmissive(TextureAtlasSprite sprite) {
        LDLMetadataSection ret = getMetadata(spriteToAbsolute(sprite.contents().name()));
        return ret.emissive;
    }

    @Nullable
    public static TextureAtlasSprite getConnection(TextureAtlasSprite sprite) {
        LDLMetadataSection ret = getMetadata(spriteToAbsolute(sprite.contents().name()));
        return ret.connection == null ? null : ModelFactory.getBlockSprite(ret.connection);
    }

    public static Identifier spriteToAbsolute(Identifier sprite) {
        if (!sprite.getPath().startsWith("textures/")) {
            sprite = Identifier.fromNamespaceAndPath(sprite.getNamespace(), "textures/" + sprite.getPath());
        }
        if (!sprite.getPath().endsWith(".png")) {
            sprite = Identifier.fromNamespaceAndPath(sprite.getNamespace(), sprite.getPath() + ".png");
        }
        return sprite;
    }

    public static final Codec<LDLMetadataSection> CODEC = Codec.PASSTHROUGH.comapFlatMap(input -> {
        JsonElement json = input.convert(JsonOps.INSTANCE).getValue();
        if (!json.isJsonObject()) {
            return DataResult.error(() -> "LDLib texture metadata must be an object");
        }
        try {
            return DataResult.success(fromJson(json.getAsJsonObject()));
        } catch (net.minecraft.IdentifierException exception) {
            return DataResult.error(exception::getMessage);
        }
    }, value -> {
        JsonObject json = new JsonObject();
        json.addProperty("emissive", value.emissive);
        if (value.connection != null) json.addProperty("connection", value.connection.toString());
        return new Dynamic<>(JsonOps.INSTANCE, json);
    });
    public static final MetadataSectionType<LDLMetadataSection> TYPE =
            new MetadataSectionType<>(SECTION_NAME, CODEC);

    private static LDLMetadataSection fromJson(JsonObject json) {
        boolean emissive = false;
        Identifier connection = null;
        JsonElement glow = json.get("emissive");
        if (glow != null && glow.isJsonPrimitive() && glow.getAsJsonPrimitive().isBoolean()) {
            emissive = glow.getAsBoolean();
        }
        JsonElement link = json.get("connection");
        if (link != null && link.isJsonPrimitive() && link.getAsJsonPrimitive().isString()) {
            connection = Identifier.parse(link.getAsString());
        }
        return new LDLMetadataSection(emissive, connection);
    }
}
