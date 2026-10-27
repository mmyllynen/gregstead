package com.firemerald.additionalplacements.test;

import com.firemerald.additionalplacements.AdditionalPlacementsMod;
import com.firemerald.additionalplacements.block.VerticalSlabBlock;
import com.firemerald.additionalplacements.generation.Registration;
import com.mojang.authlib.GameProfile;
import fi.neogreg.calm.CalmAreas;
import java.nio.file.*;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.*;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.*;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.*;
import net.minecraft.world.phys.*;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import top.theillusivec4.curios.api.*;

@Mod(AdditionalPlacementsMod.MOD_ID)
public final class CuriosReloadRegression {
    int phase;
    long start=System.currentTimeMillis();
    List<String> checks=new ArrayList<>();
    ServerPlayer real;
    CompoundTag saved;
    public CuriosReloadRegression(){if(Boolean.getBoolean("ap.curiosRegression"))NeoForge.EVENT_BUS.addListener(this::tick);}
    void check(boolean b,String name){if(!b)throw new IllegalStateException(name);checks.add(name);System.out.println("CURIOS_REGRESSION_CHECK "+name);}
    Item amulet(){return BuiltInRegistries.ITEM.getValue(Identifier.parse("neogreg_calm:calm_amulet"));}
    void slots(String prefix){
        for(String id:List.of("head","necklace","back","hands","belt","gtceu_magnet"))
            check(CuriosSlotTypes.getSlotType(id,false)!=null,prefix+"_slot_"+id);
    }
    void restoreSaved(MinecraftServer server,String prefix)throws Exception{
        var l=server.overworld();
        var p=FakePlayerFactory.get(l,new GameProfile(UUID.randomUUID(),"SavedCuriosTest"));
        p.setGameMode(GameType.SURVIVAL);p.setPos(80,200,80);
        // Exercise precisely the saved Curios payload that failed on Mushroom, without touching live files.
        var root=NbtIo.readCompressed(Path.of("huuuru-before.dat"),NbtAccounter.unlimitedHeap());
        var data=root.getCompound("neoforge:attachments").orElseThrow().getCompound("curios:inventory").orElseThrow();
        var inv=CuriosApi.getCuriosInventory(p).orElseThrow();
        inv.deserialize(TagValueInput.create(ProblemReporter.DISCARDING,l.registryAccess(),data));
        inv.loadDatapacks();
        check(inv.getStacksHandler("necklace").orElseThrow().getStacks().getStackInSlot(0).is(amulet()),prefix+"_saved_amulet_loaded");
        check(inv.isSlotActive("necklace",0),prefix+"_necklace_active");
        l.addNewPlayer(p);
        var z=EntityTypes.ZOMBIE.create(l,EntitySpawnReason.COMMAND);z.setPos(p.position());
        check(CalmAreas.contains(z),prefix+"_saved_amulet_calms");z.setTarget(p);
        check(z.getTarget()==null,prefix+"_zombie_target_blocked");
        var output=TagValueOutput.createWithContext(ProblemReporter.DISCARDING,l.registryAccess());inv.serialize(output);
        var again=FakePlayerFactory.get(l,new GameProfile(UUID.randomUUID(),"CuriosRoundtrip"));
        var inv2=CuriosApi.getCuriosInventory(again).orElseThrow();
        inv2.deserialize(TagValueInput.create(ProblemReporter.DISCARDING,l.registryAccess(),output.buildResult()));
        check(inv2.getStacksHandler("necklace").orElseThrow().getStacks().getStackInSlot(0).is(amulet()),prefix+"_save_relogin_roundtrip");
        // Original slab item must still place the generated vertical block and spend one item.
        BlockPos target=new BlockPos(80,200,80);l.setBlockAndUpdate(target,Blocks.AIR.defaultBlockState());
        l.setBlockAndUpdate(target.north(),Blocks.STONE.defaultBlockState());p.setPos(80.5,200,82.5);p.setYRot(180);
        p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(Items.OAK_SLAB,8));
        var hit=new BlockHitResult(new Vec3(80.5,200.5,80),Direction.SOUTH,target.north(),false);
        ((BlockItem)Items.OAK_SLAB).place(new BlockPlaceContext(p,InteractionHand.MAIN_HAND,p.getMainHandItem(),hit));
        check(l.getBlockState(target).getBlock() instanceof VerticalSlabBlock,prefix+"_additional_placement");
        check(p.getMainHandItem().count()==7,prefix+"_placement_cost");
        l.removeBlock(target,false);l.removePlayerImmediately(p,Entity.RemovalReason.UNLOADED_WITH_PLAYER);
        check(Registration.created().count()==3017,prefix+"_all_3017_variants");
    }
    void reload(MinecraftServer s,int next){
        s.reloadResources(s.getPackRepository().getSelectedIds()).whenComplete((v,error)->s.execute(()->{
            if(error!=null){fail(s,error);return;}
            phase=next;
        }));
    }
    void fail(MinecraftServer s,Throwable t){
        t.printStackTrace();try{Files.writeString(Path.of("curios-regression-failure.txt"),t+"\n"+checks);}catch(Exception ignored){}
        phase=99;s.halt(false);
    }
    void tick(ServerTickEvent.Post e){
        var s=e.getServer();try{
            if(System.currentTimeMillis()-start>600000)throw new IllegalStateException("Regression timeout phase "+phase);
            if(phase==0&&System.currentTimeMillis()-start>45000){
                check(s.getPlayerList().getPlayerCount()==0,"initial_no_online_players");slots("automatic_reload");restoreSaved(s,"automatic_reload");
                phase=1;reload(s,2);
            }else if(phase==2){
                slots("empty_server_reload");restoreSaved(s,"empty_server_reload");
                System.out.println("CURIOS_REGRESSION_READY_FOR_CLIENT");phase=3;
            }else if(phase==3&&s.getPlayerList().getPlayerCount()>0){
                real=s.getPlayerList().getPlayers().getFirst();real.setGameMode(GameType.CREATIVE);
                for(BlockPos pos:BlockPos.betweenClosed(new BlockPos(46,199,46),new BlockPos(50,199,50)))real.level().setBlockAndUpdate(pos,Blocks.STONE.defaultBlockState());
                real.teleportTo(real.level(),48.5,200,48.5,Set.of(),0,0,true);
                var inv=CuriosApi.getCuriosInventory(real).orElseThrow();inv.setEquippedCurio("necklace",0,new ItemStack(amulet()));
                var output=TagValueOutput.createWithContext(ProblemReporter.DISCARDING,real.registryAccess());inv.serialize(output);saved=output.buildResult();
                phase=4;reload(s,5);
            }else if(phase==5){
                slots("online_reload");
                var inv=CuriosApi.getCuriosInventory(real).orElseThrow();
                check(inv.getStacksHandler("necklace").orElseThrow().getStacks().getStackInSlot(0).is(amulet()),"online_equipped_amulet_preserved");
                var z=EntityTypes.ZOMBIE.create(real.level(),EntitySpawnReason.COMMAND);z.setPos(real.position());
                check(CalmAreas.contains(z),"online_reload_amulet_calms");z.setTarget(real);check(z.getTarget()==null,"online_reload_target_blocked");
                restoreSaved(s,"online_reload");
                Files.writeString(Path.of("curios-regression-pass.json"),new com.google.gson.GsonBuilder().setPrettyPrinting().create().toJson(checks));
                System.out.println("CURIOS_REGRESSION_PASS "+checks.size());phase=6;
            }else if(phase==6&&s.getPlayerList().getPlayerCount()==0){phase=99;s.halt(false);}
        }catch(Throwable t){fail(s,t);}
    }
}
