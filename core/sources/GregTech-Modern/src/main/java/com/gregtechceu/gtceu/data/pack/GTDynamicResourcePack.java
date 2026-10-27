package com.gregtechceu.gtceu.data.pack;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.addon.AddonFinder;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.integration.kjs.GregTechKubeJSPlugin;

import net.minecraft.SharedConstants;
import net.minecraft.client.renderer.texture.atlas.SpriteSource;
import net.minecraft.client.renderer.texture.atlas.SpriteSources;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.client.data.models.blockstates.BlockModelDefinitionGenerator;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelDispatcher;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.MetadataSectionType;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
import net.minecraft.server.packs.resources.IoSupplier;

import com.google.common.collect.Sets;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import org.jetbrains.annotations.Nullable;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import static com.gregtechceu.gtceu.data.pack.GTDynamicDataPack.writeJson;

public class GTDynamicResourcePack implements PackResources {

    protected static final ObjectSet<String> CLIENT_DOMAINS = new ObjectOpenHashSet<>();
    protected static final GTDynamicPackContents CONTENTS = new GTDynamicPackContents();

    private static final FileToIdConverter ATLAS_ID_CONVERTER = FileToIdConverter.json("atlases");
    public static final FileToIdConverter TEXTURE_ID_CONVERTER = SpriteSource.TEXTURE_ID_CONVERTER;
    public static final FileToIdConverter BLOCKSTATE_ID_CONVERTER = FileToIdConverter.json("blockstates");
    public static final FileToIdConverter MODEL_ID_CONVERTER = FileToIdConverter.json("models");

    private final PackLocationInfo info;

    static {
        CLIENT_DOMAINS.addAll(Sets.newHashSet(GTCEu.MOD_ID, "minecraft", "neoforge", "c", "kubejs"));
    }

    public GTDynamicResourcePack(PackLocationInfo info) {
        this(info, AddonFinder.getAddons().keySet());
    }

    public GTDynamicResourcePack(PackLocationInfo info, Collection<String> domains) {
        this.info = info;
        var categoryLabels = com.gregtechceu.gtceu.utils.input.SyncedKeyMapping.categoryLabels();
        if (!categoryLabels.isEmpty()) {
            com.google.gson.JsonObject language = new com.google.gson.JsonObject();
            categoryLabels.forEach(language::addProperty);
            addResource(GTCEu.id("lang/en_us.json"), language);
        }
        CLIENT_DOMAINS.addAll(domains);

        if (GTCEu.Mods.isKubeJSLoaded()) {
            GregTechKubeJSPlugin.generateMachineBlockModels();
        }
    }

    public static void addNamespace(String namespace) {
        CLIENT_DOMAINS.add(namespace);
    }

    public static void clearClient() {
        CONTENTS.clearData();
    }

    public static void addResource(Identifier location, JsonElement obj) {
        addResource(location, obj.toString().getBytes(StandardCharsets.UTF_8));
    }

    public static void addResource(Identifier location, byte[] data) {
        if (ConfigHolder.INSTANCE.dev.dumpAssets) {
            Path parent = GTCEu.GTCEU_FOLDER.resolve("dumped/assets");
            writeJson(location, parent, data);
        }
        CONTENTS.addToData(location, data);
    }

    public static void addBlockModel(Identifier loc, JsonElement obj) {
        if (!loc.getPath().startsWith("block/")) {
            loc = loc.withPrefix("block/");
        }
        addModel(loc, obj);
    }

    public static void addBlockModel(Identifier loc, Supplier<JsonElement> obj) {
        addBlockModel(loc, obj.get());
    }

    public static void addItemModel(Identifier loc, JsonElement obj) {
        com.gregtechceu.gtceu.data.model.NativeDynamicItemFiles.emit(loc, obj, GTDynamicResourcePack::addResource);
    }

    public static void addItemModel(Identifier loc, Supplier<JsonElement> obj) {
        addItemModel(loc, obj.get());
    }

    public static void addModel(Identifier loc, JsonElement obj) {
        loc = MODEL_ID_CONVERTER.idToFile(loc);
        addResource(loc, obj);
    }

    public static void addModel(Identifier loc, Supplier<JsonElement> obj) {
        addModel(loc, obj.get());
    }

    public static void addBlockState(Identifier loc, JsonElement stateJson) {
        loc = BLOCKSTATE_ID_CONVERTER.idToFile(loc);
        addResource(loc, stateJson);
    }

    public static void addBlockState(Identifier loc, BlockModelDefinitionGenerator generator) {
        addBlockState(loc, BlockStateModelDispatcher.CODEC.encodeStart(JsonOps.INSTANCE, generator.create()).getOrThrow());
    }

    public static void addClientItem(Identifier id, net.minecraft.client.renderer.item.ClientItem item) {
        addResource(id.withPrefix("items/").withSuffix(".json"),
                net.minecraft.client.renderer.item.ClientItem.CODEC.encodeStart(JsonOps.INSTANCE, item).getOrThrow());
    }

    public static void addBlockState(Identifier loc, Supplier<JsonElement> generator) {
        addBlockState(loc, generator.get());
    }

    public static void addBlockState(BlockModelDefinitionGenerator generator) {
        addBlockState(BuiltInRegistries.BLOCK.getKey(generator.block()), generator);
    }

    public static void addAtlasSpriteSource(Identifier atlasLoc, SpriteSource source) {
        addAtlasSpriteSourceList(atlasLoc, Collections.singletonList(source));
    }

    public static void addAtlasSpriteSourceList(Identifier loc, List<SpriteSource> sources) {
        loc = ATLAS_ID_CONVERTER.idToFile(loc);
        JsonElement sourceJson = SpriteSources.FILE_CODEC.encodeStart(JsonOps.INSTANCE, sources)
                .getOrThrow();
        addResource(loc, sourceJson);
    }

    @Override
    public @Nullable IoSupplier<InputStream> getRootResource(String... elements) {
        if (elements.length > 0 && elements[0].equals("pack.png")) {
            return () -> GTCEu.class.getResourceAsStream("/icon.png");
        }
        return null;
    }

    @Override
    public @Nullable IoSupplier<InputStream> getResource(PackType type, Identifier location) {
        if (type == PackType.CLIENT_RESOURCES) {
            return CONTENTS.getResource(location);
        }
        return null;
    }

    @Override
    public void listResources(PackType packType, String namespace, String path, ResourceOutput resourceOutput) {
        if (packType == PackType.CLIENT_RESOURCES) {
            CONTENTS.listResources(namespace, path, resourceOutput);
        }
    }

    @Override
    public Set<String> getNamespaces(PackType type) {
        return type == PackType.CLIENT_RESOURCES ? CLIENT_DOMAINS : Set.of();
    }

    @SuppressWarnings("unchecked")
    @Override
    public @Nullable <T> T getMetadataSection(MetadataSectionType<T> metaReader) {
        if (metaReader == PackMetadataSection.CLIENT_TYPE) {
            return (T) new PackMetadataSection(Component.literal("GTCEu dynamic assets"),
                    new net.minecraft.util.InclusiveRange<>(SharedConstants.getCurrentVersion().packVersion(PackType.CLIENT_RESOURCES)));
        }
        return null;
    }

    @Override
    public PackLocationInfo location() {
        return info;
    }

    @Override
    public void close() {
        // NOOP
    }
}
