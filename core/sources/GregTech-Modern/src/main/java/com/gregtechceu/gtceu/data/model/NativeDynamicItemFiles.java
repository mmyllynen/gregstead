package com.gregtechceu.gtceu.data.model;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import net.minecraft.client.data.models.model.ItemModelUtils;
import net.minecraft.client.renderer.item.ClientItem;
import net.minecraft.resources.Identifier;
import java.util.function.BiConsumer;

/** Emits geometry and its current client item definition into the dynamic pack. */
public final class NativeDynamicItemFiles {
    private NativeDynamicItemFiles() {}
    public static void emit(Identifier id, JsonElement geometry, BiConsumer<Identifier, JsonElement> output) {
        Identifier itemId = id.getPath().startsWith("item/") ? id.withPath(id.getPath().substring(5)) : id;
        Identifier modelId = id.getPath().startsWith("item/") ? id : id.withPrefix("item/");
        JsonElement definition = ClientItem.CODEC.encodeStart(JsonOps.INSTANCE,
                new ClientItem(ItemModelUtils.plainModel(modelId), ClientItem.Properties.DEFAULT)).getOrThrow();
        output.accept(modelId.withPrefix("models/").withSuffix(".json"), geometry);
        output.accept(itemId.withPrefix("items/").withSuffix(".json"), definition);
    }
}
