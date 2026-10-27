package com.lowdragmc.lowdraglib.transfer.persistence;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.function.Supplier;

/** Adapts original named NBT SavedData without changing its payload. */
public final class LegacySavedDataTypes {
    private static final Set<Identifier> LEGACY_IDS = ConcurrentHashMap.newKeySet();
    private LegacySavedDataTypes() {}
    @FunctionalInterface public interface Writer<T> {
        CompoundTag save(T data, CompoundTag tag, HolderLookup.Provider registries);
    }
    public static <T extends SavedData> SavedDataType<T> type(String name, ServerLevel level,
            Supplier<T> create, BiFunction<CompoundTag,HolderLookup.Provider,T> read, Writer<T> write) {
        if (!name.matches("[a-z0-9_][a-z0-9_.-]*")) throw new IllegalArgumentException("Invalid legacy saved-data name: " + name);
        var id = Identifier.withDefaultNamespace(name);
        LEGACY_IDS.add(id);
        return new SavedDataType<>(id, create, CompoundTag.CODEC.xmap(
                tag -> read.apply(tag, level.registryAccess()),
                data -> write.save(data, new CompoundTag(), level.registryAccess())));
    }
    public static Path readableFile(Identifier id, Path nativeFile) {
        if (!LEGACY_IDS.contains(id) || Files.exists(nativeFile)) return nativeFile;
        Path legacy = nativeFile.getParent().getParent().resolve(id.getPath()+".dat");
        return Files.isRegularFile(legacy) ? legacy : nativeFile;
    }
}
