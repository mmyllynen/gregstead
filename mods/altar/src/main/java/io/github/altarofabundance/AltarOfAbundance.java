package io.github.altarofabundance;

import java.util.List;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

@Mod(AltarOfAbundance.MOD_ID)
public final class AltarOfAbundance {
    public static final String MOD_ID = "altarofabundance";
    public static final TagKey<Item> MATERIALS = TagKey.create(Registries.ITEM, id("duplicable_materials"));
    public static final TagKey<Item> OFFERINGS = TagKey.create(Registries.ITEM, id("offerings"));
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MOD_ID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MOD_ID);
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, MOD_ID);
    private static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MOD_ID);

    public static final DeferredBlock<PedestalBlock> MATERIAL = pedestal("material_pedestal", PedestalBlock.Role.MATERIAL);
    public static final DeferredBlock<PedestalBlock> OFFERING = pedestal("offering_pedestal", PedestalBlock.Role.OFFERING);
    public static final DeferredBlock<PedestalBlock> OUTPUT = pedestal("output_pedestal", PedestalBlock.Role.OUTPUT);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<PedestalBlockEntity>> PEDESTAL = BLOCK_ENTITIES.register(
        "pedestal", () -> new BlockEntityType<>(PedestalBlockEntity::new, MATERIAL.get(), OFFERING.get(), OUTPUT.get()));

    static {
        TABS.register("altar", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.altarofabundance"))
            .icon(() -> OFFERING.asItem().getDefaultInstance())
            .displayItems((parameters, output) -> {
                output.accept(MATERIAL.get());
                output.accept(OFFERING.get());
                output.accept(OUTPUT.get());
            }).build());
    }

    public AltarOfAbundance(IEventBus bus) {
        BLOCKS.register(bus);
        ITEMS.register(bus);
        BLOCK_ENTITIES.register(bus);
        TABS.register(bus);
    }

    private static DeferredBlock<PedestalBlock> pedestal(String name, PedestalBlock.Role role) {
        DeferredBlock<PedestalBlock> block = BLOCKS.registerBlock(name, properties -> new PedestalBlock(role, properties
            .mapColor(MapColor.COLOR_BLACK).strength(3.5F, 6.0F).requiresCorrectToolForDrops()
            .noOcclusion().pushReaction(PushReaction.IMMOVEABLE)));
        ITEMS.registerItem(name, properties -> new BlockItem(block.get(), properties.component(DataComponents.LORE,
            new ItemLore(List.of(Component.translatable("tooltip.altarofabundance." + name),
                Component.translatable("tooltip.altarofabundance.layout"))))));
        return block;
    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }
}
