package dev.emi.emi.jemi.impl;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.library.gui.ingredients.ICycler;
import mezz.jei.library.gui.ingredients.RecipeSlotIngredients;
import mezz.jei.library.ingredients.DisplayIngredientAcceptor;
import mezz.jei.library.ingredients.IIngredientManagerInternal;
import mezz.jei.library.ingredients.SlotIngredient;
import net.minecraft.tags.TagKey;

/** Native grouping, visibility, blank cycling and display overrides shared with JEI. */
public final class NativeSlotChoices {
    private final IIngredientManager manager;
    private final RecipeSlotIngredients choices;
    public NativeSlotChoices(IIngredientManager manager, DisplayIngredientAcceptor source,
            mezz.jei.api.recipe.RecipeIngredientRole role, mezz.jei.api.recipe.IFocusGroup focus, Runnable changed) {
        this(manager, source, role, focus, null, changed);
    }
    public NativeSlotChoices(IIngredientManager manager, DisplayIngredientAcceptor source,
            mezz.jei.api.recipe.RecipeIngredientRole role, mezz.jei.api.recipe.IFocusGroup focus,
            java.util.Set<Integer> indexes, Runnable changed) {
        this.manager = manager;
        choices = new RecipeSlotIngredients((IIngredientManagerInternal)manager,source.getContextMap(),role,
            source.getAllSlotIngredients(),focused(source,indexes == null ? source.getMatches(focus, role) : indexes),focus,changed);
    }
    private static List<SlotIngredient<?>> focused(DisplayIngredientAcceptor source, java.util.Set<Integer> indexes) {
        if (indexes == null || indexes.isEmpty()) return null;
        var result = new java.util.ArrayList<SlotIngredient<?>>();
        var all = source.getAllSlotIngredients();
        for (int i=0; i<all.size(); i++) if(indexes.contains(i)) result.add(all.get(i));
        return result;
    }
    public List<ITypedIngredient<?>> all() { return choices.getAllIngredientsList(); }
    public Stream<ITypedIngredient<?>> allValid() { return choices.getAllIngredients(); }
    public Optional<ITypedIngredient<?>> displayed(long second) { return current(second).map(SlotIngredient::typedIngredient); }
    private Optional<SlotIngredient<?>> current(long second) {
        if (!mezz.jei.common.Internal.getClientConfigs().getClientConfig().recipeSlotCyclingEnabled().get())
            return choices.getFirstDisplayedIngredient();
        return choices.getDisplayedIngredient(new ICycler() {
            public <T> Optional<T> getCycled(List<T> list) {
                return list.isEmpty() ? Optional.empty() : Optional.ofNullable(list.get(Math.floorMod(second,list.size())));
            }
        });
    }
    public Stream<ITypedIngredient<?>> displayedAll(long second) {
        return choices.hasDisplayOverrides()
            ? current(second).stream().flatMap(choices::getVisibleTypedIngredientsInDisplayGroup)
            : choices.getVisibleTypedIngredients();
    }
    public Optional<TagKey<?>> tag(long second) {
        if(choices.hasDisplayOverrides()) return current(second).flatMap(value -> choices.getDisplayGroupTagKey(value,
            () -> equivalent(choices.getCandidateIngredientsInDisplayGroup(value).toList())));
        return choices.getSingleDisplayGroupTagKey(() -> equivalent(allValid().toList()));
    }
    private Optional<TagKey<?>> equivalent(List<ITypedIngredient<?>> values) {
        return values.isEmpty() ? Optional.empty() : equivalent(values,values.getFirst());
    }
    private <T> Optional<TagKey<?>> equivalent(List<ITypedIngredient<?>> values, ITypedIngredient<T> first) {
        var typed=values.stream().flatMap(value -> value.getIngredient(first.getType()).stream()).toList();
        return typed.size()!=values.size() ? Optional.empty() : manager.getIngredientHelper(first.getType()).getTagKeyEquivalent(typed);
    }
    public mezz.jei.api.gui.builder.IIngredientAcceptor<?> overrideDisplay() { return choices.createDisplayOverrides(); }
    public void clearOverrides() { choices.clearDisplayOverrides(); }
}
