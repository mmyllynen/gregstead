package com.gregtechceu.gtceu.common.data;

import com.gregtechceu.gtceu.api.data.DimensionMarker;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.common.recipe.condition.DimensionCondition;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

import com.tterrag.registrate.util.entry.BlockEntry;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

import static com.gregtechceu.gtceu.common.registry.GTRegistration.REGISTRATE;

@SuppressWarnings("unused")
public class GTDimensionMarkers {

    static {
        REGISTRATE.resetCreativeModeTab();
    }

    public static final BlockEntry<Block> OVERWORLD_MARKER = createMarker("overworld");
    public static final BlockEntry<Block> NETHER_MARKER = createMarker("the_nether");
    public static final BlockEntry<Block> END_MARKER = createMarker("the_end");

    public static final DimensionMarker OVERWORLD = createAndRegister(Level.OVERWORLD, 0,
            () -> OVERWORLD_MARKER, null);
    public static final DimensionMarker NETHER = createAndRegister(Level.NETHER, 0,
            () -> NETHER_MARKER, null);
    public static final DimensionMarker END = createAndRegister(Level.END, 0,
            () -> END_MARKER, null);

    public static DimensionMarker createAndRegister(ResourceKey<Level> dimension, int tier, Supplier<ItemLike> supplier,
                                                    @Nullable Component overrideName) {
        if (overrideName == null) {
            // if a special name hasn't been set, use the dimension's 'translated' name as a default
            overrideName = DimensionCondition.getDimensionName(dimension);
        }

        DimensionMarker marker = new DimensionMarker(tier, supplier, overrideName);
        GTRegistries.register(GTRegistries.DIMENSION_MARKERS, dimension.identifier(), marker);
        return marker;
    }

    private static BlockEntry<Block> createMarker(String name) {
        return REGISTRATE.block("%s_marker".formatted(name), Block::new)
                .lang(FormattingUtil.toEnglishName(name))
                .blockstate(() -> (ctx, prov) -> {
                    var model = prov.withParent(net.minecraft.client.data.models.model.ModelTemplates.CUBE)
                            .guiLight(net.minecraft.client.resources.model.UnbakedModel.GuiLight.FRONT);
                    var slots = new net.minecraft.client.data.models.model.TextureSlot[] {
                            net.minecraft.client.data.models.model.TextureSlot.DOWN,
                            net.minecraft.client.data.models.model.TextureSlot.UP,
                            net.minecraft.client.data.models.model.TextureSlot.NORTH,
                            net.minecraft.client.data.models.model.TextureSlot.SOUTH,
                            net.minecraft.client.data.models.model.TextureSlot.EAST,
                            net.minecraft.client.data.models.model.TextureSlot.WEST };
                    for (var slot : slots) {
                        model.texture(slot, new net.minecraft.client.resources.model.sprite.Material(
                                prov.modLoc("block/dim_markers/%s/%s".formatted(name, slot.getId()))));
                    }
                    model.texture(net.minecraft.client.data.models.model.TextureSlot.PARTICLE,
                            new net.minecraft.client.resources.model.sprite.Material(
                                    prov.modLoc("block/dim_markers/%s/north".formatted(name))));
                    prov.create(ctx.get(), model.build(ctx.get()));
                })
                .simpleItem()
                .register();
    }

    public static void init() {}
}
