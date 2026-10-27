package com.gregtechceu.gtceu.api.item;

import com.gregtechceu.gtceu.api.block.MaterialPipeBlock;

import com.tterrag.registrate.util.RegistrateItemColors.Color;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import org.jetbrains.annotations.NotNull;

public class MaterialPipeBlockItem extends PipeBlockItem implements com.lowdragmc.lowdraglib.transfer.name.DynamicItemName,
        com.lowdragmc.lowdraglib.transfer.name.DynamicDescription {

    public MaterialPipeBlockItem(MaterialPipeBlock block, Properties properties) {
        super(block, properties);
    }

    @Override
    @NotNull
    public MaterialPipeBlock getBlock() {
        return (MaterialPipeBlock) super.getBlock();
    }

    @OnlyIn(Dist.CLIENT)
    public static Color tintColor() {
        return (itemStack, index) -> {
            if (itemStack.getItem() instanceof MaterialPipeBlockItem materialBlockItem) {
                return materialBlockItem.getBlock().tinted(materialBlockItem.getBlock().defaultBlockState(), null, null,
                        index);
            }
            return -1;
        };
    }

    @Override
    public String dynamicDescriptionId() { return getBlock().getDescriptionId(); }

    public Component getDescription() {
        return this.getBlock().getName();
    }

    @Override
    public Component dynamicItemName(ItemStack stack) {
        return getDescription();
    }
}
