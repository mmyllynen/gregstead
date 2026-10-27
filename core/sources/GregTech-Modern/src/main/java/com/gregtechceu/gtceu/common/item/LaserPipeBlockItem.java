package com.gregtechceu.gtceu.common.item;

import com.gregtechceu.gtceu.api.block.PipeBlock;
import com.gregtechceu.gtceu.api.item.PipeBlockItem;
import com.gregtechceu.gtceu.common.block.LaserPipeBlock;

import com.tterrag.registrate.util.RegistrateItemColors.Color;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class LaserPipeBlockItem extends PipeBlockItem {

    public LaserPipeBlockItem(PipeBlock block, Properties properties) {
        super(block, properties);
    }

    @Override
    public LaserPipeBlock getBlock() {
        return (LaserPipeBlock) super.getBlock();
    }

    @OnlyIn(Dist.CLIENT)
    public static Color tintColor() {
        return (itemStack, index) -> {
            if (itemStack.getItem() instanceof LaserPipeBlockItem materialBlockItem) {
                return -1;
            }
            return -1;
        };
    }
}
