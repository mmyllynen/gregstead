package com.gregtechceu.gtceu.data.recipe.misc;

import com.gregtechceu.gtceu.api.block.OreBlock;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.List;

/** Additional manufacturing routes required by the port's all-item acquisition scope. */
public final class OreHostReconstitutionRecipes {
    private OreHostReconstitutionRecipes() {}

    public static void init(RecipeOutput output) {
        var ores=new ArrayList<OreBlock>();
        var sources=new IdentityHashMap<Material,List<Item>>();
        for(var block:BuiltInRegistries.BLOCK) {
            if(!(block instanceof OreBlock ore) || !BuiltInRegistries.BLOCK.getKey(block).getNamespace().equals("gtceu"))continue;
            ores.add(ore);
            sources.computeIfAbsent(ore.material,key->new ArrayList<>()).add(ore.asItem());
        }
        ores.sort(Comparator.comparing(ore->BuiltInRegistries.BLOCK.getKey(ore).toString()));
        sources.values().forEach(items->items.sort(Comparator.comparing(item->BuiltInRegistries.ITEM.getKey(item).toString())));
        for(var ore:ores) {
            var type=TagPrefix.ORES.get(ore.tagPrefix);
            if(type==null)throw new IllegalStateException("Ore has no registered host: "+ore);
            var host=type.stoneType().get().getBlock().asItem();
            if(host==Items.AIR)throw new IllegalStateException("Ore host has no item: "+ore);
            // Only actual GT ore items of this material are accepted, not broad addon/vanilla tags.
            // Every registered ore host has a multiplier of one or two. Consuming two blocks
            // for one prevents primary ore amplification even when changing to double-drop hosts;
            // byproduct opportunities decrease, and the new host is separately consumed.
            var ingredient=Ingredient.of(sources.get(ore.material).toArray(Item[]::new));
            GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder("ore_host_reconstitution/"+BuiltInRegistries.BLOCK.getKey(ore).getPath())
                .inputItems(ingredient,2).inputItems(host)
                .inputFluids(GTMaterials.Water.getFluid(250))
                .outputItems(ore.asItem()).duration(200).EUt(16).save(output);
        }
    }
}
