package com.lowdragmc.lowdraglib.transfer.recipe;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
/** Legacy single-item payload: item and component patch only; count is always one. */
public final class SingleItemStacks {
    private SingleItemStacks() {}
    public static final Codec<ItemStack> CODEC=RecordCodecBuilder.create(instance->instance.group(
            Item.CODEC_WITH_BOUND_COMPONENTS.fieldOf("id").forGetter(ItemStack::typeHolder),
            DataComponentPatch.CODEC.optionalFieldOf("components",DataComponentPatch.EMPTY).forGetter(ItemStack::getComponentsPatch)
    ).apply(instance,(item,components)->new ItemStack(item,1,components)));
}
