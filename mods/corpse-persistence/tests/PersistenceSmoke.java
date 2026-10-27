package corpseprobe;
import de.maxhenkel.corpse.CorpseMod;
import de.maxhenkel.corpse.entities.CorpseEntity;
import de.maxhenkel.corpse.corelib.death.Death;
import net.minecraft.core.*;
import net.minecraft.world.item.*;
import net.minecraft.world.entity.Entity;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import java.nio.file.*;import java.util.*;
@Mod("corpse_persistence_smoke") public class PersistenceSmoke {
 int ticks;List<String> checks=new ArrayList<>();
 public PersistenceSmoke(){NeoForge.EVENT_BUS.addListener(this::tick);
  NeoForge.EVENT_BUS.addListener((net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent e)->{
   if(Boolean.getBoolean("corpse.visual")&&e.getEntity() instanceof net.minecraft.server.level.ServerPlayer p){var l=p.level();for(int x=-2;x<7;x++)for(int z=-5;z<5;z++)l.setBlock(new BlockPos(x,79,z),net.minecraft.world.level.block.Blocks.QUARTZ_BLOCK.defaultBlockState(),3);p.teleportTo(l,2,80,-3,java.util.Set.of(),0,15,false);}
  });
 }
 void check(boolean c,String n){if(!c)throw new IllegalStateException(n);checks.add(n);}
 void age(CorpseEntity c,int age,int emptyAge)throws Exception{var f=CorpseEntity.class.getDeclaredField("age");f.setAccessible(true);f.setInt(c,age);f=CorpseEntity.class.getDeclaredField("emptyAge");f.setAccessible(true);f.setInt(c,emptyAge);}
 CorpseEntity corpse(ServerLevel l,boolean full){var c=new CorpseEntity(l);c.setPos(1,80,1);c.setNoGravity(true);c.setCorpseName(full?"Loot test":"Empty test");if(full)c.setDeath(new Death.Builder(UUID.randomUUID(),UUID.randomUUID()).playerName("Synthetic").mainInventory(NonNullList.of(ItemStack.EMPTY,new ItemStack(Items.DIAMOND,3))).build());return c;}
 void validate(ServerLevel l)throws Exception {
  var marker=Path.of("persisted-corpses.json");
  if(Files.exists(marker)){
   var ids=new com.google.gson.Gson().fromJson(Files.readString(marker),String[].class);
   for(String id:ids){var c=(CorpseEntity)l.getEntity(UUID.fromString(id));check(c!=null&&c.isSkeleton()&&!c.isRemoved(),"skeleton_loaded_after_restart");for(int i=0;i<1000;i++)c.tick();check(!c.isRemoved(),"empty_skeleton_remains_after_restart");}
   return;
  }
  var empty=corpse(l,false);age(empty,71995,0);for(int i=0;i<1000;i++)empty.tick();check(!empty.isRemoved(),"empty_corpse_outlives_normal_30_second_expiry");check(empty.isSkeleton(),"empty_corpse_ages_into_skeleton");
  CorpseMod.SERVER_CONFIG.corpseForceDespawnTime.set(20);var full=corpse(l,true);age(full,71995,-1);for(int i=0;i<1000;i++)full.tick();check(!full.isRemoved()&&full.isSkeleton(),"full_skeleton_ignores_forced_expiry");check(full.getDeath().getMainInventory().getFirst().getCount()==3,"loot_preserved");full.getDeath().getMainInventory().clear();check(full.isEmpty(),"loot_can_be_removed");for(int i=0;i<1000;i++)full.tick();check(!full.isRemoved()&&full.isSkeleton(),"looted_skeleton_remains");CorpseMod.SERVER_CONFIG.corpseForceDespawnTime.set(-1);
  var removable=corpse(l,false);removable.discard();check(removable.isRemoved(),"explicit_removal_still_works");
  age(empty,72000,0);age(full,72000,0);l.addFreshEntity(empty);full.setPos(3,80,1);l.addFreshEntity(full);
  Files.writeString(marker,new com.google.gson.Gson().toJson(new String[]{empty.getUUID().toString(),full.getUUID().toString()}));
 }
 void tick(ServerTickEvent.Post e){if(Boolean.getBoolean("corpse.visual"))return;if(++ticks==20)e.getServer().overworld().setChunkForced(0,0,true);if(ticks!=80)return;try{validate(e.getServer().overworld());var result=new com.google.gson.JsonObject();result.addProperty("passed",true);result.add("checks",new com.google.gson.Gson().toJsonTree(checks));Files.writeString(Path.of(Files.exists(Path.of("first-proof.json"))?"restart-proof.json":"first-proof.json"),new com.google.gson.GsonBuilder().setPrettyPrinting().create().toJson(result));System.out.println("PERSISTENCE_SMOKE_PASSED");}catch(Throwable t){t.printStackTrace();try{Files.writeString(Path.of("failure.txt"),t.toString());}catch(Exception ignored){}}e.getServer().halt(false);}
}
