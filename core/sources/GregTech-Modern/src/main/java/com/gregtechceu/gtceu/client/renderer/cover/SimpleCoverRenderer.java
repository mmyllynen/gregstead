package com.gregtechceu.gtceu.client.renderer.cover;

import com.gregtechceu.gtceu.api.cover.CoverBehavior;
import com.gregtechceu.gtceu.client.util.NativeAtlasListeners;
import com.gregtechceu.gtceu.client.util.StaticFaceBakery;

import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.model.data.ModelData;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class SimpleCoverRenderer implements ICoverRenderer {

    @OnlyIn(Dist.CLIENT)
    protected TextureAtlasSprite sprite = null;
    @OnlyIn(Dist.CLIENT)
    protected TextureAtlasSprite emissiveSprite = null;

    public SimpleCoverRenderer(Identifier texture) {
        this(texture, null);
    }

    public SimpleCoverRenderer(Identifier texture, Identifier emissiveTexture) {
        var textures=new NativeCoverTextures(texture,emissiveTexture);
        NativeAtlasListeners.register(false,net.minecraft.client.renderer.texture.TextureAtlas.LOCATION_BLOCKS,atlas -> {
            textures.reload(atlas::getSprite,atlas.getTextures()::containsKey);
            var palette=textures.palette();sprite=palette.base();emissiveSprite=palette.emissive();
        });
    }

    @OnlyIn(Dist.CLIENT)
    public void renderCover(List<BakedQuad> quads, Direction side, RandomSource rand,
                            @NotNull CoverBehavior coverBehavior, BlockPos pos, BlockAndTintGetter level,
                            @NotNull ModelData modelData, @Nullable RenderType renderType) {
        if (side == null || side == coverBehavior.attachedSide) {
            quads.add(StaticFaceBakery.bakeFace(StaticFaceBakery.COVER_OVERLAY, coverBehavior.attachedSide, sprite));
            if (emissiveSprite != null) {
                quads.add(StaticFaceBakery.bakeFace(StaticFaceBakery.COVER_OVERLAY, coverBehavior.attachedSide,
                        emissiveSprite));
            }
        }
    }
}
