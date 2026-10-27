package com.gregtechceu.gtceu.common.item;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.fluids.GTFluid;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.material.Fluid;

import org.jetbrains.annotations.Nullable;

public class GTBucketItem extends BucketItem implements com.lowdragmc.lowdraglib.transfer.name.DynamicDescription, com.lowdragmc.lowdraglib.transfer.name.DynamicItemName {

    final Material material;
    final String langKey;

    public GTBucketItem(Fluid fluid, Properties properties, Material material, String langKey) {
        super(fluid, properties);
        this.material = material;
        this.langKey = langKey;
    }

    public static int color(ItemStack itemStack, int index) {
        if (itemStack.getItem() instanceof GTBucketItem item) {
            if (index == 1) {
                return com.lowdragmc.lowdraglib.transfer.fluid.FluidColors.color(item.content);
            }
        }
        return -1;
    }

    @Override
    public String dynamicDescriptionId() {
        return "item.gtceu.bucket";
    }

    public Component getDescription() {
        Component materialName = material.getLocalizedName();
        return Component.translatable("item.gtceu.bucket", Component.translatable(this.langKey, materialName));
    }

    @Override
    public Component dynamicItemName(ItemStack stack) {
        return this.getDescription();
    }

}
