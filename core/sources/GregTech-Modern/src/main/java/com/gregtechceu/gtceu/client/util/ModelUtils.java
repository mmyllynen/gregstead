package com.gregtechceu.gtceu.client.util;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.client.renderer.cover.ICoverableRenderer;


import net.minecraft.ChatFormatting;
import net.minecraft.util.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.util.RandomSource;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.AddClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.TextureAtlasStitchedEvent;
import net.neoforged.neoforge.model.data.ModelData;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@EventBusSubscriber(modid = GTCEu.MOD_ID, value = Dist.CLIENT)
public class ModelUtils {

    private ModelUtils() {}

    private static final List<EventListenerHolder> EVENT_LISTENERS = new ArrayList<>();

    public static List<BakedQuad> getBakedModelQuads(net.minecraft.client.renderer.block.dispatch.BlockStateModel model, BlockAndTintGetter level, BlockPos pos,
                                                     BlockState state, Direction side, RandomSource rand) {
        var parts=new java.util.ArrayList<net.minecraft.client.renderer.block.dispatch.BlockStateModelPart>();
        model.collectParts(level,pos,state,rand,parts);
        var quads=new java.util.ArrayList<BakedQuad>();
        for(var part:parts)quads.addAll(part.getQuads(side));
        return quads;
    }

    public static net.minecraft.client.renderer.block.dispatch.BlockStateModel getModelForState(BlockState state) {
        return Minecraft.getInstance().getModelManager().getBlockStateModelSet().get(state);
    }

    public static String getPropertyValueString(Map.Entry<Property<?>, Comparable<?>> entry) {
        Property<?> property = entry.getKey();
        Comparable<?> value = entry.getValue();

        String valueString = Util.getPropertyName(property, value);
        if (Boolean.TRUE.equals(value)) {
            valueString = ChatFormatting.GREEN + valueString;
        } else if (Boolean.FALSE.equals(value)) {
            valueString = ChatFormatting.RED + valueString;
        }

        return property.getName() + ": " + valueString;
    }

    public static void registerAtlasStitchedEventListener(boolean removeOnReload,
                                                          AssetEventListener.AtlasStitched listener) {
        NativeAtlasListeners.register(removeOnReload,null,atlas->listener.accept(new TextureAtlasStitchedEvent(atlas)));
    }

    public static void registerAtlasStitchedEventListener(boolean removeOnReload, final Identifier atlasLocation,
                                                          final AssetEventListener.AtlasStitched listener) {
        registerAtlasStitchedEventListener(removeOnReload, event -> {
            if (event.getAtlas().location().equals(atlasLocation)) {
                listener.accept(event);
            }
        });
    }

    public static void registerBakeEventListener(boolean removeOnReload,
                                                 AssetEventListener.ModifyBakingResult listener) {
        EVENT_LISTENERS.add(new EventListenerHolder(listener, removeOnReload));
    }

    public static void registerAddModelsEventListener(boolean removeOnReload,
                                                      AssetEventListener.RegisterStandalone listener) {
        EVENT_LISTENERS.add(new EventListenerHolder(listener, removeOnReload));
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void registerReloadListener(AddClientReloadListenersEvent event) {
        NativeAssetReloads.register(event,()->EVENT_LISTENERS.removeIf(EventListenerHolder::removeOnReload));
    }

    @SuppressWarnings({ "unchecked", "deprecation" })
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onAtlasStitched(TextureAtlasStitchedEvent event) {
        TextureAtlas atlas = event.getAtlas();
        NativeAtlasListeners.dispatch(atlas);
        if (atlas.location() == TextureAtlas.LOCATION_BLOCKS) {
            ICoverableRenderer.initSprites(atlas);
        }

        for (var listener : EVENT_LISTENERS) {
            Class<?> eventClass = listener.listener.eventClass();
            if (eventClass != null && eventClass.isInstance(event)) {
                ((AssetEventListener<TextureAtlasStitchedEvent>) listener.listener).accept(event);
            }
        }
    }

    @SuppressWarnings("unchecked")
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onModifyBakingResult(ModelEvent.ModifyBakingResult event) {
        for (var listener : EVENT_LISTENERS) {
            Class<?> eventClass = listener.listener.eventClass();
            if (eventClass != null && eventClass.isInstance(event)) {
                ((AssetEventListener<ModelEvent.ModifyBakingResult>) listener.listener).accept(event);
            }
        }

        // Native LDLib CTM resolves each selected part from the current mesh snapshot;
        // the removed BakedModel cache workaround is no longer applicable.

    }

    @SuppressWarnings("unchecked")
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onRegisterStandalone(ModelEvent.RegisterStandalone event) {
        for (var listener : EVENT_LISTENERS) {
            Class<?> eventClass = listener.listener.eventClass();
            if (eventClass != null && eventClass.isInstance(event)) {
                ((AssetEventListener<ModelEvent.RegisterStandalone>) listener.listener).accept(event);
            }
        }
    }

    private record EventListenerHolder(AssetEventListener<?> listener, boolean removeOnReload) {}
}
