package dev.emi.emi.jemi;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import mezz.jei.api.ingredients.ITypedIngredient;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.RegistryOps;

/** Native typed-ingredient codec persistence, retaining type, amount and component data. */
public final class NativeJemiPersistence {
    private NativeJemiPersistence() {}
    public record Entry(ITypedIngredient<?> ingredient,long amount) {}
    public static JsonElement encode(Codec<ITypedIngredient<?>> codec,RegistryAccess registries,ITypedIngredient<?> ingredient,long amount) {
        var value=new JsonObject();
        value.add("data",codec.encodeStart(RegistryOps.create(JsonOps.INSTANCE,registries),ingredient).getOrThrow());
        value.addProperty("amount",amount);
        return value;
    }
    public static Entry decode(Codec<ITypedIngredient<?>> codec,RegistryAccess registries,JsonElement value) {
        var object=value.getAsJsonObject();
        return new Entry(codec.parse(RegistryOps.create(JsonOps.INSTANCE,registries),object.get("data")).getOrThrow(),
            net.minecraft.util.GsonHelper.getAsLong(object,"amount",1));
    }
}
