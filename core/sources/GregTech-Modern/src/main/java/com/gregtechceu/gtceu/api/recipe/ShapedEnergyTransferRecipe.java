package com.gregtechceu.gtceu.api.recipe;

import com.gregtechceu.gtceu.api.capability.IElectricItem;
import com.gregtechceu.gtceu.api.item.datacomponents.SimpleEnergyContent;
import com.lowdragmc.lowdraglib.transfer.recipe.IngredientStacks;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.ShapedCraftingRecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Arrays;
import java.util.List;

public class ShapedEnergyTransferRecipe extends ShapedRecipe {
    private final Ingredient chargeIngredient;
    private final boolean transferMaxCharge;
    private final boolean overrideCharge;
    private final ItemStackTemplate resultTemplate;

    public ShapedEnergyTransferRecipe(Recipe.CommonInfo commonInfo, CraftingRecipe.CraftingBookInfo bookInfo,
                                      ShapedRecipePattern pattern, Ingredient chargeIngredient,
                                      boolean overrideCharge, boolean transferMaxCharge, ItemStackTemplate result) {
        super(commonInfo, bookInfo, pattern, result);
        this.chargeIngredient = chargeIngredient;
        this.transferMaxCharge = transferMaxCharge;
        this.overrideCharge = overrideCharge;
        this.resultTemplate = result;
    }

    public ShapedEnergyTransferRecipe(String group, CraftingBookCategory category, ShapedRecipePattern pattern,
                                      Ingredient chargeIngredient, boolean overrideCharge, boolean transferMaxCharge,
                                      ItemStack result, boolean showNotification) {
        this(new Recipe.CommonInfo(showNotification), new CraftingRecipe.CraftingBookInfo(category, group),
                pattern, chargeIngredient, overrideCharge, transferMaxCharge, ItemStackTemplate.fromNonEmptyStack(result));
    }

    public Ingredient getChargeIngredient() { return chargeIngredient; }
    public boolean isTransferMaxCharge() { return transferMaxCharge; }
    public boolean isOverrideCharge() { return overrideCharge; }

    @Override
    public ItemStack assemble(CraftingInput input) {
        return transferFirstCharge(super.assemble(input), input.items());
    }

    private ItemStack transferFirstCharge(ItemStack result, List<ItemStack> inputs) {
        // Preserve upstream's first source selection, rather than aggregating multiple batteries.
        for (var representative : IngredientStacks.getItems(chargeIngredient)) {
            for (var input : inputs) {
                if (!ItemStack.isSameItem(input, representative)) continue;
                // Electric item capability construction may initialize components. Never query the original crafting stack.
                var electric = input.copy().getCapability(IElectricItem.CAPABILITY);
                if (electric != null) {
                    result.set(SimpleEnergyContent.TYPE, new SimpleEnergyContent(electric.getMaxCharge(), electric.getCharge()));
                    return result;
                }
            }
        }
        return result;
    }

    public ItemStack getPreviewResult() {
        return transferFirstCharge(resultTemplate.create(), Arrays.asList(IngredientStacks.getItems(chargeIngredient)));
    }

    @Override
    public List<RecipeDisplay> display() {
        return List.of(new ShapedCraftingRecipeDisplay(getWidth(), getHeight(),
                pattern.ingredients().stream().map(value -> value.map(Ingredient::display).orElse(SlotDisplay.Empty.INSTANCE)).toList(),
                new SlotDisplay.ItemStackSlotDisplay(ItemStackTemplate.fromNonEmptyStack(getPreviewResult())),
                new SlotDisplay.ItemSlotDisplay(Items.CRAFTING_TABLE)));
    }

    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public RecipeSerializer getSerializer() { return Serializer.INSTANCE; }

    public static final class Serializer {
        private Serializer() {}
        public static final MapCodec<ShapedEnergyTransferRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Recipe.CommonInfo.MAP_CODEC.forGetter(recipe -> recipe.commonInfo),
                CraftingRecipe.CraftingBookInfo.MAP_CODEC.forGetter(recipe -> recipe.bookInfo),
                ShapedRecipePattern.MAP_CODEC.forGetter(recipe -> recipe.pattern),
                Ingredient.CODEC.fieldOf("chargeIngredient").forGetter(ShapedEnergyTransferRecipe::getChargeIngredient),
                Codec.BOOL.fieldOf("overrideCharge").forGetter(ShapedEnergyTransferRecipe::isOverrideCharge),
                Codec.BOOL.fieldOf("transferMaxCharge").forGetter(ShapedEnergyTransferRecipe::isTransferMaxCharge),
                ItemStackTemplate.CODEC.fieldOf("result").forGetter(recipe -> recipe.resultTemplate))
                .apply(instance, ShapedEnergyTransferRecipe::new));
        public static final StreamCodec<RegistryFriendlyByteBuf, ShapedEnergyTransferRecipe> STREAM_CODEC = StreamCodec.of(
                (buffer, recipe) -> {
                    Recipe.CommonInfo.STREAM_CODEC.encode(buffer, recipe.commonInfo);
                    CraftingRecipe.CraftingBookInfo.STREAM_CODEC.encode(buffer, recipe.bookInfo);
                    ShapedRecipePattern.STREAM_CODEC.encode(buffer, recipe.pattern);
                    Ingredient.CONTENTS_STREAM_CODEC.encode(buffer, recipe.chargeIngredient);
                    buffer.writeBoolean(recipe.overrideCharge);
                    buffer.writeBoolean(recipe.transferMaxCharge);
                    ItemStackTemplate.STREAM_CODEC.encode(buffer, recipe.resultTemplate);
                }, buffer -> new ShapedEnergyTransferRecipe(
                        Recipe.CommonInfo.STREAM_CODEC.decode(buffer),
                        CraftingRecipe.CraftingBookInfo.STREAM_CODEC.decode(buffer),
                        ShapedRecipePattern.STREAM_CODEC.decode(buffer),
                        Ingredient.CONTENTS_STREAM_CODEC.decode(buffer), buffer.readBoolean(), buffer.readBoolean(),
                        ItemStackTemplate.STREAM_CODEC.decode(buffer)));
        public static final RecipeSerializer<ShapedEnergyTransferRecipe> INSTANCE = new RecipeSerializer<>(CODEC, STREAM_CODEC);
    }
}
