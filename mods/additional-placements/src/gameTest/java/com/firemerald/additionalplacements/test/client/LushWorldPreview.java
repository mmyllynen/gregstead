package com.firemerald.additionalplacements.test.client;
import com.firemerald.additionalplacements.AdditionalPlacementsMod;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.minecraft.client.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.effect.*;
import java.nio.file.*;import java.util.*;
@Mod(value=AdditionalPlacementsMod.MOD_ID,dist=Dist.CLIENT)
public final class LushWorldPreview {
 int ticks,phase;boolean pending;volatile boolean ready;List<String> checks=new ArrayList<>();
 static final double[][] SCENES={{-1388.5,71,-5731.5,180,12},{-1280.5,-5,-5803.5,90,-5},{-1280.5,-5,-5803.5,270,8},{-1232.5,-2,-5658.5,180,0}};
 public LushWorldPreview(){if(Boolean.getBoolean("ap.lushPreview"))NeoForge.EVENT_BUS.addListener(this::tick);}
 void tick(ClientTickEvent.Post e){var mc=Minecraft.getInstance();if(mc.player==null||mc.level==null||mc.getSingleplayerServer()==null)return;
 try{
  if(!pending){pending=true;ready=false;double[] scene=SCENES[phase];UUID id=mc.player.getUUID();mc.getSingleplayerServer().execute(()->{
   var server=mc.getSingleplayerServer();var l=server.overworld();var p=server.getPlayerList().getPlayer(id);
   if(!l.getBiome(new BlockPos(-1222,-6,-5740)).is(Biomes.LUSH_CAVES))throw new IllegalStateException("Former sulfur biome was not replaced");
   String b=BuiltInRegistries.BLOCK.getKey(l.getBlockState(new BlockPos(-1222,-6,-5740)).getBlock()).toString();if(b.contains("sulfur"))throw new IllegalStateException("Sulfur material remained at validation point");
   p.setGameMode(GameType.SPECTATOR);p.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION,20000,0,false,false,false));p.teleportTo(l,scene[0],scene[1],scene[2],Set.of(),(float)scene[3],(float)scene[4],true);ready=true;
  });return;}
  if(!ready)return;if(++ticks<100)return;
  String name="lush-preview-"+phase+".png";Screenshot.grab(mc.gameDirectory,name,mc.gameRenderer.mainRenderTarget(),1,c->{});checks.add(name);System.out.println("LUSH_PREVIEW_SCENE_PASS "+phase);phase++;ticks=0;pending=false;
  if(phase==SCENES.length){Files.writeString(mc.gameDirectory.toPath().resolve("preview-pass.json"),new com.google.gson.GsonBuilder().setPrettyPrinting().create().toJson(checks));mc.stop();}
 }catch(Throwable t){t.printStackTrace();try{Files.writeString(mc.gameDirectory.toPath().resolve("preview-failure.txt"),t.toString());}catch(Exception ignored){}mc.stop();}}
}
