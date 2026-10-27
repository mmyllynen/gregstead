package com.gregtechceu.gtceu.utils.data;

import com.gregtechceu.gtceu.api.registry.registrate.provider.GTBlockstateProvider;
import com.gregtechceu.gtceu.common.registry.GTRegistration;
import com.gregtechceu.gtceu.data.model.NativeRuntimeModelOutputs;
import com.gregtechceu.gtceu.data.model.NativeTextureResources;
import com.gregtechceu.gtceu.data.pack.GTDynamicResourcePack;
import com.google.gson.JsonElement;
import com.tterrag.registrate.AbstractRegistrate;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import java.util.function.BiConsumer;

/** Runtime native model generation emitted into the dynamic resource pack. */
public class RuntimeBlockstateProvider extends GTBlockstateProvider {
    public static final RuntimeBlockstateProvider INSTANCE = new RuntimeBlockstateProvider(
            GTRegistration.REGISTRATE, GTDynamicResourcePack::addResource);
    private final NativeRuntimeModelOutputs outputs;
    private final BiConsumer<Identifier, JsonElement> consumer;
    public RuntimeBlockstateProvider(AbstractRegistrate<?> parent, BiConsumer<Identifier, JsonElement> consumer) {
        this(parent, consumer, new NativeRuntimeModelOutputs());
    }
    private RuntimeBlockstateProvider(AbstractRegistrate<?> parent, BiConsumer<Identifier, JsonElement> consumer,
                                      NativeRuntimeModelOutputs outputs) {
        super(parent, outputs::state, outputs, outputs::model);
        this.outputs = outputs;
        this.consumer = consumer;
        setTextureResources(sprite -> new NativeTextureResources(Minecraft.getInstance().getResourceManager()).test(sprite));
    }
    @Override public void run() {
        emitPendingBlockstates();
        outputs.flush(consumer);
        clearGeneratedCycle();
    }
}
