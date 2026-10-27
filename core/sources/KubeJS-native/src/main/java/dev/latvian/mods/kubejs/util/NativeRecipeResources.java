package dev.latvian.mods.kubejs.util;

import com.google.gson.JsonElement;
import net.minecraft.SharedConstants;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.*;
import net.minecraft.server.packs.*;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.resources.*;
import net.minecraft.util.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Consumer;

/** Recipe JSON changes feed the original native decoder; unchanged resources retain their provenance. */
public final class NativeRecipeResources {
    private NativeRecipeResources() {}
    public static Map<Identifier, Resource> mutate(Map<Identifier, Resource> resources, FileToIdConverter lister,
                                                  Consumer<Map<Identifier, JsonElement>> change) {
        var jsons = new LinkedHashMap<Identifier, JsonElement>();
        var before = new LinkedHashMap<Identifier, JsonElement>();
        for (var entry : resources.entrySet()) {
            try (var reader = entry.getValue().openAsReader()) {
                var json = StrictJsonParser.parse(reader);
                var id = lister.fileToId(entry.getKey());
                jsons.put(id, json); before.put(id, json.deepCopy());
            } catch (IOException | com.google.gson.JsonParseException failed) {
                // Keep malformed resources intact so the native PendingRegistration reports the original error.
            }
        }
        change.accept(jsons);
        var result = new LinkedHashMap<>(resources);
        var generated = new GeneratedPack();
        for (var id : before.keySet()) if (!jsons.containsKey(id)) result.remove(lister.idToFile(id));
        for (var entry : jsons.entrySet()) {
            if (entry.getValue().equals(before.get(entry.getKey()))) continue;
            var file = lister.idToFile(entry.getKey());
            byte[] content = entry.getValue().toString().getBytes(StandardCharsets.UTF_8);
            generated.content.put(file, content);
            var previous = resources.get(file);
            result.put(file, new Resource(generated, () -> new ByteArrayInputStream(content),
                    previous == null ? ResourceMetadata.EMPTY_SUPPLIER : previous::metadata));
        }
        return result;
    }
    private static final class GeneratedPack extends AbstractPackMetadataResources implements PackResources {
        private final Map<Identifier, byte[]> content = new LinkedHashMap<>();
        GeneratedPack() {
            super(new PackLocationInfo("kubejs_generated_recipes", Component.literal("KubeJS generated recipes"),
                    PackSource.BUILT_IN, Optional.empty()));
        }
        public IoSupplier<InputStream> getRootResource(String... path) {
            if (path.length != 1 || !path[0].equals(PACK_META)) return null;
            var metadata = new PackMetadataSection(Component.literal("KubeJS generated recipes"),
                    new InclusiveRange<>(SharedConstants.getCurrentVersion().packVersion(PackType.SERVER_DATA)));
            var section = PackMetadataSection.forPackType(PackType.SERVER_DATA);
            var root = new com.google.gson.JsonObject();
            root.add(section.name(), section.codec().encodeStart(com.mojang.serialization.JsonOps.INSTANCE, metadata).getOrThrow());
            byte[] bytes = root.toString().getBytes(StandardCharsets.UTF_8);
            return () -> new ByteArrayInputStream(bytes);
        }
        public IoSupplier<InputStream> getResource(PackType type, Identifier id) {
            byte[] bytes = type == PackType.SERVER_DATA ? content.get(id) : null;
            return bytes == null ? null : () -> new ByteArrayInputStream(bytes);
        }
        public void listResources(PackType type, String namespace, String directory, ResourceOutput output) {
            if (type != PackType.SERVER_DATA) return;
            String prefix = directory.isEmpty() || directory.endsWith("/") ? directory : directory + "/";
            for (var id : content.keySet()) if (id.getNamespace().equals(namespace) && id.getPath().startsWith(prefix))
                output.accept(id, getResource(type, id));
        }
        public Set<String> getNamespaces(PackType type) {
            if (type != PackType.SERVER_DATA) return Set.of();
            var names = new HashSet<String>();for (var id : content.keySet()) names.add(id.getNamespace());return Set.copyOf(names);
        }
        public void close() {}
    }
}
