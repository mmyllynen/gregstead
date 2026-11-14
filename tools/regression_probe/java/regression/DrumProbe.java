package regression;
import com.gregtechceu.gtceu.common.data.GTMachines;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.transfer.fluid.FluidUtil;
import java.nio.file.*;
public class DrumProbe {
 public static void verifyPersistence(net.minecraft.server.MinecraftServer server){
  var player=server.getPlayerList().getPlayers().getFirst();var level=player.level();var report=new StringBuilder();
  try{
   check(level.getBlockEntity(SOURCE) instanceof com.gregtechceu.gtceu.common.machine.storage.DrumMachine drum&&drum.getStored().getAmount()==1000,"saved drum contains 1000mB");
   report.append("PASS placed drum survives restart with 1000mB\n");
   player.setGameMode(GameType.SURVIVAL);player.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(net.minecraft.world.item.Items.DIAMOND_PICKAXE));
   check(player.gameMode.destroyBlock(SOURCE),"break fixture drum");
   var drops=level.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,new net.minecraft.world.phys.AABB(SOURCE).inflate(1),e->amount(e.getItem())==1000);
   check(drops.size()==1,"one filled drum drop");
   var drop=drops.getFirst();drop.setNoPickUpDelay();drop.playerTouch(player);check(drop.isRemoved(),"collect drum");report.append("PASS break and collect preserves 1000mB\n");
   player.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.FIRE_RESISTANCE,200,0));player.setHealth(player.getMaxHealth());float health=player.getHealth();player.setItemInHand(InteractionHand.MAIN_HAND,GTMachines.WOODEN_DRUM.asStack());level.setBlock(SOURCE,Blocks.LAVA.defaultBlockState(),3);activate(player,InteractionHand.MAIN_HAND);check(player.getMainHandItem().isEmpty()&&player.getHealth()==health&&!level.getFluidState(SOURCE).isEmpty(),"fire resistance protects health");player.removeEffect(net.minecraft.world.effect.MobEffects.FIRE_RESISTANCE);player.clearFire();report.append("PASS fire resistance honored\n");
   player.setGameMode(GameType.CREATIVE);player.setItemInHand(InteractionHand.MAIN_HAND,GTMachines.WOODEN_DRUM.asStack(2));var hit=new net.minecraft.world.phys.BlockHitResult(new net.minecraft.world.phys.Vec3(120.5,160,8.5),net.minecraft.core.Direction.UP,SOURCE.below(),false);player.gameMode.useItemOn(player,level,player.getMainHandItem(),InteractionHand.MAIN_HAND,hit);check(player.getMainHandItem().getCount()==1&&!level.getFluidState(SOURCE).isEmpty(),"creative consumes one wooden barrel");report.append("PASS creative block-use consumes one barrel\n");
  }catch(Throwable ex){report.append("FAIL "+ex+"\n");ex.printStackTrace();}
  finally{player.setGameMode(GameType.CREATIVE);try{Files.writeString(net.neoforged.fml.loading.FMLPaths.GAMEDIR.get().resolve("drum-persistence-results.txt"),report);}catch(Exception ex){throw new RuntimeException(ex);}}
 }
 static final BlockPos SOURCE=new BlockPos(120,160,8);
 static void check(boolean value,String message) {if(!value)throw new AssertionError(message);}
 static net.minecraft.world.InteractionResult activate(net.minecraft.server.level.ServerPlayer player,InteractionHand hand){
  return player.gameMode.useItem(player,player.level(),player.getItemInHand(hand),hand);
 }
 static ItemStack water(ItemStack stack,int amount){stack.set(com.gregtechceu.gtceu.common.data.item.GTDataComponents.FLUID_CONTENT,net.neoforged.neoforge.fluids.SimpleFluidContent.copyOf(new net.neoforged.neoforge.fluids.FluidStack(net.minecraft.world.level.material.Fluids.WATER,amount)));return stack;}
 static int amount(ItemStack stack){return FluidUtil.getFirstStackContained(stack).getAmount();}
 public static void run(net.minecraft.server.MinecraftServer server) {
  var report=new StringBuilder();var player=server.getPlayerList().getPlayers().getFirst();var level=player.level();
  try {
   if(!server.getWorldData().getLevelName().equals("Gregstead-Regression"))throw new IllegalStateException("Wrong world");
   for(int x=119;x<=121;x++)for(int z=7;z<=11;z++)level.setBlock(new BlockPos(x,159,z),Blocks.STONE.defaultBlockState(),3);
   for(var dir:net.minecraft.core.Direction.Plane.HORIZONTAL)level.setBlock(SOURCE.relative(dir),Blocks.GLASS.defaultBlockState(),3);
   level.setBlock(SOURCE.south(),Blocks.AIR.defaultBlockState(),3);
   // Keep original held items in the inventory rather than deleting player progress.
   for(var hand:InteractionHand.values()){var old=player.getItemInHand(hand);player.setItemInHand(hand,ItemStack.EMPTY);if(!old.isEmpty()&&!player.getInventory().add(old))player.drop(old,false,net.minecraft.util.Prediction.SERVER_ONLY);}
   player.setPos(120.5,160,10.5);player.setYRot(180);player.setXRot(29.3f);player.setShiftKeyDown(false);player.setGameMode(GameType.SURVIVAL);player.setHealth(player.getMaxHealth());
   player.setItemInHand(InteractionHand.MAIN_HAND,GTMachines.WOODEN_DRUM.asStack());
   for(int n=1;n<=16;n++){
    level.setBlock(SOURCE,Blocks.WATER.defaultBlockState(),3);
    activate(player,InteractionHand.MAIN_HAND);
    check(amount(player.getMainHandItem())==n*1000,"wood water scoop "+n);
    check(level.getFluidState(SOURCE).isEmpty(),"source removed "+n);
   }
   level.setBlock(SOURCE,Blocks.WATER.defaultBlockState(),3);activate(player,InteractionHand.MAIN_HAND);
   check(amount(player.getMainHandItem())==16000&&!level.getFluidState(SOURCE).isEmpty(),"full barrel unchanged");report.append("PASS wooden 16 scoops and full rejection\n");
   player.setItemInHand(InteractionHand.MAIN_HAND,GTMachines.STEEL_DRUM.asStack());level.setBlock(SOURCE,Blocks.LAVA.defaultBlockState(),3);
   activate(player,InteractionHand.MAIN_HAND);
   check(amount(player.getMainHandItem())==1000&&level.getFluidState(SOURCE).isEmpty(),"steel lava");report.append("PASS steel lava\n");
   level.setBlock(SOURCE,Blocks.WATER.defaultBlockState(),3);activate(player,InteractionHand.MAIN_HAND);
   check(amount(player.getMainHandItem())==1000&&!level.getFluidState(SOURCE).isEmpty(),"foreign fluid");report.append("PASS incompatible contents\n");
   player.setItemInHand(InteractionHand.MAIN_HAND,GTMachines.WOODEN_DRUM.asStack());level.setBlock(SOURCE,Blocks.LAVA.defaultBlockState(),3);float health=player.getHealth();
   activate(player,InteractionHand.MAIN_HAND);
   report.append("BURN held="+player.getMainHandItem()+" source="+level.getFluidState(SOURCE)+" fire="+player.getRemainingFireTicks()+" health="+player.getHealth()+" before="+health+" effects="+player.getActiveEffects()+"\n");
   check(player.getMainHandItem().isEmpty()&&!level.getFluidState(SOURCE).isEmpty()&&player.isOnFire()&&player.getHealth()<health,"wood lava consequence");
   player.clearFire();player.setHealth(player.getMaxHealth());report.append("PASS wood lava destroyed, source retained, ignite and damage\n");
   player.setItemInHand(InteractionHand.OFF_HAND,GTMachines.WOODEN_DRUM.asStack());level.setBlock(SOURCE,Blocks.WATER.defaultBlockState(),3);activate(player,InteractionHand.OFF_HAND);
   check(amount(player.getOffhandItem())==1000,"offhand");report.append("PASS offhand\n");
   level.setBlock(SOURCE,Blocks.LAVA.defaultBlockState(),3);activate(player,InteractionHand.OFF_HAND);check(amount(player.getOffhandItem())==1000&&!level.getFluidState(SOURCE).isEmpty(),"filled wood lava rejected");report.append("PASS filled wood lava rejected\n");
   player.setItemInHand(InteractionHand.MAIN_HAND,water(GTMachines.WOODEN_DRUM.asStack(),15999));level.setBlock(SOURCE,Blocks.WATER.defaultBlockState(),3);activate(player,InteractionHand.MAIN_HAND);check(amount(player.getMainHandItem())==15999&&!level.getFluidState(SOURCE).isEmpty(),"partial bucket rejected");report.append("PASS insufficient room\n");
   player.setItemInHand(InteractionHand.MAIN_HAND,GTMachines.ALUMINIUM_DRUM.asStack());level.setBlock(SOURCE,Blocks.LAVA.defaultBlockState(),3);activate(player,InteractionHand.MAIN_HAND);check(amount(player.getMainHandItem())==0&&!level.getFluidState(SOURCE).isEmpty()&&!player.getMainHandItem().isEmpty(),"aluminium heat rejected");report.append("PASS aluminium temperature limit\n");
   player.setItemInHand(InteractionHand.MAIN_HAND,GTMachines.STEEL_DRUM.asStack());level.setBlock(SOURCE,com.gregtechceu.gtceu.common.data.GTMaterials.Oil.getFluid().defaultFluidState().createLegacyBlock(),3);activate(player,InteractionHand.MAIN_HAND);check(amount(player.getMainHandItem())==1000&&level.getFluidState(SOURCE).isEmpty(),"mod oil source");report.append("PASS mod oil source\n");
   player.setItemInHand(InteractionHand.MAIN_HAND,GTMachines.WOODEN_DRUM.asStack());level.setBlock(SOURCE,com.gregtechceu.gtceu.common.data.GTMaterials.NaturalGas.getFluid().defaultFluidState().createLegacyBlock(),3);activate(player,InteractionHand.MAIN_HAND);check(amount(player.getMainHandItem())==0&&!level.getFluidState(SOURCE).isEmpty(),"wood gas rejected");report.append("PASS gas restriction\n");
   player.setItemInHand(InteractionHand.MAIN_HAND,GTMachines.WOODEN_DRUM.asStack());level.setBlock(SOURCE,Blocks.WATER.defaultBlockState().setValue(net.minecraft.world.level.block.LiquidBlock.LEVEL,1),3);activate(player,InteractionHand.MAIN_HAND);check(amount(player.getMainHandItem())==0,"flowing water rejected");report.append("PASS flowing source rejection\n");
   player.setItemInHand(InteractionHand.MAIN_HAND,GTMachines.WOODEN_DRUM.asStack(2));level.setBlock(SOURCE,Blocks.LAVA.defaultBlockState(),3);activate(player,InteractionHand.MAIN_HAND);check(player.getMainHandItem().getCount()==1&&!level.getFluidState(SOURCE).isEmpty(),"one wooden item burned");player.clearFire();report.append("PASS one barrel consumed from stack\n");
   player.setItemInHand(InteractionHand.MAIN_HAND,GTMachines.WOODEN_DRUM.asStack());level.setBlock(SOURCE,Blocks.WATER.defaultBlockState(),3);player.getAbilities().mayBuild=false;activate(player,InteractionHand.MAIN_HAND);check(amount(player.getMainHandItem())==0&&!level.getFluidState(SOURCE).isEmpty(),"permission denied");player.getAbilities().mayBuild=true;report.append("PASS denied build permission\n");
   player.setShiftKeyDown(true);activate(player,InteractionHand.MAIN_HAND);check(amount(player.getMainHandItem())==0&&!level.getFluidState(SOURCE).isEmpty(),"sneak bypass pickup");player.setShiftKeyDown(false);report.append("PASS sneak bypass\n");
   var hit=new net.minecraft.world.phys.BlockHitResult(new net.minecraft.world.phys.Vec3(120.5,160,8.5),net.minecraft.core.Direction.UP,SOURCE.below(),false);player.gameMode.useItemOn(player,level,player.getMainHandItem(),InteractionHand.MAIN_HAND,hit);check(amount(player.getMainHandItem())==1000&&level.getFluidState(SOURCE).isEmpty(),"block-use path");report.append("PASS block-use path\n");
   // Stash existing test inventory in persistent fixture chests before testing a full inventory.
   var stash1=new BlockPos(119,160,11);var stash2=new BlockPos(121,160,11);
   for(var pos:java.util.List.of(stash1,stash2))if(level.getBlockState(pos).isAir())level.setBlock(pos,Blocks.CHEST.defaultBlockState(),3);
   var chests=java.util.List.of((net.minecraft.world.Container)level.getBlockEntity(stash1),(net.minecraft.world.Container)level.getBlockEntity(stash2));
   for(int slot=0;slot<36;slot++){
    var item=player.getInventory().getItem(slot);if(item.isEmpty())continue;boolean stored=false;
    for(var chest:chests){for(int j=0;j<chest.getContainerSize();j++)if(chest.getItem(j).isEmpty()){chest.setItem(j,item);player.getInventory().setItem(slot,ItemStack.EMPTY);stored=true;break;}if(stored)break;}
    check(stored,"stash room");
   }
   var oldOffhand=player.getOffhandItem();boolean offStored=oldOffhand.isEmpty();
   for(var chest:chests){for(int j=0;j<chest.getContainerSize();j++)if(!offStored&&chest.getItem(j).isEmpty()){chest.setItem(j,oldOffhand);offStored=true;break;}if(offStored)break;}check(offStored,"offhand stash room");player.setItemInHand(InteractionHand.OFF_HAND,new ItemStack(net.minecraft.world.item.Items.COBBLESTONE,64));
   for(int slot=0;slot<36;slot++)player.getInventory().setItem(slot,new ItemStack(net.minecraft.world.item.Items.COBBLESTONE,64));
   player.setItemInHand(InteractionHand.MAIN_HAND,GTMachines.WOODEN_DRUM.asStack(2));level.setBlock(SOURCE,Blocks.WATER.defaultBlockState(),3);
   var area=new net.minecraft.world.phys.AABB(118,159,6,123,164,13);
   int before=level.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,area,e->amount(e.getItem())==1000).size();
   activate(player,InteractionHand.MAIN_HAND);
   int after=level.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,area,e->amount(e.getItem())==1000).size();
   report.append("OVERFLOW held="+player.getMainHandItem()+" amount="+amount(player.getMainHandItem())+" before="+before+" after="+after+" offhand="+player.getOffhandItem()+"\n");
   check(player.getMainHandItem().getCount()==1&&amount(player.getMainHandItem())==0&&after==before+1&&level.getFluidState(SOURCE).isEmpty(),"full inventory overflow exactly one filled drum");report.append("PASS stack and full-inventory overflow\n");
   // Discard only the cobblestone generated by this test; original items remain in the fixture chests.
   for(int slot=0;slot<36;slot++)if(player.getInventory().getItem(slot).is(net.minecraft.world.item.Items.COBBLESTONE))player.getInventory().setItem(slot,ItemStack.EMPTY);
   if(player.getOffhandItem().is(net.minecraft.world.item.Items.COBBLESTONE))player.setItemInHand(InteractionHand.OFF_HAND,ItemStack.EMPTY);
   player.setItemInHand(InteractionHand.MAIN_HAND,water(GTMachines.WOODEN_DRUM.asStack(),1000));player.setShiftKeyDown(true);
   player.gameMode.useItemOn(player,level,player.getMainHandItem(),InteractionHand.MAIN_HAND,hit);player.setShiftKeyDown(false);
   check(level.getBlockEntity(SOURCE) instanceof com.gregtechceu.gtceu.common.machine.storage.DrumMachine drum&&drum.getStored().getAmount()==1000,"placed filled drum");report.append("PASS sneak placement retains 1000mB; restart verification pending\n");
   report.append("CORE_TESTS_PASSED\n");
  }catch(Throwable ex){report.append("FAIL ").append(ex.toString()).append("\n");ex.printStackTrace();}
  finally{player.clearFire();player.setHealth(player.getMaxHealth());player.setGameMode(GameType.CREATIVE);try{Files.writeString(net.neoforged.fml.loading.FMLPaths.GAMEDIR.get().resolve("drum-results.txt"),report);}catch(Exception ex){throw new RuntimeException(ex);}}
 }
}
