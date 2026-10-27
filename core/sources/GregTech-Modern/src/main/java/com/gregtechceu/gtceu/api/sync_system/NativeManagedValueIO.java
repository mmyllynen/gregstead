package com.gregtechceu.gtceu.api.sync_system;

import com.mojang.serialization.*;
import java.util.function.Function;
import java.util.stream.Stream;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/** Root NBT bridge uses the actual native save context, including detached block entities. */
public final class NativeManagedValueIO {
    private NativeManagedValueIO() {}
    private static final MapCodec<CompoundTag> ROOT=MapCodec.assumeMapUnsafe(CompoundTag.CODEC);
    private static final MapCodec<Function<HolderLookup.Provider,CompoundTag>> SAVE=new MapCodec<>() {
        @Override public <T> RecordBuilder<T> encode(Function<HolderLookup.Provider,CompoundTag> save,DynamicOps<T> ops,RecordBuilder<T> prefix) {
            if(ops instanceof RegistryOps<?> registry && registry.lookupProvider instanceof RegistryOps.HolderLookupAdapter adapter)
                return ROOT.encode(save.apply(adapter.lookupProvider),ops,prefix);
            return prefix.withErrorsFrom(DataResult.error(() -> "Managed block entity save requires native registry context"));
        }
        @Override public <T> DataResult<Function<HolderLookup.Provider,CompoundTag>> decode(DynamicOps<T> ops,MapLike<T> input) {
            return DataResult.error(() -> "Save callback codec cannot be decoded");
        }
        @Override public <T> Stream<T> keys(DynamicOps<T> ops) { return Stream.empty(); }
    };
    public static void save(ValueOutput output,Function<HolderLookup.Provider,CompoundTag> save) { output.store(SAVE,save); }
    public static CompoundTag read(ValueInput input) { return input.read(ROOT).orElseGet(CompoundTag::new); }
}
