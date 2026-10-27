package com.gregtechceu.gtceu.integration.jade;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.common.blockentity.FluidPipeBlockEntity;
import com.gregtechceu.gtceu.common.data.GTMaterialItems;
import com.gregtechceu.gtceu.integration.jade.provider.*;

import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;

import com.tterrag.registrate.util.entry.ItemProviderEntry;
import snownee.jade.addon.harvest.HarvestToolProvider;
import snownee.jade.api.harvest.ToolTier;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

import java.util.List;
import java.util.Objects;

@WailaPlugin
public class GTJadePlugin implements IWailaPlugin {

    @Override
    public void register(IWailaCommonRegistration registration) {
        registration.registerBlockDataProvider(new ElectricContainerBlockProvider(), BlockEntity.class);
        registration.registerBlockDataProvider(new WorkableBlockProvider(), BlockEntity.class);
        registration.registerBlockDataProvider(new ControllableBlockProvider(), BlockEntity.class);
        registration.registerBlockDataProvider(new RecipeLogicProvider(), BlockEntity.class);
        registration.registerBlockDataProvider(new ParallelProvider(), BlockEntity.class);
        registration.registerBlockDataProvider(new RecipeOutputProvider(), BlockEntity.class);
        registration.registerBlockDataProvider(new MultiblockStructureProvider(), BlockEntity.class);
        registration.registerBlockDataProvider(new MaintenanceBlockProvider(), BlockEntity.class);
        registration.registerBlockDataProvider(new ExhaustVentBlockProvider(), BlockEntity.class);
        registration.registerBlockDataProvider(new SteamBoilerBlockProvider(), BlockEntity.class);
        registration.registerBlockDataProvider(new AutoOutputBlockProvider(), BlockEntity.class);
        registration.registerBlockDataProvider(new CableBlockProvider(), BlockEntity.class);
        registration.registerBlockDataProvider(new MachineModeProvider(), BlockEntity.class);
        registration.registerBlockDataProvider(new StainedColorProvider(), BlockEntity.class);
        registration.registerBlockDataProvider(new HazardCleanerBlockProvider(), BlockEntity.class);
        registration.registerBlockDataProvider(new TransformerBlockProvider(), BlockEntity.class);
        registration.registerBlockDataProvider(new PrimitivePumpBlockProvider(), BlockEntity.class);
        registration.registerBlockDataProvider(new DataBankBlockProvider(), BlockEntity.class);
        registration.registerBlockDataProvider(new EnergyConverterModeProvider(), BlockEntity.class);
        registration.registerBlockDataProvider(new BatteryStorageInfoProvider(), BlockEntity.class);
        registration.registerBlockDataProvider(new LDPEndpointProvider(), BlockEntity.class);
        if (GTCEu.Mods.isAE2Loaded()) {
            registration.registerBlockDataProvider(new MEPatternBufferProvider(), BlockEntity.class);
            registration.registerBlockDataProvider(new MEPatternBufferProxyProvider(), BlockEntity.class);
        }

        registration.registerItemStorage(GTItemStorageProvider.INSTANCE, MetaMachine.class);
        registration.registerFluidStorage(GTFluidStorageProvider.INSTANCE, MetaMachine.class);
        registration.registerFluidStorage(FluidPipeStorageProvider.INSTANCE, FluidPipeBlockEntity.class);
    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerBlockComponent(new ClientGTJadeComponent(new ElectricContainerBlockProvider()), Block.class);
        registration.registerBlockComponent(new ClientGTJadeComponent(new WorkableBlockProvider()), Block.class);
        registration.registerBlockComponent(new ClientGTJadeComponent(new ControllableBlockProvider()), Block.class);
        registration.registerBlockComponent(new ClientGTJadeComponent(new RecipeLogicProvider()), Block.class);
        registration.registerBlockComponent(new ClientGTJadeComponent(new ParallelProvider()), Block.class);
        registration.registerBlockComponent(new ClientGTJadeComponent(new RecipeOutputProvider()), Block.class);
        registration.registerBlockComponent(new ClientGTJadeComponent(new MultiblockStructureProvider()), Block.class);
        registration.registerBlockComponent(new ClientGTJadeComponent(new MaintenanceBlockProvider()), Block.class);
        registration.registerBlockComponent(new ClientGTJadeComponent(new ExhaustVentBlockProvider()), Block.class);
        registration.registerBlockComponent(new ClientGTJadeComponent(new SteamBoilerBlockProvider()), Block.class);
        registration.registerBlockComponent(new ClientGTJadeComponent(new AutoOutputBlockProvider()), Block.class);
        registration.registerBlockComponent(new ClientGTJadeComponent(new CableBlockProvider()), Block.class);
        registration.registerBlockComponent(new ClientGTJadeComponent(new MachineModeProvider()), Block.class);
        registration.registerBlockComponent(new ClientGTJadeComponent(new StainedColorProvider()), Block.class);
        registration.registerBlockComponent(new ClientGTJadeComponent(new HazardCleanerBlockProvider()), Block.class);
        registration.registerBlockComponent(new ClientGTJadeComponent(new TransformerBlockProvider()), Block.class);
        registration.registerBlockComponent(new ClientGTJadeComponent(new PrimitivePumpBlockProvider()), Block.class);
        registration.registerBlockComponent(new ClientGTJadeComponent(new DataBankBlockProvider()), Block.class);
        registration.registerBlockComponent(new ClientGTJadeComponent(new LDPEndpointProvider()), Block.class);
        registration.registerBlockComponent(new ClientGTJadeComponent(new EnergyConverterModeProvider()), Block.class);
        registration.registerBlockComponent(new ClientGTJadeComponent(new BatteryStorageInfoProvider()), Block.class);
        if (GTCEu.Mods.isAE2Loaded()) {
            registration.registerBlockComponent(new ClientGTJadeComponent(new MEPatternBufferProvider()), Block.class);
            registration.registerBlockComponent(new ClientGTJadeComponent(new MEPatternBufferProxyProvider()), Block.class);
        }

        registration.registerItemStorageClient(GTItemStorageProvider.INSTANCE);
        registration.registerFluidStorageClient(GTFluidStorageProvider.INSTANCE);
        registration.registerFluidStorageClient(FluidPipeStorageProvider.INSTANCE);
        registerHarvestTools(registration);
    }

    private static void registerHarvestTools(IWailaClientRegistration registration) {
        GTMaterialItems.TOOL_ITEMS.columnMap().forEach((type, map) -> {
            if (type.toolDefinition.getTool().rules().isEmpty() || map.isEmpty()) return;

            List<Item> tools = map
                    .values()
                    .stream()
                    .filter(Objects::nonNull)
                    .filter(ItemProviderEntry::isBound)
                    .map(ItemProviderEntry::asItem)
                    .toList();
            if (tools.isEmpty()) return;
            registration.addHarvestPlugin(registry -> {
                var toolType = registry.type(GTCEu.id(type.name), true);
                tools.forEach(item -> toolType.addTier(ToolTier.item(item)));
            });
        });
    }
}
