package io.github.altarofabundance;

import java.util.Set;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;

/** An opt-in disposable scene for checking real client rendering against a dedicated server. */
@Mod(AltarOfAbundance.MOD_ID)
public final class AltarSmokeServer {
    public AltarSmokeServer(IEventBus ignored) {
        if (!Boolean.getBoolean("altar.smokeServer")) return;
        NeoForge.EVENT_BUS.addListener(AltarSmokeServer::started);
        NeoForge.EVENT_BUS.addListener(AltarSmokeServer::joined);
        NeoForge.EVENT_BUS.addListener(AltarSmokeServer::commands);
    }

    private static void started(ServerStartedEvent event) {
        var level = event.getServer().overworld();
        for (BlockPos pos : BlockPos.betweenClosed(new BlockPos(-10, 100, -10), new BlockPos(14, 105, 12))) {
            level.setBlockAndUpdate(pos, pos.getY() == 100 ? Blocks.SMOOTH_STONE.defaultBlockState() : Blocks.AIR.defaultBlockState());
        }
        level.setBlockAndUpdate(new BlockPos(0, 101, 0), AltarOfAbundance.MATERIAL.get().defaultBlockState());
        level.setBlockAndUpdate(new BlockPos(2, 101, 0), AltarOfAbundance.OFFERING.get().defaultBlockState());
        level.setBlockAndUpdate(new BlockPos(4, 101, 0), AltarOfAbundance.OUTPUT.get().defaultBlockState());
        event.getServer().getCommands().performPrefixedCommand(event.getServer().createCommandSourceStack(), "time set day");
    }

    private static void joined(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof net.minecraft.server.level.ServerPlayer player)) return;
        player.setGameMode(GameType.CREATIVE);
        player.getAbilities().flying = true;
        player.onUpdateAbilities();
        player.teleportTo(player.level(), 2.5, 103, 7.5, Set.of(), 180, 22, true);
    }

    private static void commands(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("altar_smoke_start").executes(context -> {
            var level = context.getSource().getLevel();
            ((PedestalBlockEntity) level.getBlockEntity(new BlockPos(0, 101, 0))).insert(new ItemStack(Items.OAK_LOG), false);
            ((PedestalBlockEntity) level.getBlockEntity(new BlockPos(2, 101, 0))).insert(new ItemStack(Items.DIAMOND), false);
            return 1;
        }));
    }
}
