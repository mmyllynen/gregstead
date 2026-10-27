package dev.latvian.mods.kubejs.util;
import com.mojang.serialization.DynamicOps;
import net.minecraft.core.*;
import net.minecraft.resources.*;
import java.util.Optional;
/** Preserves the actual reload holder getters without assuming they enumerate a full registry. */
public final class NativeRegistryOps {
    private NativeRegistryOps() {}
    public static <T> RegistryOps<T> create(DynamicOps<T> ops, HolderGetter.Provider provider) {
        if (provider instanceof HolderLookup.Provider complete) return RegistryOps.create(ops, complete);
        return RegistryOps.create(ops, new RegistryOps.RegistryInfoLookup() {
            public <E> Optional<HolderGetter<E>> lookup(ResourceKey<? extends Registry<? extends E>> key) {
                return provider.lookup(key).map(getter -> getter);
            }
            @SuppressWarnings("unchecked")
            public <E> Optional<HolderLookup<E>> fullLookup(ResourceKey<? extends Registry<? extends E>> key) {
                return provider.lookup(key).filter(getter -> getter instanceof HolderLookup<?>)
                        .map(getter -> (HolderLookup<E>) getter);
            }
        });
    }
}
