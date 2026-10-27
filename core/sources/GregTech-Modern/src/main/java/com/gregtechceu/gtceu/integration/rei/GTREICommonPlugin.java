package com.gregtechceu.gtceu.integration.rei;

import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.data.GTFluids;
import com.gregtechceu.gtceu.common.fluid.potion.PotionFluid;
import me.shedaniel.rei.api.common.plugins.REICommonPlugin;
import me.shedaniel.rei.api.common.entry.comparison.ItemComparatorRegistry;
import me.shedaniel.rei.api.common.entry.comparison.FluidComparatorRegistry;
import me.shedaniel.rei.forge.REIPluginCommon;

@REIPluginCommon
public class GTREICommonPlugin implements REICommonPlugin {
    @Override
    public void registerItemComparators(ItemComparatorRegistry registry) {
        registry.registerComponents(GTItems.PROGRAMMED_CIRCUIT.asItem());
        registry.registerComponents(GTItems.TURBINE_ROTOR.asItem());
    }

    @Override
    public void registerFluidComparators(FluidComparatorRegistry registry) {
        PotionFluid potionFluid = GTFluids.POTION.get();
        registry.registerNbt(potionFluid.getSource());
        registry.registerNbt(potionFluid.getFlowing());
    }

}
