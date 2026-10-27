package dev.emi.emi.jemi.impl;

import com.google.common.collect.Lists;
import dev.emi.emi.EmiPort;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.config.FluidUnit;
import dev.emi.emi.jemi.JemiStack;
import dev.emi.emi.jemi.JemiUtil;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import mezz.jei.api.gui.builder.IIngredientAcceptor;
import mezz.jei.api.gui.ingredient.IRecipeSlotRichTooltipCallback;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.Nullable;

public class JemiIngredientAcceptor extends NativeIngredientAcceptor<JemiIngredientAcceptor> {
   public static final Pattern FLUID_END = Pattern.compile("(^|\\s)([\\d,]+)\\s*mB$");
   public final RecipeIngredientRole role;
   public final List<EmiStack> stacks = Lists.newArrayList();

   public JemiIngredientAcceptor(RecipeIngredientRole role) {
      this.role = role;
   }

   public void coerceStacks(IRecipeSlotRichTooltipCallback tooltipCallback, Map<IIngredientType<?>, JemiRecipeSlot.IngredientRenderer<?>> renderers) {
      if (tooltipCallback != null || renderers != null) {
         Iterator var3 = this.stacks.iterator();

         while (true) {
            EmiStack stack;
            List<Component> base;
            while (true) {
               if (!var3.hasNext()) {
                  return;
               }

               stack = (EmiStack)var3.next();
               ITypedIngredient typed = JemiUtil.getTyped(stack).orElse(null);
               if (typed != null && (stack instanceof JemiStack || stack.getKey() instanceof Fluid)) {
                  base = Lists.newArrayList();
                  if (renderers != null && renderers.containsKey(typed.getType())) {
                     base.addAll(rendererTooltip(renderers.get(typed.getType()), typed.getIngredient()));
                  }

                  if (base != null && !base.isEmpty()) {
                     break;
                  }

                  if (tooltipCallback != null) {
                     base.add(stack.getName());
                     base.add(EmiPort.literal(""));
                     break;
                  }
               }
            }

            if (tooltipCallback != null) {
               JemiRecipeSlot jsr = new JemiRecipeSlot(this.role, stack);
               JemiTooltipBuilder tooltip = new JemiTooltipBuilder();
               tooltip.addAll(base);
               tooltipCallback.onRichTooltip(jsr, tooltip);
               base = tooltip.getLines().stream().flatMap(line -> line.left().stream())
                   .map(text -> text instanceof Component component ? component : Component.literal(text.getString())).toList();
            }

            for (int i = 0; i < 2 && i < base.size(); i++) {
               Component t = base.get(i);
               if (t != null) {
                  Matcher m = FLUID_END.matcher(t.getString());
                  if (m.find()) {
                     long amount = Long.parseLong(m.group(2).replace(",", ""));
                     if (amount != stack.getAmount()) {
                        stack.setAmount(amount);
                     }
                  }
               }
            }
         }
      }
   }

   public EmiIngredient build() {
      return EmiIngredient.of(this.stacks);
   }

   private static <T> List<Component> rendererTooltip(JemiRecipeSlot.IngredientRenderer<T> renderer, Object ingredient) {
      return renderer.type().castIngredient(ingredient)
          .map(value -> renderer.renderer().getTooltip(value, TooltipFlag.NORMAL)).orElse(List.of());
   }

   @Override protected JemiIngredientAcceptor self() { return this; }
   @Override protected void replace(List<EmiStack> values) { stacks.clear(); stacks.addAll(values); }
   @Override protected RecipeIngredientRole role() { return role; }
}
