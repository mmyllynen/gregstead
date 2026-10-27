package com.gregtechceu.gtceu.data;

import com.gregtechceu.gtceu.api.registry.registrate.provider.GTBlockstateProvider;
import com.gregtechceu.gtceu.api.registry.registrate.provider.GTModelProvider;
import com.gregtechceu.gtceu.common.registry.GTRegistration;
import com.gregtechceu.gtceu.core.mixins.registrate.RegistrateDataProviderAccessor;
import com.gregtechceu.gtceu.data.lang.LangHandler;
import com.gregtechceu.gtceu.data.model.BlockstateModelLoader;
import com.gregtechceu.gtceu.data.tags.BlockTagLoader;
import com.gregtechceu.gtceu.data.tags.EntityTypeTagLoader;
import com.gregtechceu.gtceu.data.tags.FluidTagLoader;
import com.gregtechceu.gtceu.data.tags.ItemTagLoader;

import net.minecraft.data.DataProvider;

import com.tterrag.registrate.providers.ProviderType;

public class GregTechDatagen {

    public static void initPre() {
        DataProvider.INDENT_WIDTH.set(4);
        // Model providers execute on the physical client, including the native client-data run.
        if (com.gregtechceu.gtceu.GTCEu.isClientSide()) ClientGregTechDatagen.initPre();
    }

    public static void initPost() {
        GTRegistration.REGISTRATE.addDataGenerator(ProviderType.BLOCK_TAGS, BlockTagLoader::init);
        GTRegistration.REGISTRATE.addDataGenerator(ProviderType.ITEM_TAGS, ItemTagLoader::init);
        GTRegistration.REGISTRATE.addDataGenerator(ProviderType.FLUID_TAGS, FluidTagLoader::init);
        GTRegistration.REGISTRATE.addDataGenerator(ProviderType.ENTITY_TAGS, EntityTypeTagLoader::init);
        GTRegistration.REGISTRATE.addDataGenerator(ProviderType.LANG, LangHandler::init);
    }
}
