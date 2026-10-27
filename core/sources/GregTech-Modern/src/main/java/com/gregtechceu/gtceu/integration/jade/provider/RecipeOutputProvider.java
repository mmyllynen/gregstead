package com.gregtechceu.gtceu.integration.jade.provider;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.RecipeHelper;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.api.recipe.content.ContentModifier;
import com.gregtechceu.gtceu.api.recipe.ingredient.IntProviderFluidIngredient;
import com.gregtechceu.gtceu.api.recipe.ingredient.IntProviderIngredient;
import com.gregtechceu.gtceu.integration.jade.GTElementHelper;
import com.gregtechceu.gtceu.utils.codec.CodecUtils;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.FluidIngredient;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

import snownee.jade.api.BlockAccessor;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.fluid.JadeFluidObject;
import snownee.jade.util.FluidTextHelper;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class RecipeOutputProvider extends MachineTraitProvider<RecipeLogic> {

    public RecipeOutputProvider() {
        super(GTCEu.id("recipe_output_info"), RecipeLogic.TYPE);
    }

    @Override
    protected void write(CompoundTag data, BlockAccessor accessor, RecipeLogic recipeLogic) {
        if (!recipeLogic.isWorking()) {
            return;
        }
        data.putBoolean("Working", recipeLogic.isWorking());
        GTRecipe recipe = recipeLogic.getLastRecipe();
        if (recipe == null) {
            return;
        }
        int recipeTier = RecipeHelper.getPreOCRecipeEuTier(recipe);
        int chanceTier = recipeTier + recipe.ocLevel;
        var function = recipe.getType().getChanceFunction();
        var itemContents = recipe.getOutputContents(ItemRecipeCapability.CAP);
        var fluidContents = recipe.getOutputContents(FluidRecipeCapability.CAP);
        int runs = recipe.getTotalRuns();

        var ops = recipeLogic.getMachine().getLevel()
                .registryAccess().createSerializationContext(NbtOps.INSTANCE);

        ListTag itemTags = new ListTag();
        for (Content item : itemContents) {
            CompoundTag itemTag;
            SizedIngredient content = ItemRecipeCapability.CAP.of(item.content);
            if (content.ingredient().getCustomIngredient() instanceof IntProviderIngredient provider) {

                // don't roll for output but do copy for chance and batch
                IntProviderIngredient chanced = provider;
                if (item.chance < item.maxChance) {
                    double countD = (double) runs *
                            function.getBoostedChance(item, recipeTier, chanceTier) / item.maxChance;
                    chanced = ItemRecipeCapability.CAP.copyWithModifier(provider,
                            ContentModifier.multiplier(countD));
                }
                itemTag = (CompoundTag) CodecUtils.encodeMap(chanced, chanced.getType().codec(), ops)
                        .result().orElse(new CompoundTag());
            } else {
                ItemStack[] stacks = com.lowdragmc.lowdraglib.transfer.recipe.IngredientStacks.getItems(content);
                if (stacks.length == 0 || stacks[0].isEmpty()) continue;
                ItemStack stack = stacks[0];
                itemTag = ItemStack.CODEC.encodeStart(ops, stack)
                        .map(tag -> (CompoundTag) tag)
                        .getOrThrow();
                if (item.chance < item.maxChance) {
                    int count = stack.getCount();
                    double countD = (double) count * runs *
                            function.getBoostedChance(item, recipeTier, chanceTier) / item.maxChance;
                    count = Math.max(1, (int) Math.round(countD));
                    itemTag.putInt("Count", count);
                }
            }
            itemTags.add(itemTag);
        }

        if (!itemTags.isEmpty()) {
            data.put("OutputItems", itemTags);
        }

        ListTag fluidTags = new ListTag();
        for (var fluid : fluidContents) {
            FluidIngredient ingredient = FluidRecipeCapability.CAP.of(fluid.getContent()).ingredient();
            var fluidTag = new CompoundTag();
            if (ingredient instanceof IntProviderFluidIngredient provider) {
                // don't bother rolling output for nothing
                fluidTag = IntProviderFluidIngredient.CODEC.codec().encodeStart(ops, provider)
                        .map(tag -> (CompoundTag) tag)
                        .getOrThrow();
                if (fluid.chance < fluid.maxChance) {
                    double countD = (double) runs *
                            function.getBoostedChance(fluid, recipeTier, chanceTier) / fluid.maxChance;
                    provider = (IntProviderFluidIngredient) FluidRecipeCapability.CAP.copyWithModifier(provider,
                            ContentModifier.multiplier(countD));
                }
            } else {
                FluidStack[] stacks = com.lowdragmc.lowdraglib.transfer.recipe.FluidIngredientStacks.getFluids(FluidRecipeCapability.CAP.of(fluid.content));
                if (stacks.length == 0) continue;
                if (stacks[0].isEmpty()) continue;
                var stack = stacks[0];

                fluidTag = FluidStack.CODEC.encodeStart(ops, stack)
                        .map(tag -> (CompoundTag) tag)
                        .getOrThrow();
                if (fluid.chance < fluid.maxChance) {
                    // <<<<<<< HEAD
                    int amount = stack.getAmount();
                    double amountD = (double) amount * runs *
                            // =======
                            // int amount = stacks[0].getAmount();
                            // double amountD = (double) amount * runs *
                            // >>>>>>> v.7.2.0-1.20.1
                            function.getBoostedChance(fluid, recipeTier, chanceTier) / fluid.maxChance;
                    amount = Math.max(1, (int) Math.round(amountD));
                    fluidTag.putInt("Amount", amount);
                }
            }
            fluidTags.add(fluidTag);
        }

        if (!fluidTags.isEmpty()) {
            data.put("OutputFluids", fluidTags);
        }
    }

    @Override
    protected void addTooltip(CompoundTag capData, ITooltip tooltip, Player player, BlockAccessor block,
                              BlockEntity blockEntity, IPluginConfig config) {
        if (!capData.getBooleanOr("Working", false)) {
            return;
        }
        var ops = block.getLevel().registryAccess().createSerializationContext(NbtOps.INSTANCE);

        List<SizedIngredient> outputItems = new ArrayList<>();
        if (com.lowdragmc.lowdraglib.transfer.persistence.NbtCompatibility.contains(capData, "OutputItems", Tag.TAG_LIST)) {
            ListTag itemTags = com.lowdragmc.lowdraglib.transfer.persistence.NbtCompatibility.getList(capData, "OutputItems", Tag.TAG_COMPOUND);
            if (!itemTags.isEmpty()) {
                for (Tag tag : itemTags) {
                    if (tag instanceof CompoundTag tCompoundTag) {
                        if (tCompoundTag.contains("count_provider")) {
                            var ingredient = IntProviderIngredient.CODEC.codec()
                                    .parse(ops, tCompoundTag).getOrThrow();
                            outputItems.add(new SizedIngredient(ingredient.toVanilla(), 1));
                        } else {
                            ItemStack stack = ItemStack.CODEC.parse(ops, tag).getOrThrow();
                            if (!stack.isEmpty()) {
                                outputItems.add(RecipeHelper.makeSizedIngredient(stack));
                            }
                        }
                    }
                }
            }
        }
        List<SizedFluidIngredient> outputFluids = new ArrayList<>();
        if (com.lowdragmc.lowdraglib.transfer.persistence.NbtCompatibility.contains(capData, "OutputFluids", Tag.TAG_LIST)) {
            ListTag fluidTags = com.lowdragmc.lowdraglib.transfer.persistence.NbtCompatibility.getList(capData, "OutputFluids", Tag.TAG_COMPOUND);
            for (Tag tag : fluidTags) {
                if (tag instanceof CompoundTag tCompoundTag) {
                    if (tCompoundTag.contains("count_provider")) {
                        var ingredient = IntProviderFluidIngredient.CODEC.codec()
                                .parse(ops, tCompoundTag).getOrThrow();
                        outputFluids.add(new SizedFluidIngredient(ingredient, 1));
                    } else {
                        FluidStack stack = FluidStack.CODEC.parse(ops, tag).getOrThrow();
                        if (!stack.isEmpty()) {
                            outputFluids.add(RecipeHelper.makeSizedFluidIngredient(stack));
                        }
                    }
                }
            }
        }
        if (!outputItems.isEmpty() || !outputFluids.isEmpty()) {
            tooltip.add(Component.translatable("gtceu.top.recipe_output"));
        }
        addItemTooltips(tooltip, outputItems);
        addFluidTooltips(tooltip, outputFluids);
    }

    private void addItemTooltips(ITooltip iTooltip, List<SizedIngredient> outputItems) {
        for (SizedIngredient itemOutput : outputItems) {
            if (itemOutput == null || com.lowdragmc.lowdraglib.transfer.recipe.IngredientStacks.hasNoItems(itemOutput.ingredient())) {
                continue;
            }
            ItemStack item = com.lowdragmc.lowdraglib.transfer.recipe.IngredientStacks.getItems(itemOutput)[0];
            int count = item.getCount();
            item.setCount(1);

            com.gregtechceu.gtceu.integration.jade.NativeJadeProgress.addItem(iTooltip, item);
            MutableComponent text = CommonComponents.space();
            item = com.lowdragmc.lowdraglib.transfer.recipe.IngredientStacks.getItems(itemOutput)[0];
            text.append(String.valueOf(item.getCount()));
            item.setCount(1);
            text.append(Component.translatable("gtceu.gui.content.times_item",
                    getItemName(item))
                    .withStyle(ChatFormatting.WHITE));

            com.gregtechceu.gtceu.integration.jade.NativeJadeProgress.addItem(iTooltip, item);
            iTooltip.append(text);
        }
    }

    private void addFluidTooltips(ITooltip iTooltip, List<SizedFluidIngredient> outputFluids) {
        for (SizedFluidIngredient fluidOutput : outputFluids) {
            if (fluidOutput == null || com.lowdragmc.lowdraglib.transfer.recipe.FluidIngredientStacks.hasNoFluids(fluidOutput.ingredient())) {
                continue;
            }
            FluidStack fluid = com.lowdragmc.lowdraglib.transfer.recipe.FluidIngredientStacks.getFluids(fluidOutput)[0];

            GTElementHelper.addSmallFluid(iTooltip, getFluid(fluid));
            MutableComponent text = CommonComponents.space();
            if (fluidOutput.ingredient() instanceof IntProviderFluidIngredient provider) {
                text.append(Component.translatable("gtceu.gui.content.range",
                        FluidTextHelper.getMillibuckets(provider.getCountProvider().minInclusive(), true),
                        FluidTextHelper.getMillibuckets(provider.getCountProvider().maxInclusive(), true)));
            } else {
                text.append(FluidTextHelper.getMillibuckets(fluidOutput.amount(), true));
            }
            text.append(CommonComponents.space())
                    .append(getFluidName(fluid))
                    .withStyle(ChatFormatting.WHITE);

            iTooltip.append(text);
        }
    }

    private Component getItemName(ItemStack stack) {
        return stack.getDisplayName().copy().withStyle(ChatFormatting.WHITE);
    }

    private Component getFluidName(FluidStack stack) {
        return ComponentUtils.wrapInSquareBrackets(stack.getHoverName()).withStyle(ChatFormatting.WHITE);
    }

    private JadeFluidObject getFluid(FluidStack stack) {
        return JadeFluidObject.of(stack.getFluid(), stack.getAmount());
    }
}
