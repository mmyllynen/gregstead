package com.tterrag.registrate.providers;

import net.minecraft.core.Registry;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.SingleRegistryBootstrap;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProvider;
import java.util.function.Function;

import org.jspecify.annotations.Nullable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class DataProviderInitializer {

	private final RegistrySetBuilder worldEntries = new RegistrySetBuilder();
	private final RegistrySetBuilder reloadableEntries = new RegistrySetBuilder();

	private final Map<ProviderType<?>, ProviderType<? extends RegistrateLookupFillerProvider>> providerDependencies = new ConcurrentHashMap<>();

	public DataProviderInitializer() {
		addDependency(ProviderType.ITEM_TAGS, ProviderType.BLOCK_TAGS);
		addDependency(ProviderType.RECIPE_RUNNER, ProviderType.DYNAMIC);
		addDependency(ProviderType.LOOT, ProviderType.DYNAMIC);
	}

	protected RegistrySetBuilder getWorldRegistryProviders() { return worldEntries; }

	protected RegistrySetBuilder getReloadableRegistryProviders() { return reloadableEntries; }

	protected List<Sorted> getSortedProviders() {
		List<Sorted> ans = new ArrayList<>();
		Set<ProviderType<?>> added = new HashSet<>();
		List<Map.Entry<String, ProviderType<?>>> remain = new ArrayList<>(RegistrateDataProvider.TYPES.entrySet());
		while (!remain.isEmpty()) {
			if (!remain.removeIf(e -> {
				ProviderType<?> type = e.getValue();
				var parent = providerDependencies.get(type);
				if (parent == null || added.contains(parent)) {
					ans.add(new Sorted(e.getKey(), type, parent));
					added.add(type);
					return true;
				}
				return false;
			})) throw new IllegalStateException("Looping dependency detected: " + remain);
		}
		return ans;
	}

	public <T> void add(ResourceKey<Registry<T>> registry, SingleRegistryBootstrap<T> provider) {
		boolean reloadable = net.neoforged.neoforge.registries.DataPackRegistriesHooks.getReloadableRegistries()
				.stream().anyMatch(data -> data.key().equals(registry));
		(reloadable ? reloadableEntries : worldEntries).add(registry, provider);
	}

	private final Map<ResourceKey<ContextIntProvider>, Function<BootstrapContext<ContextIntProvider>, ContextIntProvider>> itemIntProviders = new LinkedHashMap<>();

	public void addItemIntProvider(ResourceKey<ContextIntProvider> key,
			Function<BootstrapContext<ContextIntProvider>, ContextIntProvider> factory) {
		if (itemIntProviders.isEmpty()) {
			reloadableEntries.add(Registries.CONTEXT_INT_PROVIDER,
					context -> itemIntProviders.forEach((id, value) -> context.register(id, value.apply(context))));
		}
		itemIntProviders.put(key, factory);
	}

	public Map<ResourceKey<ContextIntProvider>, Function<BootstrapContext<ContextIntProvider>, ContextIntProvider>> getItemIntProviders() {
		return Collections.unmodifiableMap(itemIntProviders);
	}

	public void addDependency(ProviderType<?> dependent, ProviderType<? extends RegistrateLookupFillerProvider> parent) {
		var old = providerDependencies.put(dependent, parent);
		if (old != null) throw new IllegalStateException("Providers can have only 1 prerequisite");
	}

	public record Sorted(
			String id, ProviderType<?> type,
			@Nullable ProviderType<? extends RegistrateLookupFillerProvider> parent
	) {
	}

}
