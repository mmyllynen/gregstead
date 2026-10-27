package com.gregtechceu.gtceu.data.model;

import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.generators.RegistrateItemModelGenerator;
import com.tterrag.registrate.util.nullness.NonNullBiConsumer;
import net.minecraft.world.item.Item;
import net.minecraft.resources.Identifier;
import com.gregtechceu.gtceu.GTCEu;

/** Client model factories resolved only by the real datagen callback. */
public final class NativeSpecialItemDatagen {
    private NativeSpecialItemDatagen() {}
    public static <T extends Item> NonNullBiConsumer<DataGenContext<Item, T>, RegistrateItemModelGenerator> lamp() {
        return (ctx, provider) -> provider.itemModelOutput.accept(ctx.getEntry(),
                new com.gregtechceu.gtceu.client.model.item.NativeLampItemModel.Unbaked(ctx.getId().withPrefix("block/")));
    }
    public static <T extends Item> NonNullBiConsumer<DataGenContext<Item, T>, RegistrateItemModelGenerator> machine() {
        return (ctx, provider) -> provider.itemModelOutput.accept(ctx.getEntry(),
                new com.gregtechceu.gtceu.client.model.machine.NativeMachineItemModel.Unbaked(ctx.getId().withPrefix("block/machine/")));
    }
    public static <T extends Item> NonNullBiConsumer<DataGenContext<Item, T>, RegistrateItemModelGenerator> nanoSaber() {
        return (ctx, provider) -> {
            var normal = new NativeInlineModel().parent(net.minecraft.resources.Identifier.withDefaultNamespace("item/generated"))
                    .texture("layer0", com.gregtechceu.gtceu.GTCEu.id("item/nano_saber/normal"))
                    .emit(ctx.getId().withPrefix("item/"), provider.modelOutput);
            var active = new NativeInlineModel().parent(net.minecraft.resources.Identifier.withDefaultNamespace("item/handheld"))
                    .texture("layer0", com.gregtechceu.gtceu.GTCEu.id("item/nano_saber/active"))
                    .emit(com.gregtechceu.gtceu.GTCEu.id("item/nano_saber/active"), provider.modelOutput);
            provider.itemModelOutput.accept(ctx.getEntry(), NativeItemRanges.select(
                    com.gregtechceu.gtceu.common.item.behavior.NanoSaberBehavior.OVERRIDE_KEY_LOCATION,
                    normal, java.util.List.of(active), java.util.List.of(1f)));
        };
    }

    public static void cellModel(DataGenContext<Item, ? extends Item> ctx, RegistrateItemModelGenerator prov) {
        Identifier id = ctx.getId().withPrefix("item/");
        Identifier empty = new NativeInlineModel().parent(Identifier.withDefaultNamespace("item/generated"))
                .texture("layer0", id.withSuffix("/base")).emit(id.withSuffix("_empty"), prov.modelOutput);
        Identifier filled = new NativeInlineModel().parent(Identifier.withDefaultNamespace("item/generated"))
                .texture("layer0", id.withSuffix("/base")).texture("layer1", id.withSuffix("/overlay"))
                .emit(id.withSuffix("_filled"), prov.modelOutput);
        prov.itemModelOutput.accept(ctx.getEntry(), NativeItemRanges.select(GTCEu.id("fluid_cell"), empty,
                java.util.List.of(empty, filled), java.util.List.of(0f, 1f)));
    }
    public static <T extends Item> NonNullBiConsumer<DataGenContext<Item, T>, RegistrateItemModelGenerator> overrideModel(Identifier predicate, int modelNumber) {
        if (modelNumber <= 0) return NonNullBiConsumer.noop();
        return (ctx, prov) -> {
            Identifier id = ctx.getId().withPrefix("item/");
            var models = new java.util.ArrayList<Identifier>();
            var thresholds = new java.util.ArrayList<Float>();
            for (int i = 0; i < modelNumber; i++) {
                models.add(new NativeInlineModel().parent(Identifier.withDefaultNamespace("item/generated"))
                        .texture("layer0", id.withSuffix("/" + (i + 1))).emit(id.withSuffix("/" + i), prov.modelOutput));
                thresholds.add(i / 100f);
            }
            prov.itemModelOutput.accept(ctx.getEntry(), NativeItemRanges.select(predicate, models.getFirst(), models, thresholds));
        };
    }

}
