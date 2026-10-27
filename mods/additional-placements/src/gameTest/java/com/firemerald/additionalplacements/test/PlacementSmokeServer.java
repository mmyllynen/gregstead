package com.firemerald.additionalplacements.test;

import com.firemerald.additionalplacements.AdditionalPlacementsMod;
import com.firemerald.additionalplacements.block.*;
import com.firemerald.additionalplacements.generation.Registration;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.*;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;

@Mod(AdditionalPlacementsMod.MOD_ID)
public final class PlacementSmokeServer {
    private static final Set<UUID> joinedPlayers = new HashSet<>();
    public PlacementSmokeServer() {
        if (!Boolean.getBoolean("ap.smokeServer")) return;
        NeoForge.EVENT_BUS.addListener(PlacementSmokeServer::started);
        NeoForge.EVENT_BUS.addListener(PlacementSmokeServer::joined);
    }
    private static void started(ServerStartedEvent event) {
        var level=event.getServer().overworld();
        for(BlockPos pos:BlockPos.betweenClosed(new BlockPos(-5,100,-5),new BlockPos(18,104,14)))
            level.setBlockAndUpdate(pos,pos.getY()==100?Blocks.SMOOTH_STONE.defaultBlockState():Blocks.AIR.defaultBlockState());
        List<AdditionalPlacementBlock<?>> display=new ArrayList<>();
        for(String id:List.of("oak_slab","stone_slab","cut_copper_slab","oak_stairs","stone_brick_stairs","red_carpet","white_carpet","oak_pressure_plate","light_weighted_pressure_plate"))
            display.add(PlacementTests.variant(BuiltInRegistries.BLOCK.getValue(Identifier.withDefaultNamespace(id))));
        for(String namespace:List.of("gtceu","mcwpaths","rechiseled"))
            Registration.created().filter(e->e.originalId().getNamespace().equals(namespace)).findFirst().ifPresent(e->display.add(e.newBlock()));
        for(int i=0;i<display.size();i++) {
            BlockPos pos=new BlockPos((i%4)*3,101,(i/4)*3);
            BlockState state=display.get(i).defaultBlockState();
            if(state.hasProperty(AdditionalFloorBlock.PLACING)){
                state=state.setValue(AdditionalFloorBlock.PLACING,Direction.NORTH);
                level.setBlockAndUpdate(pos.north(),Blocks.STONE.defaultBlockState());
            }
            level.setBlockAndUpdate(pos,state);
        }
        level.setBlockAndUpdate(new BlockPos(5,101,9),Blocks.STONE.defaultBlockState());
        event.getServer().getCommands().performPrefixedCommand(event.getServer().createCommandSourceStack(),"time set day");
        System.out.println("AP_SMOKE_SCENE_READY "+display.size()+" display blocks; generated "+Registration.created().count());
    }
    private static void joined(PlayerEvent.PlayerLoggedInEvent event) {
        if(!(event.getEntity() instanceof ServerPlayer player))return;
        player.setGameMode(GameType.CREATIVE);player.getAbilities().flying=true;player.onUpdateAbilities();
        player.teleportTo(player.level(),5.5,104,14.5,Set.of(),180,22,true);
        if (joinedPlayers.add(player.getUUID())) {
            player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,new ItemStack(Items.DARK_OAK_SLAB,64));
        } else {
            if (!player.getMainHandItem().is(Items.DARK_OAK_SLAB)) throw new IllegalStateException("Dark oak slab was not retained across reconnect");
            System.out.println("AP_SMOKE_PERSISTED_DARK_OAK_REJOIN_PASS");
        }
    }
}
