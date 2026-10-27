package dev.emi.emi.jemi;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.stack.serializer.EmiIngredientSerializer;
import java.util.Optional;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.runtime.IIngredientManager;
import net.minecraft.util.GsonHelper;

public class JemiStackSerializer implements EmiIngredientSerializer<JemiStack> {
   private final IIngredientManager manager;
   private final com.mojang.serialization.Codec<mezz.jei.api.ingredients.ITypedIngredient<?>> codec;
   private final net.minecraft.core.RegistryAccess registries;
   private final IIngredientType<?> fluidType;

   public JemiStackSerializer(IIngredientManager manager) {
      this.manager = manager;
      this.codec=JemiPlugin.runtime.getJeiHelpers().getCodecHelper().getTypedIngredientCodec().codec();
      this.registries=java.util.Objects.requireNonNull(net.minecraft.client.Minecraft.getInstance().level).registryAccess();
      this.fluidType=JemiUtil.getFluidType();
   }

   public String getType() {
      return "jemi";
   }

   public EmiStack create(String uid,long amount) {
      for(var type:manager.getRegisteredIngredientTypes()) {
          if(type!=VanillaTypes.ITEM_STACK && type!=fluidType) {
              var match=legacyMatch(type,uid);
              if(match.isPresent()) return match.get().setAmount(amount);
          }
      }
      dev.emi.emi.runtime.EmiLog.warn("Unable to resolve legacy JEMI UID; native object UIDs require codec persistence: "+uid);
      return EmiStack.EMPTY;
   }
   private <T> Optional<EmiStack> legacyMatch(IIngredientType<T> type,String uid) {
      var helper=manager.getIngredientHelper(type);
      return manager.getAllIngredients(type).stream().filter(value -> uid.equals(helper.getUid(value,mezz.jei.api.ingredients.subtypes.UidContext.Ingredient)))
          .findFirst().map(value -> JemiUtil.getStack(type,value));
   }
   private NativeJemiPersistence.Entry decode(JsonElement value) {
      return NativeJemiPersistence.decode(codec,registries,value);
   }

   public EmiIngredient deserialize(JsonElement element) {
      JsonObject json = element.getAsJsonObject();
      long amount = GsonHelper.getAsLong(json, "amount", 1L);
      float chance = GsonHelper.getAsFloat(json, "chance", 1.0F);
      EmiStack remainder = EmiStack.EMPTY;
      if (GsonHelper.isValidNode(json, "remainder") && EmiIngredientSerializer.getDeserialized(json.get("remainder")) instanceof EmiStack stack) {
         remainder = stack;
      }

      EmiStack stack;
      if(json.has("ingredient")) {
          var restored=decode(json.get("ingredient"));
          stack=JemiUtil.getStack(restored.ingredient()).setAmount(restored.amount());
      } else stack=create(GsonHelper.getAsString(json,"uid"),amount);
      if (chance != 1.0F) {
         stack.setChance(chance);
      }

      if (!remainder.isEmpty()) {
         stack.setRemainder(remainder);
      }

      return stack;
   }

   private <T> mezz.jei.api.ingredients.ITypedIngredient<T> typed(JemiStack<T> stack) {
      return manager.createTypedIngredient(stack.ingredient,false).orElseThrow();
   }

   public JsonElement serialize(JemiStack stack) {
      JsonObject json = new JsonObject();
      json.addProperty("type", this.getType());
      var typed=typed(stack);
      json.add("ingredient",NativeJemiPersistence.encode(codec,registries,typed,stack.getAmount()));
      if (stack.getChance() != 1.0F) {
         json.addProperty("chance", stack.getChance());
      }

      if (!stack.getRemainder().isEmpty()) {
         EmiStack remainder = stack.getRemainder();
         if (!remainder.getRemainder().isEmpty()) {
            remainder = remainder.copy().setRemainder(EmiStack.EMPTY);
         }

         if (remainder.getRemainder().isEmpty()) {
            JsonElement remainderElement = EmiIngredientSerializer.getSerialized(remainder);
            if (remainderElement != null) {
               json.add("remainder", remainderElement);
            }
         }
      }

      return json;
   }
}
