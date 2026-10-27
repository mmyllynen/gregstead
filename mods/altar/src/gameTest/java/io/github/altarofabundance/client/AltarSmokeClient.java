package io.github.altarofabundance.client;

import io.github.altarofabundance.AltarOfAbundance;
import io.github.altarofabundance.PedestalBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.common.NeoForge;

@Mod(value = AltarOfAbundance.MOD_ID, dist = Dist.CLIENT)
public final class AltarSmokeClient {
    private int ticks;

    public AltarSmokeClient() {
        if (Boolean.getBoolean("altar.smokeClient")) NeoForge.EVENT_BUS.addListener(this::tick);
    }

    private void capture(Minecraft minecraft, String name) {
        Screenshot.grab(minecraft.gameDirectory, name, minecraft.gameRenderer.mainRenderTarget(), 1,
            message -> System.out.println("ALTAR_SMOKE_SCREENSHOT " + message.getString()));
    }

    private void tick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || minecraft.player == null
                || !(minecraft.level.getBlockEntity(new BlockPos(0, 101, 0)) instanceof PedestalBlockEntity)) return;
        ticks++;
        if (!minecraft.gui.hud.isHidden()) minecraft.gui.hud.toggle();
        if (ticks == 40) capture(minecraft, "altar-empty.png");
        if (ticks == 50) minecraft.player.connection.sendCommand("altar_smoke_start");
        if (ticks == 75) {
            if (((PedestalBlockEntity) minecraft.level.getBlockEntity(new BlockPos(0, 101, 0))).displayedItem().isEmpty()) {
                throw new IllegalStateException("ALTAR_SMOKE: inserted material did not synchronize to client");
            }
            capture(minecraft, "altar-ritual.png");
        }
        if (ticks == 150) {
            int result = minecraft.level.getEntitiesOfClass(ItemEntity.class, new AABB(-2, 100, -2, 7, 105, 3))
                .stream().filter(item -> item.getItem().is(Items.OAK_LOG)).mapToInt(item -> item.getItem().getCount()).sum();
            if (result != 64) throw new IllegalStateException("ALTAR_SMOKE: expected 64 logs visible to client, got " + result);
            capture(minecraft, "altar-result.png");
            System.out.println("ALTAR_SMOKE_PASS: ingredients synchronized and 64-block output received from dedicated server");
        }
        if (ticks == 180) minecraft.stop();
    }
}
