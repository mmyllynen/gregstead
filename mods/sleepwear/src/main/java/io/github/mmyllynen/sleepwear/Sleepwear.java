package io.github.mmyllynen.sleepwear;

import com.mojang.serialization.Codec;
import net.minecraft.core.component.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.*;
import net.minecraft.resources.*;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.item.equipment.*;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.registries.*;

@Mod("sleepwear")
public final class Sleepwear {
  public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems("sleepwear");
  public static final DeferredRegister<DataComponentType<?>> COMPONENTS =
      DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, "sleepwear");
  public static final DeferredHolder<DataComponentType<?>, DataComponentType<String>> DESIGN =
      COMPONENTS.register(
          "design",
          () ->
              DataComponentType.<String>builder()
                  .persistent(Codec.STRING)
                  .networkSynchronized(ByteBufCodecs.STRING_UTF8)
                  .build());
  public static final DeferredItem<Item> SHIRT =
      ITEMS.registerItem(
          "pajama_shirt",
          p ->
              new Item(
                  p.stacksTo(1)
                      .component(
                          DataComponents.LORE,
                          new net.minecraft.world.item.component.ItemLore(
                              java.util.List.of(
                                  net.minecraft.network.chat.Component.translatable(
                                      "sleepwear.hint.sleep"),
                                  net.minecraft.network.chat.Component.translatable(
                                      "sleepwear.hint.dye"))))
                      .component(
                          DataComponents.EQUIPPABLE,
                          equipment(EquipmentSlot.CHEST, "solid_white_white"))));
  public static final DeferredItem<Item> PANTS =
      ITEMS.registerItem(
          "pajama_pants",
          p ->
              new Item(
                  p.stacksTo(1)
                      .component(
                          DataComponents.LORE,
                          new net.minecraft.world.item.component.ItemLore(
                              java.util.List.of(
                                  net.minecraft.network.chat.Component.translatable(
                                      "sleepwear.hint.sleep"),
                                  net.minecraft.network.chat.Component.translatable(
                                      "sleepwear.hint.dye"))))
                      .component(
                          DataComponents.EQUIPPABLE,
                          equipment(EquipmentSlot.LEGS, "solid_white_white"))));
  public static final DeferredRegister<RecipeSerializer<?>> RECIPES =
      DeferredRegister.create(Registries.RECIPE_SERIALIZER, "sleepwear");
  public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<ColourRecipe>> COLOUR =
      RECIPES.register(
          "colour",
          () ->
              new RecipeSerializer<>(
                  com.mojang.serialization.MapCodec.unit(ColourRecipe.INSTANCE),
                  StreamCodec.unit(ColourRecipe.INSTANCE)));

  public Sleepwear(IEventBus bus) {
    ITEMS.register(bus);
    COMPONENTS.register(bus);
    RECIPES.register(bus);
    SleepScheduler.register();
    bus.addListener((net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent e) -> {
      if (e.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
        e.accept(SHIRT);
        e.accept(PANTS);
      }
    });
  }

  public static Identifier id(String s) {
    return Identifier.fromNamespaceAndPath("sleepwear", s);
  }

  public static boolean isClothing(ItemStack s) {
    return s.is(SHIRT.get()) || s.is(PANTS.get());
  }

  public static Equippable equipment(EquipmentSlot slot, String design) {
    return Equippable.builder(slot)
        .setAsset(ResourceKey.create(EquipmentAssets.ROOT_ID, id(design)))
        .setDamageOnHurt(false)
        .build();
  }

  public static ItemStack colour(ItemStack s, String pattern, DyeColor a, DyeColor b) {
    ItemStack out = s.copyWithCount(1);
    String d = pattern + "_" + a.getName() + "_" + b.getName();
    out.set(DESIGN.get(), d);
    out.set(
        DataComponents.EQUIPPABLE,
        equipment(s.is(SHIRT.get()) ? EquipmentSlot.CHEST : EquipmentSlot.LEGS, d));
    out.set(DataComponents.ITEM_MODEL, id((s.is(SHIRT.get()) ? "shirt/" : "pants/") + d));
    return out;
  }
}
