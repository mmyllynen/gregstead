package com.gregtechceu.gtceu.api.recipe.ingredient;

import com.gregtechceu.gtceu.data.recipe.GTIngredientTypes;
import com.lowdragmc.lowdraglib.transfer.recipe.StackIngredient;
import com.lowdragmc.lowdraglib.transfer.recipe.IngredientStacks;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.crafting.IngredientType;
import java.util.stream.Stream;

/** Legacy arbitrary NBT constraints expressed on the native custom_data component. */
public final class CustomDataIngredient implements StackIngredient {
    public static final MapCodec<CustomDataIngredient> CODEC = RecordCodecBuilder.mapCodec(b -> b.group(
            Ingredient.CODEC.fieldOf("inner").forGetter(CustomDataIngredient::inner),
            net.minecraft.nbt.TagParser.LENIENT_CODEC.fieldOf("data").forGetter(CustomDataIngredient::data),
            Codec.BOOL.optionalFieldOf("strict",false).forGetter(CustomDataIngredient::strict)
    ).apply(b,CustomDataIngredient::new));
    private final Ingredient inner;
    private final CompoundTag data;
    private final boolean strict;
    public CustomDataIngredient(Ingredient inner,CompoundTag data,boolean strict){this.inner=inner;this.data=data.copy();this.strict=strict;}
    public Ingredient inner(){return inner;}
    public CompoundTag data(){return data.copy();}
    public boolean strict(){return strict;}
    public static Ingredient of(Item item,CompoundTag data,boolean strict){return new CustomDataIngredient(Ingredient.of(item),data,strict).toVanilla();}
    public boolean matchesData(CompoundTag candidate){return strict?data.equals(candidate):NbtUtils.compareNbt(data,candidate,true);}
    @Override public boolean test(ItemStack stack){return inner.test(stack) && matchesData(stack.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag());}
    @Override public Stream<ItemStack> getItems(){return java.util.Arrays.stream(IngredientStacks.getItems(inner)).map(s->{var copy=s.copy();copy.set(DataComponents.CUSTOM_DATA,CustomData.of(data));return copy;});}
    @Override public boolean isSimple(){return false;}
    @Override public IngredientType<CustomDataIngredient> getType(){return GTIngredientTypes.CUSTOM_DATA_INGREDIENT.get();}
}
