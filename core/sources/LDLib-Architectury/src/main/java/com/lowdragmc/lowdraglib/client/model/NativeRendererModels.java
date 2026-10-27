package com.lowdragmc.lowdraglib.client.model;
import com.lowdragmc.lowdraglib.client.renderer.NativeBlockModelProvider;
import com.lowdragmc.lowdraglib.client.model.forge.LDLRendererModel;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.neoforge.client.event.ModelEvent;

/** Provider geometry supersedes resource blockstate variants, including absent variants. */
public final class NativeRendererModels {
    private NativeRendererModels() {}
    public static void modify(ModelEvent.ModifyBakingResult event) {
        var models = event.getBakingResult().blockStateModels();
        for (var block : BuiltInRegistries.BLOCK) {
            if (block instanceof NativeBlockModelProvider) {
                for (var state : block.getStateDefinition().getPossibleStates()) {
                    models.put(state, new LDLRendererModel.RendererBakedModel());
                }
            }
        }
    }
}
