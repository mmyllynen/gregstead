package dev.emi.emi.jemi;

import com.google.common.collect.Sets;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.platform.EmiAgnos;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.helpers.IPlatformFluidHelper;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.runtime.IIngredientManager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;

public class JemiUtil {
   public static EmiIngredient getIngredient(List<ITypedIngredient<?>> ingredients) {
      return (EmiIngredient)(ingredients.isEmpty()
         ? EmiStack.EMPTY
         : EmiIngredient.of(ingredients.stream().map(JemiUtil::getStack).filter(i -> !i.isEmpty()).toList()));
   }

   public static EmiStack getStack(Object ingredient) {
      return JemiPlugin.runtime.getIngredientManager().getIngredientTypeChecked(ingredient)
          .map(type -> getStackTyped(type, ingredient)).orElse(EmiStack.EMPTY);
   }

   public static EmiStack getStack(ITypedIngredient<?> ingredient) {
      return getStackCaptured(ingredient);
   }

   private static <T> EmiStack getStackCaptured(ITypedIngredient<T> ingredient) {
      return getStackTyped(ingredient.getType(), ingredient.getIngredient());
   }

   public static EmiStack getStack(IIngredientType<?> type, Object ingredient) {
      return type.castIngredient(ingredient).map(value -> getStackCapturedType(type, value)).orElse(EmiStack.EMPTY);
   }

   private static <T> EmiStack getStackCapturedType(IIngredientType<T> type, Object value) {
      return type.castIngredient(value).map(ingredient -> getStackTyped(type, ingredient)).orElse(EmiStack.EMPTY);
   }

   private static <T> EmiStack getStackTyped(IIngredientType<T> type, T ingredient) {
      if (ingredient == null) return EmiStack.EMPTY;
      if (type == (Object) VanillaTypes.ITEM_STACK) return EmiStack.of((ItemStack) ingredient);
      if (type == getFluidType()) return EmiAgnos.createFluidStack(ingredient);
      IIngredientManager manager = JemiPlugin.runtime.getIngredientManager();
      var helper = manager.getIngredientHelper(type);
      return helper.isValidIngredient(ingredient)
          ? new JemiStack<>(type, helper, manager.getIngredientRenderer(type), ingredient) : EmiStack.EMPTY;
   }

   public static Optional<ITypedIngredient<?>> getTyped(EmiStack stack) {
      if (stack.isEmpty()) return Optional.empty();
      if (stack.getKey() instanceof Fluid fluid) return fluidTyped(getFluidHelper(), stack, fluid);
      IIngredientManager manager = JemiPlugin.runtime.getIngredientManager();
      if (stack instanceof JemiStack<?> jemi) {
         return manager.createTypedIngredient(jemi.ingredient, false).map(typed -> (ITypedIngredient<?>) typed);
      }
      return manager.createTypedIngredient(VanillaTypes.ITEM_STACK, stack.getItemStack(), false).map(typed -> (ITypedIngredient<?>) typed);
   }

   private static <T> Optional<ITypedIngredient<?>> fluidTyped(IPlatformFluidHelper<T> helper, EmiStack stack, Fluid fluid) {
      T ingredient = helper.create(fluid.builtInRegistryHolder(), stack.getAmount() == 0 ? helper.bucketVolume() : stack.getAmount(), stack.getComponentChanges());
      return JemiPlugin.runtime.getIngredientManager().createTypedIngredient(helper.getFluidIngredientType(), ingredient, false)
          .map(typed -> (ITypedIngredient<?>) typed);
   }

   public static IPlatformFluidHelper<?> getFluidHelper() {
      return JemiPlugin.runtime.getJeiHelpers().getPlatformFluidHelper();
   }

   public static IIngredientType<?> getFluidType() {
      return getFluidHelper().getFluidIngredientType();
   }

   public static Set<String> getHandledMods() {
      Set<String> set = Sets.newHashSet();

      for (String mod : EmiAgnos.getModsWithPlugins()) {
         set.add(mod);
      }

      return set;
   }
}
