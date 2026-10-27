package com.gregtechceu.gtceu.api.sync_system.data_transformers;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.*;
import net.neoforged.neoforge.fluids.FluidStack;
import com.mojang.serialization.JsonOps;
import com.google.gson.JsonParser;
/** Native stack/component codecs retain original managed empty-compound and JSON-string schemas. */
public final class NativeSyncValueCodecs {
    private NativeSyncValueCodecs() {}
    public static Tag item(ItemStack value,HolderLookup.Provider lookup) {
        return value.isEmpty()?new CompoundTag():ItemStack.CODEC.encodeStart(lookup.createSerializationContext(NbtOps.INSTANCE),value).getOrThrow();
    }
    /** Configuration readers historically treat empty or malformed item compounds as empty. */
    public static ItemStack item(CompoundTag value,HolderLookup.Provider lookup) {
        return value.isEmpty()?ItemStack.EMPTY:ItemStack.CODEC.parse(lookup.createSerializationContext(NbtOps.INSTANCE),value)
                .result().orElse(ItemStack.EMPTY);
    }
    public static Tag fluid(FluidStack value,HolderLookup.Provider lookup) {
        return value.isEmpty()?new CompoundTag():FluidStack.CODEC.encodeStart(lookup.createSerializationContext(NbtOps.INSTANCE),value).getOrThrow();
    }
    public static StringTag component(Component value,HolderLookup.Provider lookup) {
        return StringTag.valueOf(ComponentSerialization.CODEC.encodeStart(lookup.createSerializationContext(JsonOps.INSTANCE),value).getOrThrow().toString());
    }
    public static Component component(StringTag value,HolderLookup.Provider lookup) {
        return ComponentSerialization.CODEC.parse(lookup.createSerializationContext(JsonOps.INSTANCE),JsonParser.parseString(value.value())).getOrThrow();
    }
}
