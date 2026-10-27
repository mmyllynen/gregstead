package com.gregtechceu.gtceu.api.recipe.lookup.ingredient.item;

import com.gregtechceu.gtceu.api.recipe.ingredient.CustomDataIngredient;
import com.gregtechceu.gtceu.api.recipe.lookup.ingredient.AbstractMapIngredient;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import java.util.List;

/** Query-side subset matching; strict and partial constraint queries stay separate. */
public final class CustomDataMapIngredient extends AbstractMapIngredient {
    private final ItemStack stack;
    private final CustomDataIngredient ingredient;
    private final boolean strict;
    private CustomDataMapIngredient(ItemStack stack,CustomDataIngredient ingredient,boolean strict){this.stack=stack.copy();this.ingredient=ingredient;this.strict=strict;}
    public static List<AbstractMapIngredient> from(CustomDataIngredient ingredient){return ingredient.getItems().<AbstractMapIngredient>map(s->new CustomDataMapIngredient(s,ingredient,ingredient.strict())).toList();}
    public static List<AbstractMapIngredient> from(ItemStack stack){return List.of(new CustomDataMapIngredient(stack,null,false),new CustomDataMapIngredient(stack,null,true));}
    @Override protected int hash(){return 31*stack.getItem().hashCode()+(strict?1:0);}
    @Override public boolean isSpecialIngredient(){return true;}
    @Override public boolean equals(Object value){
        if(this==value)return true;
        if(!(value instanceof CustomDataMapIngredient other)||strict!=other.strict||!ItemStack.isSameItem(stack,other.stack))return false;
        if(other.ingredient!=null)return other.ingredient.test(stack);
        if(ingredient!=null)return ingredient.test(other.stack);
        CompoundTag a=stack.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag();
        return a.equals(other.stack.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag());
    }
}
