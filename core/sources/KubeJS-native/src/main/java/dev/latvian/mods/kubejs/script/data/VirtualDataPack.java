package dev.latvian.mods.kubejs.script.data;

import dev.latvian.mods.kubejs.generator.KubeDataGenerator;
import dev.latvian.mods.kubejs.script.ScriptType;
import dev.latvian.mods.kubejs.util.Cast;
import dev.latvian.mods.kubejs.util.ID;
import dev.latvian.mods.kubejs.util.RegistryAccessContainer;
import net.minecraft.server.packs.PackType;
import net.neoforged.neoforge.registries.datamaps.DataMapFile;
import net.neoforged.neoforge.registries.datamaps.DataMapType;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class VirtualDataPack extends VirtualResourcePack implements KubeDataGenerator, dev.latvian.mods.kubejs.util.NativeItemComponentRules.Source {
	private final Map<DataMapType<?, ?>, VirtualDataMapFile<?, ?>> dataMaps;
    private final dev.latvian.mods.kubejs.util.NativeItemComponentRules componentRules;

	public VirtualDataPack(GeneratedDataStage stage, Supplier<RegistryAccessContainer> registries) {
		super(ScriptType.SERVER, PackType.SERVER_DATA, stage, registries);

		this.dataMaps = new HashMap<>();
        this.componentRules = new dev.latvian.mods.kubejs.util.NativeItemComponentRules(stage.name);
	}

    @Override public dev.latvian.mods.kubejs.util.NativeItemComponentRules componentRules() { return componentRules; }

	@Override
	public <R, T> void dataMap(DataMapType<R, T> type, Consumer<VirtualDataMapFile<R, T>> consumer) {
		VirtualDataMapFile<R, T> map = Cast.to(dataMaps.computeIfAbsent(type,
			k -> new VirtualDataMapFile<>(type, this)));
		consumer.accept(map);
	}

	@Override
	public void reset() {
		super.reset();

		dataMaps.clear();
        componentRules.reset();
	}

	@Override
	public void flush() {
		super.flush();

		var jsonOps = getRegistries().json();

		for (var typeEntry : dataMaps.entrySet()) {
			var type = typeEntry.getKey();
			var id = type.id();

			var registry = type.registryKey();
			var regId = registry.identifier();

			var codec = DataMapFile.codec(Cast.to(registry), type);
			var data = typeEntry.getValue();
			var file = data.toFile();

			var json = codec.encodeStart(jsonOps, Cast.to(file))
				.getOrThrow(str -> new RuntimeException("Failed to encode data for %s / %s / %s: %s".formatted(regId, id, data, str)));

			add(GeneratedData.json(id.withPath("data_maps/%s/%s.json".formatted(ID.resourcePath(regId), id.getPath())), () -> json));
		}

		dataMaps.clear();
	}
}
