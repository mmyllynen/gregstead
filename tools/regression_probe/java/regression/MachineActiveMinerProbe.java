package regression;
import com.gregtechceu.gtceu.common.data.GTMachines;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.machine.steam.SteamMinerMachine;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import java.util.*;

public final class MachineActiveMinerProbe {
    static final BlockPos ACTIVE=new BlockPos(125,160,5);
    static final BlockPos ORE=ACTIVE.below(3);
    public static void start(net.minecraft.server.MinecraftServer server){
        var report=new StringBuilder();try{
            var player=server.getPlayerList().getPlayers().getFirst();var level=player.level();
            MachineStateProbe.check(server.getWorldData().getLevelName().equals("Gregstead-Regression"),"world");
            MachineStateProbe.check(level.isEmptyBlock(ACTIVE)&&level.isEmptyBlock(ORE),"active fixture occupied");
            var def=GTMachines.STEAM_MINER.second();level.setBlock(MachineStateProbe.POS,def.defaultBlockState(),3);
            var old=(SteamMinerMachine)level.getBlockEntity(MachineStateProbe.POS);old.getRecipeLogic().setCurrentRadius(1);old.getRecipeLogic().initPos(new BlockPos(-512,96,512),1);MachineStateProbe.set(old.getRecipeLogic(),"isDone",true);
            old.steamTank.setFluidInTank(0,GTMaterials.Steam.getFluid(16000));var item=def.asStack();old.saveToItem(item,level.registryAccess());level.setBlock(MachineStateProbe.POS,Blocks.AIR.defaultBlockState(),3);
            var moved=(SteamMinerMachine)MachineStateProbe.place(player,item,ACTIVE);
            MachineStateProbe.check(!moved.getRecipeLogic().isDone(),"Done reset on relocation");
            level.setBlock(ORE,Blocks.DIAMOND_ORE.defaultBlockState(),3);
            // The reloaded pending queue contains real, not-yet-mined ore at both positions.
            var resumed=(SteamMinerMachine)level.getBlockEntity(MachineStateProbe.SAVED);
            for(var p:List.of(MachineStateProbe.SAVED.below(3),MachineStateProbe.SAVED.below(7)))level.setBlock(p,Blocks.DIAMOND_ORE.defaultBlockState(),3);
            resumed.steamTank.setFluidInTank(0,GTMaterials.Steam.getFluid(16000));resumed.getRecipeLogic().setWorkingEnabled(true);
            report.append("PASS relocated formerly Done miner placed with 16000mB steam; real ore and restored pending-queue ores prepared\n");
        }catch(Throwable e){report.append("FAIL "+e+"\n");e.printStackTrace();}MachineStateProbe.write("machine-active-start-results.txt",report.toString());
    }
    public static void verify(net.minecraft.server.MinecraftServer server){
        var report=new StringBuilder();try{
            var level=server.overworld();var moved=(SteamMinerMachine)level.getBlockEntity(ACTIVE);var resumed=(SteamMinerMachine)level.getBlockEntity(MachineStateProbe.SAVED);
            MachineStateProbe.check(!level.getBlockState(ORE).is(Blocks.DIAMOND_ORE),"moved miner mined new ore");
            MachineStateProbe.check(moved.getRecipeLogic().getStartX()==ACTIVE.getX()-1,"new mining origin");
            MachineStateProbe.check(moved.exportItems.getStackInSlot(0).getCount()>0,"moved miner output");
            for(var p:List.of(MachineStateProbe.SAVED.below(3),MachineStateProbe.SAVED.below(7)))MachineStateProbe.check(!level.getBlockState(p).is(Blocks.DIAMOND_ORE),"reloaded queue mines "+p);
            MachineStateProbe.check(resumed.exportItems.getStackInSlot(0).getCount()>0,"resumed miner output");
            moved.getRecipeLogic().setWorkingEnabled(false);resumed.getRecipeLogic().setWorkingEnabled(false);
            report.append("PASS moved Done miner mines at new origin and produces output using actual server ticks\nPASS restored pending queue mines both saved ore positions and produces output\n");
        }catch(Throwable e){report.append("FAIL "+e+"\n");e.printStackTrace();}MachineStateProbe.write("machine-active-results.txt",report.toString());
    }
}
