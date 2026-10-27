package io.github.altarofabundance.client;

import io.github.altarofabundance.AltarOfAbundance;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@Mod(value = AltarOfAbundance.MOD_ID, dist = Dist.CLIENT)
public final class AltarClient {
    public AltarClient(IEventBus bus) {
        bus.addListener(AltarClient::registerRenderers);
    }

    private static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(AltarOfAbundance.PEDESTAL.get(), PedestalRenderer::new);
    }
}
