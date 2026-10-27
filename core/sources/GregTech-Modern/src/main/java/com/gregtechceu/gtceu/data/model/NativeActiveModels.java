package com.gregtechceu.gtceu.data.model;

import com.gregtechceu.gtceu.api.block.property.GTBlockStateProperties;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.client.data.models.blockstates.PropertyDispatch;
import net.minecraft.client.renderer.block.dispatch.Variant;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;

public final class NativeActiveModels {
    private NativeActiveModels() {}
    public static MultiVariantGenerator states(Block block, Identifier inactive, Identifier active) {
        return MultiVariantGenerator.dispatch(block).with(PropertyDispatch.initial(GTBlockStateProperties.ACTIVE)
                .generate(enabled -> BlockModelGenerators.variant(new Variant(enabled ? active : inactive))));
    }
    public static NativeInlineModel cube(Identifier texture) {
        return cube(texture,false);
    }
    public static NativeInlineModel cube(Identifier texture,boolean forceTranslucent) {
        return new NativeInlineModel().parent(Identifier.withDefaultNamespace("block/cube_all")).texture("all", texture,forceTranslucent);
    }
    public static NativeInlineModel sided(Identifier side, Identifier bottom, Identifier top) {
        return new NativeInlineModel().parent(Identifier.withDefaultNamespace("block/cube_bottom_top"))
                .texture("side", side).texture("bottom", bottom).texture("top", top);
    }
    public static NativeInlineModel glowing(Identifier texture) {
        return new NativeInlineModel().parent(Identifier.parse("gtceu:block/cube_2_layer/all"))
                .texture("bot_all", texture).texture("top_all", texture.withSuffix("_bloom"));
    }
}
