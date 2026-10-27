package com.lowdragmc.lowdraglib.client.model.custommodel;

import com.lowdragmc.lowdraglib.core.mixins.accessor.ModelBakeryAccessor;
import net.minecraft.client.resources.model.ResolvedModel;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.event.ModelEvent;
import java.util.*;

/** CTM discovery through native root dependencies and resolved inherited texture slots. */
public final class NativeCtmRegistration {
    private NativeCtmRegistration() {}
    public static void modify(ModelEvent.ModifyBakingResult event) {
        LDLMetadataSection.clearCache();
        var access = (ModelBakeryAccessor) event.getModelBakery();
        var models = access.getResolvedModels();
        for (var entry : event.getBakingResult().blockStateModels().entrySet()) {
            if (entry.getValue() instanceof CustomBakedModel<?>
                    || entry.getValue() instanceof com.lowdragmc.lowdraglib.client.model.forge.LDLRendererModel.RendererBakedModel) continue;
            var root = access.getUnbakedBlockStateModels().get(entry.getKey());
            if (root == null) continue;
            Deque<Identifier> pending = new ArrayDeque<>();
            root.resolveDependencies(pending::addLast);
            Set<Identifier> seen = new HashSet<>();
            boolean wrap = false;
            while (!wrap && !pending.isEmpty()) {
                Identifier id = pending.removeFirst();
                if (!seen.add(id)) continue;
                ResolvedModel model = models.get(id);
                if (model == null) continue;
                var slots = model.getTopTextureSlots();
                Set<ResolvedModel> parents = Collections.newSetFromMap(new IdentityHashMap<>());
                for (ResolvedModel current = model; current != null && parents.add(current); current = current.parent()) {
                    for (String slot : current.wrapped().textureSlots().values().keySet()) {
                        var material = slots.getMaterial(slot);
                        if (material != null && !LDLMetadataSection.getMetadata(
                                LDLMetadataSection.spriteToAbsolute(material.sprite())).isMissing()) {
                            wrap = true;
                            break;
                        }
                    }
                    current.wrapped().resolveDependencies(pending::addLast);
                    if (wrap) break;
                }
            }
            if (wrap) entry.setValue(new CustomBakedModel<>(entry.getValue()));
        }
    }
}
