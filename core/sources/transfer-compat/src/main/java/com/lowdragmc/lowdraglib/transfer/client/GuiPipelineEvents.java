package com.lowdragmc.lowdraglib.transfer.client;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterRenderPipelinesEvent;

@EventBusSubscriber(modid = "gttransfercompat", value = Dist.CLIENT)
public final class GuiPipelineEvents {
    private GuiPipelineEvents() {}
    @SubscribeEvent public static void register(RegisterRenderPipelinesEvent event) {
        GuiBuiltInPipelines.values().forEach(event::registerPipeline);
    }
}
