package fi.gregstead.mushroom;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.registries.DeferredRegister;

import com.mojang.serialization.MapCodec;

import java.util.Set;
import java.util.function.Supplier;

@Mod(MushroomDimension.ID)
public final class MushroomDimension {
    public static final String ID = "gregstead_mushroom";
    public static final ResourceKey<Level> VANILLA = ResourceKey.create(Registries.DIMENSION, id("vanilla"));

    private static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(ID);
    public static final Supplier<MushroomPortalBlock> PORTAL = BLOCKS.registerBlock(
            "portal", MushroomPortalBlock::new, () -> BlockBehaviour.Properties.ofFullCopy(Blocks.NETHER_PORTAL));

    private static final DeferredRegister<PoiType> POIS = DeferredRegister.create(Registries.POINT_OF_INTEREST_TYPE, ID);
    public static final ResourceKey<PoiType> PORTAL_POI = ResourceKey.create(Registries.POINT_OF_INTEREST_TYPE, id("portal"));
    private static final Supplier<PoiType> PORTAL_TYPE = POIS.register("portal",
            () -> new PoiType(Set.copyOf(PORTAL.get().getStateDefinition().getPossibleStates()), 0, 1));

    private static final DeferredRegister<MapCodec<? extends BiomeSource>> SOURCES =
            DeferredRegister.create(Registries.BIOME_SOURCE, ID);

    public MushroomDimension(IEventBus bus) {
        SOURCES.register("vanilla_overworld", () -> VanillaBiomeSource.CODEC);
        SOURCES.register(bus);
        BLOCKS.register(bus);
        POIS.register(bus);
    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(ID, path);
    }

    public static boolean allowsPortal(Level level) {
        return level.dimension() == Level.OVERWORLD || level.dimension() == VANILLA;
    }
}
