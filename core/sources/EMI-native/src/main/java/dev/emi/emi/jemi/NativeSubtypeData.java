package dev.emi.emi.jemi;

import mezz.jei.api.ingredients.IIngredientTypeWithSubtypes;
import mezz.jei.api.ingredients.subtypes.ISubtypeManager;
import mezz.jei.api.ingredients.subtypes.UidContext;

public final class NativeSubtypeData {
    private NativeSubtypeData() {}
    public static <T> Object recipe(ISubtypeManager manager, IIngredientTypeWithSubtypes<?,T> type, Object ingredient) {
        return type.castIngredient(ingredient).map(value -> manager.getSubtypeData(type,value,UidContext.Recipe)).orElse(null);
    }
}
