package com.gregtechceu.gtceu.data.model;

import com.gregtechceu.gtceu.api.registry.registrate.MachineBuilder;
import com.gregtechceu.gtceu.api.registry.registrate.provider.GTBlockstateProvider;
import com.gregtechceu.gtceu.data.model.builder.MachineModelBuilder;
import com.tterrag.registrate.providers.DataGenContext;
import net.minecraft.world.level.block.Block;

/** Avoids lambda-metafactory resolution of client provider parameter types on the server. */
public final class DeferredMachineModelInitializer implements MachineBuilder.ModelInitializer {
    @FunctionalInterface public interface Action { void run(Object context, Object provider, Object builder); }
    private final Action action;
    public DeferredMachineModelInitializer(Action action) { this.action = action; }
    @Override public void configureModel(DataGenContext<Block, ? extends Block> context, GTBlockstateProvider provider, MachineModelBuilder builder) {
        action.run(context, provider, builder);
    }
}
