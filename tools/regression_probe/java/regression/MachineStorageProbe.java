package regression;
import com.gregtechceu.gtceu.api.machine.*;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.common.data.item.GTDataComponents;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.fluids.FluidStack;
import net.minecraft.world.level.material.Fluids;
import java.util.*;

public final class MachineStorageProbe {
    public static void run(net.minecraft.server.MinecraftServer server){
        var report=new StringBuilder();var p=server.getPlayerList().getPlayers().getFirst();var level=p.level();var pos=MachineStateProbe.POS;
        MachineConservationProbe.player=p;
        try{
            MachineStateProbe.check(server.getWorldData().getLevelName().equals("Gregstead-Regression"),"world");
            MachineStateProbe.check(level.isEmptyBlock(pos),"fixture occupied");
            for(String id:List.of("wood_drum","steel_drum","iv_quantum_chest","iv_quantum_tank","creative_chest","creative_tank")){
                var def=GTRegistries.MACHINES.getValue(net.minecraft.resources.Identifier.parse("gtceu:"+id));MachineStateProbe.check(def!=null,"definition "+id);
                level.setBlock(pos,def.defaultBlockState(),3);var m=(MetaMachine)level.getBlockEntity(pos);
                if(id.endsWith("drum")){
                    var tank=(com.gregtechceu.gtceu.api.machine.trait.NotifiableFluidTank)MachineStateProbe.get(m,"cache");tank.setFluidInTank(0,new FluidStack(Fluids.WATER,12000));MachineStateProbe.set(m,"stored",new FluidStack(Fluids.WATER,12000));
                }else if(id.endsWith("chest")){
                    MachineStateProbe.set(m,"stored",new ItemStack(Items.DIAMOND));MachineStateProbe.set(m,"storedAmount",100000L);
                }else{
                    MachineStateProbe.set(m,"stored",new FluidStack(Fluids.WATER,1));MachineStateProbe.set(m,"storedAmount",1000000L);
                }
                m.setOwnerUUID(p.getUUID());var before=MachineConservationProbe.drops();var expected=def.asStack();m.saveToItem(expected,level.registryAccess());
                var stack=MachineConservationProbe.breakMachine(def,before);MachineStateProbe.check(ItemStack.isSameItemSameComponents(stack,expected),"loot components "+id);
                var restored=MachineStateProbe.place(p,stack.copy(),pos);var actual=def.asStack();restored.saveToItem(actual,level.registryAccess());MachineStateProbe.check(ItemStack.isSameItemSameComponents(expected,actual),"typed storage roundtrip "+id+" expected="+expected+" actual="+actual);
                report.append("PASS "+id+" typed storage survives actual break/place, exact component equality\n");
                level.setBlock(pos,Blocks.AIR.defaultBlockState(),3);MachineConservationProbe.drops().stream().filter(e->!before.contains(e)).forEach(net.minecraft.world.entity.item.ItemEntity::discard);
            }
            var def=GTRegistries.MACHINES.getValue(net.minecraft.resources.Identifier.parse("gtceu:lv_macerator"));
            var m=MachineStateProbe.place(p,def.asStack(),pos);var stack=def.asStack();m.saveToItem(stack,level.registryAccess());MachineStateProbe.check(ItemStack.isSameItemSameComponents(def.asStack(),stack),"empty machine stacks with fresh craft");report.append("PASS empty default machine stacks with fresh craft\n");level.setBlock(pos,Blocks.AIR.defaultBlockState(),3);
        }catch(Throwable ex){report.append("FAIL "+ex+"\n");ex.printStackTrace();}MachineStateProbe.write("machine-storage-results.txt",report.toString());
    }
}
