package com.gregtechceu.gtceu.integration.map.ftbchunks;

import dev.ftb.mods.ftblibrary.config.value.Config;
import dev.ftb.mods.ftblibrary.config.value.BooleanValue;
import dev.ftb.mods.ftblibrary.config.serializer.Json5ConfigSerializer;
import com.mojang.brigadier.StringReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

/** Migrates the three privately owned legacy layer flags; keeps the original SNBT file intact. */
public final class NativeFtbLayerConfig {
    public static final String GROUP = "journeymap.options.layers";
    private NativeFtbLayerConfig() {}
    public static void load(Config config, Path directory, Map<String, BooleanValue> values) throws IOException {
        Path current = directory.resolve("client-config.json5");
        Path legacy = directory.resolve("client-config.snbt");
        if (!Files.exists(current) && Files.isRegularFile(legacy)) {
            var root = new LegacyFlags(Files.readString(legacy)).read();
            var group = root.get(GROUP);
            if (group instanceof Map<?, ?> flags) {
                for (var entry : values.entrySet()) {
                    Object value = flags.get(entry.getKey());
                    if (value != null) entry.getValue().set(booleanValue(value));
                }
            }
        }
        Json5ConfigSerializer.readFromFile(config, current);
    }
    public static void save(Config config, Path directory) throws IOException {
        Json5ConfigSerializer.writeToFile(config, directory.resolve("client-config.json5"));
    }
    private static boolean booleanValue(Object value) throws IOException {
        if (value.equals("true")) return true;
        if (value.equals("false")) return false;
        String number = value.toString().replaceFirst("[bBsSlL]$", "");
        try { return Long.parseLong(number) != 0; }
        catch (NumberFormatException invalid) { throw new IOException("Invalid legacy layer boolean: " + value, invalid); }
    }
    /** Old FTB canonical configs allow line-separated pairs and #/slash comments, unlike vanilla SNBT. */
    private static final class LegacyFlags {
        private final StringReader reader;
        LegacyFlags(String text) { reader = new StringReader(text); }
        Map<String, Object> read() throws IOException {
            var result = compound(); space();
            if (reader.canRead()) throw new IOException("Trailing legacy config content");
            return result;
        }
        private void space() {
            while (reader.canRead()) {
                char c = reader.peek();
                if (Character.isWhitespace(c) || c == ',') { reader.skip(); continue; }
                if (c == '#' || (c == '/' && reader.canRead(2) && reader.peek(1) == '/')) {
                    while (reader.canRead() && reader.read() != '\n') {}
                    continue;
                }
                break;
            }
        }
        private Map<String, Object> compound() throws IOException {
            space(); expect('{'); var result = new LinkedHashMap<String, Object>();
            while (true) {
                space(); if (!reader.canRead()) throw new IOException("Unclosed legacy config group");
                if (reader.peek() == '}') { reader.skip(); return result; }
                String key = token(); space();
                if (!reader.canRead() || (reader.peek() != ':' && reader.peek() != '=')) throw new IOException("Missing legacy config separator");
                reader.skip(); space();
                Object value = reader.canRead() && reader.peek() == '{' ? compound() : token();
                result.put(key, value);
            }
        }
        private String token() throws IOException {
            if (!reader.canRead()) throw new IOException("Missing legacy config value");
            if (reader.peek() == '\"' || reader.peek() == '\'') {
                try { return reader.readString(); }
                catch (com.mojang.brigadier.exceptions.CommandSyntaxException invalid) { throw new IOException(invalid); }
            }
            int start = reader.getCursor();
            while (reader.canRead() && !Character.isWhitespace(reader.peek()) && ",{}:=#".indexOf(reader.peek()) < 0) reader.skip();
            if (start == reader.getCursor()) throw new IOException("Invalid legacy config token");
            return reader.getString().substring(start, reader.getCursor());
        }
        private void expect(char expected) throws IOException {
            if (!reader.canRead() || reader.read() != expected) throw new IOException("Expected legacy config " + expected);
        }
    }
}
