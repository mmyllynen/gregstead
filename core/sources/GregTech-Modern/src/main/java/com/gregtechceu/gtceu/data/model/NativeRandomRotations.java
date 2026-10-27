package com.gregtechceu.gtceu.data.model;

import com.gregtechceu.gtceu.api.registry.registrate.provider.GTBlockstateProvider;
import net.minecraft.client.data.models.MultiVariant;
import net.minecraft.client.renderer.block.dispatch.Variant;
import net.minecraft.resources.Identifier;
import net.minecraft.util.random.WeightedList;

public final class NativeRandomRotations {
    private NativeRandomRotations() {}
    public static MultiVariant variants(Identifier normal, Identifier mirrored) {
        return new MultiVariant(WeightedList.<Variant>builder()
                .add(new Variant(normal))
                .add(GTBlockstateProvider.applyRotation(new Variant(normal), 0, 180, 0))
                .add(new Variant(mirrored))
                .add(GTBlockstateProvider.applyRotation(new Variant(mirrored), 0, 180, 0)).build());
    }
}
