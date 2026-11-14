package regression;

import com.gregtechceu.gtceu.api.machine.*;
import com.gregtechceu.gtceu.api.sync_system.*;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.api.machine.trait.*;
import com.gregtechceu.gtceu.common.data.item.GTDataComponents;
import com.gregtechceu.gtceu.common.machine.trait.miner.MinerLogic;
import net.minecraft.core.*;
import net.minecraft.nbt.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.*;
import net.minecraft.world.InteractionHand;
import java.nio.file.*;
import java.lang.reflect.*;
import java.util.*;

/** Runs only in the persistent regression world; never bundled in the mod. */
public final class MachineStateProbe {
    static final BlockPos POS = new BlockPos(124,160,8);
    static final BlockPos SAVED = new BlockPos(124,160,10);
    static final BlockPos LEGACY = new BlockPos(126,160,10);
    static StringBuilder report;
    static void check(boolean test,String message) { if(!test) throw new AssertionError(message); }
    static Object get(Object object,String name) throws Exception {return field(object,name).get(object);}
    static Field field(Object object,String name) throws Exception {
        for(Class<?> c=object.getClass();c!=null;c=c.getSuperclass())try{var f=c.getDeclaredField(name);f.setAccessible(true);return f;}catch(NoSuchFieldException e){}
        throw new NoSuchFieldException(name);
    }
    static void set(Object object,String name,Object value)throws Exception{field(object,name).set(object,value);}
    static void seed(ISyncManaged object,Set<Object> seen) throws Exception {
        if(!seen.add(object))return;
        if(object instanceof RecipeLogic logic)logic.setWorkingEnabled(false);
        if(object instanceof NotifiableEnergyContainer e)e.setEnergyStored(Math.min(1234,e.getEnergyCapacity()));
        if(object instanceof NotifiableFluidTank t && t.getTanks()>0)
            t.setFluidInTank(0,new net.neoforged.neoforge.fluids.FluidStack(net.minecraft.world.level.material.Fluids.WATER,321));
        for(Class<?> c=object.getClass();c!=null;c=c.getSuperclass())for(var f:c.getDeclaredFields()){
            var save=f.getAnnotation(SaveField.class);if(save==null)continue;f.setAccessible(true);var v=f.get(object);
            if(save.item()==SaveField.ItemSave.CHILDREN && v instanceof ISyncManaged child)seed(child,seen);
            if(v instanceof com.gregtechceu.gtceu.common.machine.multiblock.electric.PowerSubstationMachine.PowerStationEnergyBank bank) {
                var tag=new CompoundTag();tag.putInt("Size",2);
                for(int i=0;i<2;i++){var cell=new CompoundTag();cell.putLong("Stored",123456789L+i);cell.putLong("Max",1000000000L);tag.put(Integer.toString(i),cell);}
                bank.deserializeNBT(net.minecraft.client.Minecraft.getInstance().getSingleplayerServer().registryAccess(),tag);
            }
            if(save.item()==SaveField.ItemSave.VALUE && !Modifier.isFinal(f.getModifiers())){
                if(f.getType()==boolean.class)f.setBoolean(object,!f.getBoolean(object));
                if(Set.of("heat","currentTemperature","timeBeforeCoolingDown","temperature").contains(f.getName())) {
                    if(f.getType()==int.class)f.setInt(object,123);
                    if(f.getType()==long.class)f.setLong(object,12345L);
                    if(f.getType()==float.class)f.setFloat(object,123.0F);
                    if(f.getType()==double.class)f.setDouble(object,123.0D);
                }
                // Keep selector/range/temperature values inside ordinary valid ranges.
                if(f.getType()==int.class && Set.of("paintingColor","currentRadius","range","throttle","currentParallel").contains(f.getName()))
                    f.setInt(object,Math.max(1, f.getInt(object)-1));
            }
        }
    }
    static MetaMachine place(net.minecraft.server.level.ServerPlayer player,ItemStack stack,BlockPos pos){
        var level=player.level();level.setBlock(pos.below(),Blocks.STONE.defaultBlockState(),3);
        var hit=new BlockHitResult(Vec3.atCenterOf(pos.below()),Direction.UP,pos.below(),false);
        var context=new net.minecraft.world.item.context.BlockPlaceContext(player,InteractionHand.MAIN_HAND,stack,hit);
        check(((com.gregtechceu.gtceu.api.item.MetaMachineItem)stack.getItem()).place(context).consumesAction(),"placement "+stack);
        check(level.getBlockEntity(pos) instanceof MetaMachine,"machine placed "+pos);
        return (MetaMachine)level.getBlockEntity(pos);
    }
    public static void run(net.minecraft.server.MinecraftServer server){
        report=new StringBuilder();var player=server.getPlayerList().getPlayers().getFirst();var level=player.level();
        var original=player.getMainHandItem();var originalMode=player.gameMode.getGameModeForPlayer();
        int pass=0,fail=0;
        try{
            check(server.getWorldData().getLevelName().equals("Gregstead-Regression"),"wrong world");
            if(level.getBlockEntity(POS) instanceof com.gregtechceu.gtceu.common.machine.steam.SteamMinerMachine old &&
                    !old.getRecipeLogic().isDone()) level.setBlock(POS,Blocks.AIR.defaultBlockState(),3);
            check(level.isEmptyBlock(POS),"fixture occupied; inspect instead of overwriting");
            server.getCommands().performPrefixedCommand(server.createCommandSourceStack(),"tp "+player.getScoreboardName()+" 124.5 160 13.5 180 20");
            player.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);
            for(var definition:GTRegistries.MACHINES){
                try{
                    level.setBlock(POS,definition.defaultBlockState(),3);
                    var machine=(MetaMachine)level.getBlockEntity(POS);
                    check(machine!=null,"block entity missing");
                    machine.setOwnerUUID(player.getUUID());
                    seed(machine,Collections.newSetFromMap(new IdentityHashMap<>()));
                    var expected=MachineItemState.collect(machine,level.registryAccess());
                    var stack=definition.asStack();machine.saveToItem(stack,level.registryAccess());
                    check(!stack.has(net.minecraft.core.component.DataComponents.BLOCK_ENTITY_DATA),"no full world NBT");
                    level.setBlock(POS,Blocks.AIR.defaultBlockState(),3);
                    var restored=place(player,stack.copy(),POS);
                    var actual=MachineItemState.collect(restored,level.registryAccess());
                    check(expected.equals(actual),"portable mismatch expected="+expected+" actual="+actual);
                    pass++;
                    report.append("PASS "+definition.getId()+" "+machine.getClass().getSimpleName()+"\n");
                }catch(Throwable e){fail++;report.append("FAIL "+definition.getId()+" "+e+"\n");e.printStackTrace();}
                finally{level.setBlock(POS,Blocks.AIR.defaultBlockState(),3);}
            }
            report.append("MATRIX passed="+pass+" failed="+fail+"\n");
            miners(server,player);
        }catch(Throwable e){report.append("FAIL suite "+e+"\n");e.printStackTrace();}
        finally{player.setItemInHand(InteractionHand.MAIN_HAND,original);player.setGameMode(originalMode);write("machine-state-results.txt",report.toString());}
    }
    static void miners(net.minecraft.server.MinecraftServer server,net.minecraft.server.level.ServerPlayer player)throws Exception{
        var level=player.level();var definition=com.gregtechceu.gtceu.common.data.GTMachines.STEAM_MINER.second();
        level.setBlock(POS,definition.defaultBlockState(),3);
        var machine=(com.gregtechceu.gtceu.common.machine.steam.SteamMinerMachine)level.getBlockEntity(POS);var logic=machine.getRecipeLogic();
        logic.initPos(POS,logic.getCurrentRadius());set(logic,"isDone",true);set(logic,"progress",57);logic.setWorkingEnabled(false);
        var legacy=machine.saveWithFullMetadata(level.registryAccess());
        var legacyStack=definition.asStack();com.lowdragmc.lowdraglib.transfer.persistence.BlockEntityItems.save(machine,legacyStack,level.registryAccess());
        legacyStack.remove(GTDataComponents.MACHINE_ITEM_DATA);
        var portable=definition.asStack();machine.saveToItem(portable,level.registryAccess());
        level.setBlock(POS,Blocks.AIR.defaultBlockState(),3);
        var moved=(com.gregtechceu.gtceu.common.machine.steam.SteamMinerMachine)place(player,portable,POS);
        check(!moved.getRecipeLogic().isDone()&&moved.getRecipeLogic().getProgress()==0,"new item resets job");
        check(!moved.getRecipeLogic().isWorkingEnabled(),"new item retains pause");
        level.setBlock(POS,Blocks.AIR.defaultBlockState(),3);
        moved=(com.gregtechceu.gtceu.common.machine.steam.SteamMinerMachine)place(player,legacyStack,POS);
        check(!moved.getRecipeLogic().isDone()&&moved.getRecipeLogic().getProgress()==0,"legacy item resets job");
        check(!moved.getRecipeLogic().isWorkingEnabled(),"legacy item retains pause");
        level.setBlock(POS,Blocks.AIR.defaultBlockState(),3);
        report.append("PASS new and legacy item runtime reset, pause retained\n");
        if(level.getBlockEntity(SAVED) instanceof com.gregtechceu.gtceu.common.machine.steam.SteamMinerMachine &&
                level.getBlockEntity(LEGACY) instanceof com.gregtechceu.gtceu.common.machine.steam.SteamMinerMachine) {
            report.append("PASS previous restart/active-mining fixtures preserved with their outputs\n");
            return;
        }
        check(level.isEmptyBlock(SAVED)&&level.isEmptyBlock(LEGACY),"restart fixtures occupied");
        level.setBlock(SAVED,definition.defaultBlockState(),3);
        machine=(com.gregtechceu.gtceu.common.machine.steam.SteamMinerMachine)level.getBlockEntity(SAVED);logic=machine.getRecipeLogic();
        logic.initPos(SAVED,logic.getCurrentRadius());logic.setWorkingEnabled(false);set(logic,"miningStateVersion",1);
        var queue=(LinkedList<BlockPos>)get(logic,"blocksToMine");queue.add(SAVED.below(3));queue.add(SAVED.below(7));set(logic,"blocksToMineOriginalCount",9);
        level.setBlock(LEGACY,definition.defaultBlockState(),3);
        machine=(com.gregtechceu.gtceu.common.machine.steam.SteamMinerMachine)level.getBlockEntity(LEGACY);logic=machine.getRecipeLogic();
        logic.initPos(new BlockPos(-512,96,512),logic.getCurrentRadius());set(logic,"isDone",true);logic.setWorkingEnabled(false);set(logic,"miningStateVersion",0);machine.setChanged();
        report.append("PASS saved queue and old relocated-Done migration fixtures prepared; restart required\n");
    }
    public static void verify(net.minecraft.server.MinecraftServer server){
        report=new StringBuilder();try{
            var level=server.overworld();
            var logic=((com.gregtechceu.gtceu.common.machine.steam.SteamMinerMachine)level.getBlockEntity(SAVED)).getRecipeLogic();
            check(get(logic,"blocksToMine").equals(new LinkedList<>(List.of(SAVED.below(3),SAVED.below(7)))),"queue reload exactly");
            check((int)get(logic,"blocksToMineOriginalCount")==9,"original count reload");
            check(!logic.isWorkingEnabled(),"saved pause");
            report.append("PASS queue, original batch count and pause survive real world restart\n");
            logic=((com.gregtechceu.gtceu.common.machine.steam.SteamMinerMachine)level.getBlockEntity(LEGACY)).getRecipeLogic();
            check(!logic.isDone()&&logic.getStartX()==LEGACY.getX()-logic.getCurrentRadius()&&logic.getStartY()==LEGACY.getY(),"legacy relocated Done migration");
            check(!logic.isWorkingEnabled(),"migration retains pause");
            check((int)get(logic,"miningStateVersion")==1,"migration schema recorded");
            report.append("PASS legacy relocated Done world migrates to actual origin, pause retained\n");
        }catch(Throwable e){report.append("FAIL "+e+"\n");e.printStackTrace();}write("machine-state-restart-results.txt",report.toString());
    }
    static void write(String file,String text){try{Files.writeString(net.neoforged.fml.loading.FMLPaths.GAMEDIR.get().resolve(file),text);}catch(Exception e){throw new RuntimeException(e);}}
}
