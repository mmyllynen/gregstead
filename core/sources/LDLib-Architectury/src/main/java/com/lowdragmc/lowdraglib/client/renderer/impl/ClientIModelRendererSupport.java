package com.lowdragmc.lowdraglib.client.renderer.impl;

import com.lowdragmc.lowdraglib.LDLib;
import com.lowdragmc.lowdraglib.client.model.ModelFactory;
import com.lowdragmc.lowdraglib.client.model.NativeModelCache;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.block.dispatch.ModelState;
import net.minecraft.client.renderer.block.dispatch.BlockModelRotation;
import com.lowdragmc.lowdraglib.client.renderer.IBlockRendererProvider;
import com.lowdragmc.lowdraglib.client.renderer.IRenderer;
import com.lowdragmc.lowdraglib.editor.ColorPattern;
import com.lowdragmc.lowdraglib.editor.annotation.ConfigSetter;
import com.lowdragmc.lowdraglib.editor.annotation.Configurable;
import com.lowdragmc.lowdraglib.registry.annotation.LDLRegisterClient;
import com.lowdragmc.lowdraglib.editor.configurator.ConfiguratorGroup;
import com.lowdragmc.lowdraglib.editor.configurator.StringConfigurator;
import com.lowdragmc.lowdraglib.editor.configurator.WrapperConfigurator;
import com.lowdragmc.lowdraglib.editor.ui.Editor;
import com.lowdragmc.lowdraglib.gui.texture.GuiTextureGroup;
import com.lowdragmc.lowdraglib.gui.texture.TextTexture;
import com.lowdragmc.lowdraglib.gui.widget.ButtonWidget;
import com.lowdragmc.lowdraglib.gui.widget.DialogWidget;
import lombok.Getter;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import java.util.Set;
import net.neoforged.neoforge.model.data.ModelData;
import net.minecraft.util.TriState;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

final class ClientIModelRendererSupport {
    private ClientIModelRendererSupport() {}
    static net.minecraft.client.renderer.item.ItemModel getNativeItemModel(IModelRenderer renderer, ItemStack stack) {
        if(renderer.nativeCache==null)renderer.nativeCache=new NativeModelCache(renderer.modelLocation);
        if(renderer.nativeItemModel==null)renderer.nativeItemModel=new com.lowdragmc.lowdraglib.client.model.NativeCachedItemModel(renderer.nativeCache,List.of());
        return renderer.nativeItemModel;
    
    }
    static BlockStateModelPart getBlockBakedModel(IModelRenderer renderer, BlockAndTintGetter level, BlockPos pos, BlockState state) {
        if(renderer.nativeCache==null)renderer.nativeCache=new NativeModelCache(renderer.modelLocation);
        ModelState rotation=BlockModelRotation.IDENTITY;
        if(level!=null && pos!=null && state!=null && state.getBlock() instanceof IBlockRendererProvider provider) {
            var supplied=provider.getModelState(level,pos,state);if(supplied!=null)rotation=supplied;
        }
        return renderer.nativeCache.block(rotation);
    }
}
