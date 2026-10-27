package dev.emi.emi.jemi.impl;

import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import java.util.ArrayList;
import java.util.List;

public class JemiIngredientConsumer extends NativeIngredientAcceptor<JemiIngredientConsumer> {
    public final List<EmiIngredient> stacks = new ArrayList<>();
    @Override protected JemiIngredientConsumer self() { return this; }
    @Override protected void replace(List<EmiStack> values) { stacks.clear(); stacks.addAll(values); }
}
