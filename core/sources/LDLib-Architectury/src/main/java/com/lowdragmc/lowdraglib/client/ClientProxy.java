package com.lowdragmc.lowdraglib.client;

import net.minecraft.client.resources.model.sprite.Material;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.lowdragmc.lowdraglib.CommonProxy;
import com.lowdragmc.lowdraglib.LDLib;
import com.lowdragmc.lowdraglib.Platform;
import com.lowdragmc.lowdraglib.client.model.custommodel.CustomBakedModel;
import com.lowdragmc.lowdraglib.client.model.custommodel.LDLMetadataSection;
import com.lowdragmc.lowdraglib.client.model.forge.LDLRendererModel;
import com.lowdragmc.lowdraglib.client.renderer.ATESRRendererProvider;
import com.lowdragmc.lowdraglib.client.renderer.IRenderer;
import com.lowdragmc.lowdraglib.client.shader.Shaders;
import com.lowdragmc.lowdraglib.client.utils.WidgetClientTooltipComponent;
import com.lowdragmc.lowdraglib.core.mixins.ParticleEngineAccessor;
import com.lowdragmc.lowdraglib.core.mixins.accessor.ModelBakeryAccessor;
import com.lowdragmc.lowdraglib.compass.CompassManager;
import com.lowdragmc.lowdraglib.gui.util.DrawerHelper;
import com.lowdragmc.lowdraglib.gui.util.WidgetTooltipComponent;
import it.unimi.dsi.fastutil.objects.Object2BooleanLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.client.resources.model.*;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterClientTooltipComponentFactoriesEvent;
import org.jetbrains.annotations.ApiStatus;

import java.util.*;

@OnlyIn(Dist.CLIENT)
public class ClientProxy extends CommonProxy {

    public ClientProxy(IEventBus eventBus) {
        super(eventBus);
        eventBus.register(this);
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(this::resourcesFinished);
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(NativeRecipeCatalog::receive);
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener((net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent.LoggingOut event) -> NativeRecipeCatalog.clear());
    }


    /**
     * should be called when Minecraft is prepared.
     */
    public static void init() {
        Shaders.reload();
        CompassManager.INSTANCE.init();
    }

    @SubscribeEvent
    public void onRegisterClientTooltipComponentFactoriesEvent(final RegisterClientTooltipComponentFactoriesEvent event) {
        event.register(WidgetTooltipComponent.class, WidgetClientTooltipComponent::new);
    }

    @SubscribeEvent
    public void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        if (Platform.isDevEnv()) {
            event.registerBlockEntityRenderer(CommonProxy.TEST_BE_TYPE.get(), ATESRRendererProvider::new);
        }
    }

    @SubscribeEvent
    public void registerNativeResources(net.neoforged.neoforge.client.event.AddClientReloadListenersEvent event) {
        event.addListener(LDLib.id("compass"), CompassManager.INSTANCE);
    }

    public void resourcesFinished(net.neoforged.neoforge.client.event.ClientResourceLoadFinishedEvent event) {
        ClientProxy.init();
    }

    @ApiStatus.Internal
    public static final Multimap<Identifier, Material> SCRAPED_TEXTURES = com.lowdragmc.lowdraglib.client.model.NativeMaterialUsage.MATERIALS;
    @ApiStatus.Internal
    public static void textureScraped(Identifier modelLocation, Material material) {
        SCRAPED_TEXTURES.put(modelLocation, material);
    }

    @SubscribeEvent
    public void modelRegistry(final ModelEvent.RegisterLoaders e) {
        e.register(LDLib.id("renderer"), LDLRendererModel.Loader.INSTANCE);
    }

    @SubscribeEvent
    public void modelBake(final ModelEvent.ModifyBakingResult event) {
        com.lowdragmc.lowdraglib.client.model.NativeRendererModels.modify(event);
        com.lowdragmc.lowdraglib.client.model.custommodel.NativeCtmRegistration.modify(event);
    }

    @SubscribeEvent
    public void guiPipelineRegistry(net.neoforged.neoforge.client.event.RegisterRenderPipelinesEvent event) {
        event.registerPipeline(com.lowdragmc.lowdraglib.client.shader.NativeHsbPipeline.PIPELINE);
        event.registerPipeline(com.lowdragmc.lowdraglib.client.shader.NativeCompassLineState.PIPELINE);
        event.registerPipeline(com.lowdragmc.lowdraglib.client.shader.LDLibRenderTypes.COLOR_PIPELINE);
        event.registerPipeline(com.lowdragmc.lowdraglib.client.shader.LDLibRenderTypes.LINE_PIPELINE);
        event.registerPipeline(com.lowdragmc.lowdraglib.client.shader.LDLibRenderTypes.GROUND_PIPELINE);
        event.registerPipeline(com.lowdragmc.lowdraglib.client.shader.LDLibRenderTypes.FACE_OVERLAY_PIPELINE);
        event.registerPipeline(com.lowdragmc.lowdraglib.client.shader.LDLibRenderTypes.SELECTION_PIPELINE);
        event.registerPipeline(com.lowdragmc.lowdraglib.client.shader.LDLibRenderTypes.TANK_FLUID_PIPELINE);
        event.registerPipeline(com.lowdragmc.lowdraglib.client.shader.LDLibRenderTypes.TANK_HOVER_PIPELINE);
    }


//    @SubscribeEvent
//    public void registerTextures(RegisterTextureAtlasSpriteLoadersEvent event) {
//        for (IRenderer renderer : IRenderer.EVENT_REGISTERS) {
//            renderer.onPrepareTextureAtlas(event.getAtlas().location(), location -> event.getAtlas().);
//        }
//    }

    @SubscribeEvent
    public void registerModels(ModelEvent.RegisterStandalone event) {
        com.lowdragmc.lowdraglib.client.model.NativeStandaloneModels.register(event,Identifier.parse("minecraft:block/stone"));
        // load all models under the ldlib folder
        for (var entry : Minecraft.getInstance().getResourceManager().listResources("models",
                id -> id.getNamespace().equals("ldlib") && id.getPath().endsWith(".json")).entrySet()) {
            if (entry.getValue().sourcePackId().equals("ldlib")) {
                com.lowdragmc.lowdraglib.client.model.NativeStandaloneModels.register(event,
                        Identifier.fromNamespaceAndPath(entry.getKey().getNamespace(),
                                entry.getKey().getPath().replace("models/", "").replace(".json", "")));
            }
        }
        for (IRenderer renderer : IRenderer.EVENT_REGISTERS) {
            renderer.onAdditionalModel(id->com.lowdragmc.lowdraglib.client.model.NativeStandaloneModels.register(event,id));
        }
    }

    public static ParticleProvider getProvider(ParticleType<?> type) {
        if (Minecraft.getInstance().particleEngine instanceof ParticleEngineAccessor accessor) {
            return accessor.ldlib$resources().getProviders().get(BuiltInRegistries.PARTICLE_TYPE.getKey(type));
        }
        return null;
    }

}
