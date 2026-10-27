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

final class ClientGregTechDatagen {
    private ClientGregTechDatagen() {}
    private static final ProviderType<GTModelProvider> MODEL_PROVIDER = ProviderType.registerClientProvider(
            "gt_model", () -> context -> new GTModelProvider(context.parent(), context.output(),
                    context.event().getResourceManager(net.minecraft.server.packs.PackType.CLIENT_RESOURCES)));

    static void initPre() {
        DataProvider.INDENT_WIDTH.set(4);
        // replace some default providers with ours
        RegistrateDataProviderAccessor.gtceu$getTypes().forcePut("model", MODEL_PROVIDER);

        GTRegistration.REGISTRATE.addDataGenerator(ProviderType.BLOCKSTATE,
                p -> BlockstateModelLoader.init((GTBlockstateProvider) p));
    }

}
