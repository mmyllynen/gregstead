package dev.emi.emi.jemi.impl;

import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.jemi.JemiPlugin;
import dev.emi.emi.jemi.JemiUtil;
import java.util.List;
import java.util.Optional;
import mezz.jei.api.gui.builder.IIngredientAcceptor;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import net.minecraft.client.Minecraft;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplayContext;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.material.Fluid;

/** Native JEI ingredient overloads share one ordered, typed conversion path. */
public abstract class NativeIngredientAcceptor<T extends IIngredientAcceptor<T>> implements IIngredientAcceptor<T> {
    protected abstract T self();
    protected abstract void replace(List<EmiStack> stacks);
    protected mezz.jei.api.recipe.RecipeIngredientRole role() { return mezz.jei.api.recipe.RecipeIngredientRole.RENDER_ONLY; }
    private mezz.jei.library.ingredients.DisplayIngredientAcceptor data;
    public mezz.jei.library.ingredients.DisplayIngredientAcceptor nativeData() {
        if (data == null) data = new mezz.jei.library.ingredients.DisplayIngredientAcceptor(
            (mezz.jei.library.ingredients.IIngredientManagerInternal) JemiPlugin.runtime.getIngredientManager(), getContextMap(), role());
        return data;
    }
    private T changed() {
        replace(nativeData().getAllIngredients().stream().map(typed -> typed == null ? EmiStack.EMPTY : JemiUtil.getStack(typed)).toList());
        return self();
    }

    @Override public ContextMap getContextMap() {
        return SlotDisplayContext.fromLevel(java.util.Objects.requireNonNull(Minecraft.getInstance().level, "Recipe displays require an active client level"));
    }
    @Override public T add(SlotDisplay display) { nativeData().add(display); return changed(); }
    @Override public <I> T add(IIngredientType<I> type, SlotDisplay display) { nativeData().add(type,display); return changed(); }
    @Override public T add(ItemStack stack) { nativeData().add(stack); return changed(); }
    @Override public T add(ItemLike item) { nativeData().add(item); return changed(); }
    @Override public T add(ItemStackTemplate template) { nativeData().add(template); return changed(); }
    @Override public T add(Fluid fluid) { nativeData().add(fluid); return changed(); }
    @Override public T add(Fluid fluid, long amount) { nativeData().add(fluid,amount); return changed(); }
    @Override public T add(Fluid fluid, long amount, DataComponentPatch patch) { nativeData().add(fluid,amount,patch); return changed(); }
    @Override public T add(Ingredient ingredient) { nativeData().add(ingredient); return changed(); }
    @Override public <I> T add(IIngredientType<I> type, Ingredient ingredient) { nativeData().add(type,ingredient); return changed(); }
    @Override public <I> T add(ITypedIngredient<I> typed) { nativeData().add(typed); return changed(); }
    @Override public <I> T add(IIngredientType<I> type, I value) { nativeData().add(type,value); return changed(); }
    @Override public <I> T addIngredients(IIngredientType<I> type, List<I> values) { nativeData().addIngredients(type,values); return changed(); }
    @Override public T addIngredientsUnsafe(List<?> values) { nativeData().addIngredientsUnsafe(values); return changed(); }
    @Override public T addTypedIngredients(List<ITypedIngredient<?>> values) { nativeData().addTypedIngredients(values); return changed(); }
    @Override public T addOptionalTypedIngredients(List<Optional<ITypedIngredient<?>>> values) { nativeData().addOptionalTypedIngredients(values); return changed(); }
    @Override public T addItemStacks(List<ItemStack> values) { nativeData().addItemStacks(values); return changed(); }
}
