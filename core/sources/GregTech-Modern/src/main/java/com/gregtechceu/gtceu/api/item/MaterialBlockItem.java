package com.gregtechceu.gtceu.api.item;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.block.MaterialBlock;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.DustProperty;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;

import com.lowdragmc.lowdraglib.client.renderer.IBlockRendererProvider;
import com.lowdragmc.lowdraglib.client.renderer.IItemRendererProvider;
import com.lowdragmc.lowdraglib.client.renderer.IRenderer;

import com.tterrag.registrate.util.RegistrateItemColors.Color;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Block;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MaterialBlockItem extends BlockItem implements IItemRendererProvider, com.lowdragmc.lowdraglib.transfer.name.DynamicDescription, com.lowdragmc.lowdraglib.transfer.name.DynamicItemName {

    public final TagPrefix tagPrefix;
    public final Material material;

    public MaterialBlockItem(Block block, Properties properties, TagPrefix tagPrefix, Material material) {
        super(block, properties);
        this.tagPrefix = tagPrefix;
        this.material = material;
    }

    @Override
    @NotNull
    public MaterialBlock getBlock() {
        return (MaterialBlock) super.getBlock();
    }

    @OnlyIn(Dist.CLIENT)
    public static Color tintColor(Material material) {
        return (itemStack, index) -> material.getLayerARGB(index);
    }

    @Nullable
    @Override
    @OnlyIn(Dist.CLIENT)
    public IRenderer getRenderer(ItemStack stack) {
        if (getBlock() instanceof IBlockRendererProvider provider) {
            return provider.getRenderer(getBlock().defaultBlockState());
        }
        return null;
    }

    @Override
    public String dynamicDescriptionId() {
        return getBlock().getDescriptionId();
    }

    public Component getDescription() {
        return getBlock().getName();
    }

    @Override
    public Component dynamicItemName(ItemStack stack) {
        return getDescription();
    }

    public int getItemBurnTime() {
        return TagPrefixItem.materialBurnTime(tagPrefix, material);
    }
}
