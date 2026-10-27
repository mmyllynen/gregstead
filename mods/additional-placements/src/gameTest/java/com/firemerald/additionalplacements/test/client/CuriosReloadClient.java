package com.firemerald.additionalplacements.test.client;

import com.firemerald.additionalplacements.AdditionalPlacementsMod;
import com.firemerald.additionalplacements.generation.Registration;
import java.nio.file.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.common.NeoForge;
import top.theillusivec4.curios.api.*;

@Mod(value=AdditionalPlacementsMod.MOD_ID,dist=Dist.CLIENT)
public final class CuriosReloadClient {
    int ticks;
    public CuriosReloadClient(){if(Boolean.getBoolean("ap.curiosRegressionClient"))NeoForge.EVENT_BUS.addListener(this::tick);}
    void tick(ClientTickEvent.Post event){
        if(!Files.exists(Path.of("../run-curios-server/curios-regression-pass.json")))return;
        if(++ticks<60)return;
        var mc=Minecraft.getInstance();
        try{
            if(mc.player==null)throw new IllegalStateException("Disconnected during datapack reload");
            if(CuriosSlotTypes.getSlotType("necklace",true)==null)throw new IllegalStateException("Client necklace slot missing");
            var inv=CuriosApi.getCuriosInventory(mc.player).orElseThrow();
            if(!inv.getStacksHandler("necklace").orElseThrow().getStacks().getStackInSlot(0).is(
                BuiltInRegistries.ITEM.getValue(Identifier.parse("neogreg_calm:calm_amulet"))))throw new IllegalStateException("Client equipped amulet lost");
            if(Registration.created().count()!=3017)throw new IllegalStateException("Client AP variants missing");
            Screenshot.grab(mc.gameDirectory,"curios-reload-pass.png",mc.gameRenderer.mainRenderTarget(),1,m->{});
            Files.writeString(mc.gameDirectory.toPath().resolve("curios-client-pass.txt"),"Client slot, equipped amulet and all 3017 AP variants synchronized after live server reload.\n");
            System.out.println("CURIOS_REGRESSION_CLIENT_PASS");
        }catch(Throwable t){t.printStackTrace();try{Files.writeString(mc.gameDirectory.toPath().resolve("curios-client-failure.txt"),t.toString());}catch(Exception ignored){}}
        mc.stop();
    }
}
