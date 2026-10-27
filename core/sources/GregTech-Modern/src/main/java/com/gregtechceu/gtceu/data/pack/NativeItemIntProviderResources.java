package com.gregtechceu.gtceu.data.pack;

import com.gregtechceu.gtceu.common.registry.GTRegistration;
import net.minecraft.core.*;
import net.minecraft.core.registries.*;
import net.minecraft.resources.*;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.world.level.storage.loot.providers.number.ints.*;
import com.mojang.serialization.*;
import java.util.*;
import java.util.stream.Stream;

/** Encode the actual Registrate factories before native reloadable registry loading. */
public final class NativeItemIntProviderResources {
    private NativeItemIntProviderResources() {}
    public static void generate(HolderLookup.Provider context) {
        var providers = new BootstrapRegistry<ContextIntProvider>(Registries.CONTEXT_INT_PROVIDER, Lifecycle.experimental());
        var lookup = HolderLookup.Provider.create(Stream.concat(context.listRegistries()
                .filter(registry -> !registry.key().equals(Registries.CONTEXT_INT_PROVIDER)), Stream.of(providers)));
        BootstrapContext<ContextIntProvider> bootstrap = new BootstrapContext<>() {
            public Holder.Reference<ContextIntProvider> register(ResourceKey<ContextIntProvider> key, ContextIntProvider value) {
                var holder = providers.getOrThrow(key); holder.bindValue(value); return holder;
            }
            public <S> HolderGetter<S> lookup(ResourceKey<? extends Registry<? extends S>> key) { return lookup.lookupOrThrow(key); }
            public <S> Stream<Holder.Reference<S>> listContextElements(ResourceKey<? extends Registry<? extends S>> key) { return context.lookupOrThrow(key).listElements(); }
        };
        var converter = FileToIdConverter.registry(Registries.CONTEXT_INT_PROVIDER);
        var ops = lookup.createSerializationContext(JsonOps.INSTANCE);
        GTRegistration.REGISTRATE.getDataGenInitializer().getItemIntProviders().forEach((key, factory) -> {
            var value = factory.apply(bootstrap);
            bootstrap.register(key, value);
            var json = ContextIntProviders.DIRECT_CODEC.encodeStart(ops, value).getOrThrow();
            GTDynamicDataPack.addResource(converter.idToFile(key.identifier()), json);
        });
    }
}
