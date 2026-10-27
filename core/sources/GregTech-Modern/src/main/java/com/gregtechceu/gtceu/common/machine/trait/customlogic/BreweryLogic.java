package com.gregtechceu.gtceu.common.machine.trait.customlogic;
import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.capability.recipe.*;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.trait.RecipeHandlerList;
import com.gregtechceu.gtceu.api.recipe.*;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.common.fluid.potion.PotionFluidHelper;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.*;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import java.util.*;
import org.jetbrains.annotations.Nullable;

/** Brewery conversion uses current native brewing recipes, preserving distinct input groups. */
public enum BreweryLogic implements GTRecipeType.ICustomRecipeLogic {
    INSTANCE;
    private static Collection<RecipeHolder<BrewingRecipe>> recipes() {
        if(GTCEu.isClientThread())return com.lowdragmc.lowdraglib.client.NativeRecipeCatalog.all().stream()
                .filter(holder->holder.value() instanceof BrewingRecipe)
                .map(holder->new RecipeHolder<>(holder.id(),(BrewingRecipe)holder.value())).toList();
        var server=GTCEu.getMinecraftServer();
        return server==null?List.of():server.getRecipeManager().recipeMap().byType(RecipeType.BREWING);
    }
    @Override public @Nullable GTRecipe createCustomRecipe(IRecipeCapabilityHolder holder) {
        var allItems=new ArrayList<IRecipeHandler<?>>();var allFluids=new ArrayList<IRecipeHandler<?>>();
        for(var handlers:holder.getCapabilitiesForIO(IO.IN)) {
            if(handlers.isDistinct()) {
                var items=new ArrayList<ItemStack>();var fluids=new ArrayList<FluidStack>();
                if(collect(handlers,items,fluids)){var recipe=match(items,fluids);if(recipe!=null)return recipe;}
            } else {allItems.addAll(handlers.getCapability(ItemRecipeCapability.CAP));allFluids.addAll(handlers.getCapability(FluidRecipeCapability.CAP));}
        }
        var items=new ArrayList<ItemStack>();var fluids=new ArrayList<FluidStack>();
        return collect(allItems,allFluids,items,fluids)?match(items,fluids):null;
    }
    private static ItemStack potion(FluidStack fluid) {
        if(fluid.getFluid()==Fluids.WATER || fluid.is(com.gregtechceu.gtceu.utils.TagUtil.createFluidTag("water")))return PotionContents.createItemStack(Items.POTION,Potions.WATER);
        if(!fluid.has(DataComponents.POTION_CONTENTS))return ItemStack.EMPTY;
        var stack=new ItemStack(Items.POTION);stack.set(DataComponents.POTION_CONTENTS,fluid.get(DataComponents.POTION_CONTENTS));return stack;
    }
    private static @Nullable GTRecipe match(List<ItemStack> items,List<FluidStack> fluids) {
        for(var recipe:recipes())for(var reagent:items)for(var fluid:fluids) {
            if(fluid.getAmount()<PotionFluidHelper.MB_PER_RECIPE)continue;
            var input=potion(fluid);if(input.isEmpty())continue;
            var output=NativeBrewingMatch.output(recipe.value(),input,reagent);
            if(!output.isEmpty()){var converted=convert(recipe,input,reagent,fluid,output,false);if(converted!=null)return converted;}
        }
        return null;
    }
    private static @Nullable GTRecipe convert(RecipeHolder<BrewingRecipe> source,ItemStack input,ItemStack reagent,FluidStack fluid,ItemStack output,boolean representative) {
        var result=PotionFluidHelper.getFluidFromPotionItem(output,PotionFluidHelper.MB_PER_RECIPE);if(result.isEmpty())return null;
        var registries=GTCEu.isClientThread()?net.minecraft.client.Minecraft.getInstance().level.registryAccess():GTCEu.getMinecraftServer().registryAccess();
        var ops=registries.createSerializationContext(com.mojang.serialization.JsonOps.INSTANCE);
        String inputKey=ItemStack.CODEC.encodeStart(ops,input.copyWithCount(1)).getOrThrow().toString();
        String reagentKey=ItemStack.CODEC.encodeStart(ops,reagent.copyWithCount(1)).getOrThrow().toString();
        String suffix;
        try {suffix=java.util.HexFormat.of().formatHex(java.security.MessageDigest.getInstance("SHA-256")
                .digest((inputKey+"\n"+reagentKey).getBytes(java.nio.charset.StandardCharsets.UTF_8)));}
        catch(java.security.NoSuchAlgorithmException failure){throw new IllegalStateException(failure);}
        var builder=GTRecipeTypes.BREWING_RECIPES.recipeBuilder("potion_native/"+source.id().identifier().getNamespace()+"/"+source.id().identifier().getPath()+"/"+suffix)
                .inputItems(net.neoforged.neoforge.common.crafting.DataComponentIngredient.of(true,reagent.copyWithCount(1)))
                .inputFluids(fluid.copyWithAmount(PotionFluidHelper.MB_PER_RECIPE)).outputFluids(result).duration(400)
                .EUt(com.gregtechceu.gtceu.api.GTValues.VHA[com.gregtechceu.gtceu.api.GTValues.MV]);
        var recipe=builder.build();if(representative)recipe.setId(recipe.getId().withPrefix("/"));return recipe;
    }
    private static boolean collect(RecipeHandlerList rhl, List<ItemStack> itemStacks, List<FluidStack> fluidStacks) {
        return collect(rhl.getCapability(ItemRecipeCapability.CAP),
                rhl.getCapability(FluidRecipeCapability.CAP),
                itemStacks, fluidStacks);
    }

    private static boolean collect(List<IRecipeHandler<?>> itemHandlers, List<IRecipeHandler<?>> fluidHandlers,
                                   List<ItemStack> itemStacks, List<FluidStack> fluidStacks) {
        for (var handler : itemHandlers) {
            for (var content : handler.getContents()) {
                if (content instanceof ItemStack stack && !stack.isEmpty()) {
                    itemStacks.add(stack);
                }
            }
        }

        for (var handler : fluidHandlers) {
            for (var content : handler.getContents()) {
                if (content instanceof FluidStack stack && !stack.isEmpty()) {
                    fluidStacks.add(stack);
                }
            }
        }

        return !(itemStacks.isEmpty() || fluidStacks.isEmpty());
    }

    @Override public void buildRepresentativeRecipes() {
        var server=GTCEu.getMinecraftServer();
        var registries=GTCEu.isClientThread()?net.minecraft.client.Minecraft.getInstance().level.registryAccess():server.registryAccess();
        for(var source:recipes())for(var input:NativeBrewingMatch.choices(source.value().getInput(),registries)) {
            if(!input.is(Items.POTION))continue;
            var fluid=PotionFluidHelper.getFluidFromPotionItem(input,PotionFluidHelper.MB_PER_RECIPE);if(fluid.isEmpty())continue;
            for(var reagent:NativeBrewingMatch.choices(source.value().getReagent(),registries)) {
                var output=NativeBrewingMatch.output(source.value(),input,reagent);if(output.isEmpty())continue;
                var converted=convert(source,input,reagent,fluid,output,true);
                if(converted!=null)GTRecipeTypes.BREWING_RECIPES.addToMainCategory(converted);
            }
        }
    }
}
