package regression;

import com.gregtechceu.gtceu.api.machine.*;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.api.machine.trait.*;
import com.gregtechceu.gtceu.api.transfer.item.CustomItemStackHandler;
import com.gregtechceu.gtceu.common.data.item.GTDataComponents;
import net.minecraft.core.*;
import net.minecraft.world.item.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.level.block.Blocks;
import java.util.*;

public final class MachineConservationProbe {
    static final BlockPos POS=MachineStateProbe.POS;
    static net.minecraft.server.level.ServerPlayer player;
    static StringBuilder report;
    static void check(boolean v,String s){MachineStateProbe.check(v,s);}
    static Object get(Object o,String n)throws Exception{return MachineStateProbe.get(o,n);}
    static void set(Object o,String n,Object v)throws Exception{MachineStateProbe.set(o,n,v);}
    static CustomItemStackHandler inventory(Object o)throws Exception {
        if(o instanceof CustomItemStackHandler h)return h;
        if(o instanceof NotifiableItemStackHandler h)return h.storage;
        return inventory(get(o,"storage"));
    }
    static List<ItemEntity> drops(){return player.level().getEntitiesOfClass(ItemEntity.class,new AABB(POS).inflate(2));}
    static ItemStack breakMachine(MachineDefinition definition,List<ItemEntity> before){
        check(player.level().destroyBlock(POS,true,player),"destroy machine");
        var list=drops().stream().filter(e->!before.contains(e)).toList();
        var machines=list.stream().filter(e->e.getItem().is(definition.getItem())).toList();
        check(machines.size()==1,"exactly one machine drop "+list);
        var result=machines.getFirst().getItem().copy();
        machines.getFirst().discard();return result;
    }
    static void physical(String className,String field,boolean packed)throws Exception {
        for(var definition:GTRegistries.MACHINES){
            var prototype=definition.getBlockEntityType().create(POS,definition.defaultBlockState());
            if(!(prototype.getClass().getSimpleName().equals(className) || (className.equals("PrimitiveWorkableMachine") && prototype.getClass().getSimpleName().equals("PrimitiveBlastFurnaceMachine"))))continue;
            player.level().setBlock(POS,definition.defaultBlockState(),3);
            var machine=(MetaMachine)player.level().getBlockEntity(POS);
            var handler=inventory(get(machine,field));if(handler.getSlots()==0){player.level().setBlock(POS,Blocks.AIR.defaultBlockState(),3);continue;}
            handler.getStacks().set(0,new ItemStack(Items.DIAMOND,7));
            if(packed&&className.equals("CrateMachine"))set(machine,"isTaped",true);
            var before=drops();var stack=breakMachine(definition,before);
            long dropped=drops().stream().filter(e->!before.contains(e)&&e.getItem().is(Items.DIAMOND)).mapToLong(e->e.getItem().getCount()).sum();
            var restored=MachineStateProbe.place(player,stack,POS);var after=inventory(get(restored,field));long stored=after.getStacks().stream().filter(s->s.is(Items.DIAMOND)).mapToLong(ItemStack::getCount).sum();
            check(dropped+stored==7,"conservation "+className+" dropped="+dropped+" stored="+stored);
            check(stored==(packed?7:0),"packing/drop policy "+className);
            report.append("PASS "+className+" "+field+" packed="+packed+" drops="+dropped+" retained="+stored+"\n");
            // Only remove generated test resources, keeping preexisting world items.
            player.level().setBlock(POS,Blocks.AIR.defaultBlockState(),3);
            drops().stream().filter(e->!before.contains(e)).forEach(ItemEntity::discard);return;
        }
        throw new AssertionError("no usable fixture "+className+"."+field);
    }
    static void circuits(boolean ghost)throws Exception{
        var config=com.gregtechceu.gtceu.config.ConfigHolder.INSTANCE.machines;boolean old=config.ghostCircuit;config.ghostCircuit=ghost;
        try{
            var def=GTRegistries.MACHINES.getValue(net.minecraft.resources.Identifier.parse("gtceu:lv_macerator"));
            player.level().setBlock(POS,def.defaultBlockState(),3);var m=(MetaMachine)player.level().getBlockEntity(POS);
            var h=inventory(get(m,"circuitInventory"));var circuit=com.gregtechceu.gtceu.common.item.behavior.IntCircuitBehaviour.stack(13);h.getStacks().set(0,circuit);
            var before=drops();var stack=breakMachine(def,before);var restored=MachineStateProbe.place(player,stack,POS);
            int stored=inventory(get(restored,"circuitInventory")).getStackInSlot(0).getCount();
            int dropped=drops().stream().filter(e->!before.contains(e)&&e.getItem().is(circuit.getItem())).mapToInt(e->e.getItem().getCount()).sum();
            check(stored==(ghost?1:0)&&dropped==(ghost?0:1),"circuit exactly once ghost="+ghost);
            report.append("PASS circuit ghost="+ghost+" drops="+dropped+" retained="+stored+"\n");
            player.level().setBlock(POS,Blocks.AIR.defaultBlockState(),3);drops().stream().filter(e->!before.contains(e)).forEach(ItemEntity::discard);
        }finally{config.ghostCircuit=old;}
    }
    static void ae()throws Exception{
        var item=new appeng.api.stacks.GenericStack(appeng.api.stacks.AEItemKey.of(Items.DIAMOND),117);
        var fluid=new appeng.api.stacks.GenericStack(appeng.api.stacks.AEFluidKey.of(net.minecraft.world.level.material.Fluids.WATER),5007);
        for(String id:List.of("me_input_bus","me_stocking_input_bus","me_input_hatch","me_stocking_input_hatch","me_output_bus","me_output_hatch","me_pattern_buffer")){
            var def=GTRegistries.MACHINES.getValue(net.minecraft.resources.Identifier.parse("gtceu:"+id));check(def!=null,"AE definition "+id);
            player.level().setBlock(POS,def.defaultBlockState(),3);var m=(MetaMachine)player.level().getBlockEntity(POS);
            if(id.contains("input")||id.contains("stocking")){
                boolean bus=id.endsWith("bus");var handler=get(m,bus?"aeItemHandler":"aeFluidHandler");Object[] slots=(Object[])get(handler,"inventory");
                for(int index:new int[]{0,slots.length-1}){set(slots[index],"config",bus?item:fluid);set(slots[index],"stock",bus?item:fluid);}
            }else if(id.contains("output")){
                var buffer=(com.gregtechceu.gtceu.integration.ae2.utils.KeyStorage)get(m,"internalBuffer");buffer.storage.put(item.what(),117);buffer.storage.put(fluid.what(),5007);
            }else{
                Object[] slots=(Object[])get(m,"internalInventory");for(int index:new int[]{0,slots.length-1}){
                    var items=(it.unimi.dsi.fastutil.objects.Object2LongMap<ItemStack>)get(slots[index],"itemInventory");items.put(new ItemStack(Items.DIAMOND),117);
                    var fluids=(it.unimi.dsi.fastutil.objects.Object2LongMap<net.neoforged.neoforge.fluids.FluidStack>)get(slots[index],"fluidInventory");fluids.put(new net.neoforged.neoforge.fluids.FluidStack(net.minecraft.world.level.material.Fluids.WATER,1),5007);
                }
            }
            m.setOwnerUUID(player.getUUID());var expected=MachineItemState.collect(m,player.level().registryAccess());var before=drops();var stack=breakMachine(def,before);
            var restored=MachineStateProbe.place(player,stack,POS);var actual=MachineItemState.collect(restored,player.level().registryAccess());check(expected.equals(actual),"AE roundtrip "+id+" expected="+expected+" actual="+actual);
            if(id.contains("stocking"))check(!actual.toString().contains("stock:"),"virtual ME stock excluded");
            check(drops().stream().filter(e->!before.contains(e)&&e.getItem().is(Items.DIAMOND)).count()==0,"no duplicate AE item drops");
            report.append("PASS "+id+" real stock/config conserved, virtual stock excluded; first and last slots\n");
            player.level().setBlock(POS,Blocks.AIR.defaultBlockState(),3);drops().stream().filter(e->!before.contains(e)).forEach(ItemEntity::discard);
        }
    }
    static void covers()throws Exception{
        var def=GTRegistries.MACHINES.getValue(net.minecraft.resources.Identifier.parse("gtceu:lv_macerator"));player.level().setBlock(POS,def.defaultBlockState(),3);var m=(MetaMachine)player.level().getBlockEntity(POS);int n=0;
        for(var definition:GTRegistries.COVERS){
            var cover=definition.createCoverBehavior(m.getCoverContainer(),Direction.UP);
            cover.onAttached(new ItemStack(Items.STICK),player);
            MachineStateProbe.seed(cover,Collections.newSetFromMap(new IdentityHashMap<>()));
            try {
                var filter=(com.gregtechceu.gtceu.api.cover.filter.FilterHandler<?,?>)get(cover,"filterHandler");
                var filterItem=cover.getClass().getSimpleName().contains("Fluid")||cover instanceof com.gregtechceu.gtceu.common.cover.PumpCover ?
                        com.gregtechceu.gtceu.common.data.GTItems.FLUID_FILTER.asStack() : com.gregtechceu.gtceu.common.data.GTItems.ITEM_FILTER.asStack();
                filter.setFilterItem(filterItem);
                var packed=cover.getPickItem().get(GTDataComponents.COVER_ITEM_DATA);
                boolean inItem=packed!=null&&packed.copyTag().toString().contains("filterItem");
                long separate=cover.getAdditionalDrops().stream().filter(i->i.is(filterItem.getItem())).mapToLong(ItemStack::getCount).sum();
                check((inItem?1:0)+separate==1,"cover filter conservation "+definition.getId()+" inItem="+inItem+" separate="+separate);
            } catch(NoSuchFieldException ignored) {}
            var expected=cover.writeItemState(player.level().registryAccess());var stack=cover.getPickItem();
            var fresh=definition.createCoverBehavior(m.getCoverContainer(),Direction.UP);fresh.onAttached(stack,player);
            check(expected.equals(fresh.writeItemState(player.level().registryAccess())),"cover config "+definition.getId());n++;
        }
        report.append("PASS "+n+" cover configuration roundtrips; filter resources conserved exactly once\n");player.level().setBlock(POS,Blocks.AIR.defaultBlockState(),3);
    }
    public static void run(net.minecraft.server.MinecraftServer server){
        player=server.getPlayerList().getPlayers().getFirst();report=new StringBuilder();var original=player.getMainHandItem();
        try{
            check(server.getWorldData().getLevelName().equals("Gregstead-Regression"),"world");if(player.level().getBlockEntity(POS) instanceof com.gregtechceu.gtceu.integration.ae2.machine.MEInputBusPartMachine || player.level().getBlockEntity(POS) instanceof com.gregtechceu.gtceu.api.machine.SimpleTieredMachine)player.level().setBlock(POS,Blocks.AIR.defaultBlockState(),3);check(player.level().isEmptyBlock(POS),"fixture occupied");
            String[][] cases={{"SimpleTieredMachine","importItems"},{"SimpleSteamMachine","exportItems"},{"BufferMachine","inventory"},{"ItemBusPartMachine","inventory"},{"BatteryBufferMachine","batteryInventory"},{"ChargerMachine","chargerInventory"},{"BlockBreakerMachine","cache"},{"FisherMachine","baitHandler"},{"ItemCollectorMachine","output"},{"MinerMachine","exportItems"},{"SteamMinerMachine","exportItems"},{"MaintenanceHatchPartMachine","itemStackHandler"},{"DataAccessHatchMachine","importItems"},{"ObjectHolderMachine","heldItems"},{"RotorHolderPartMachine","inventory"},{"MufflerPartMachine","inventory"},{"CrateMachine","inventory"},{"PrimitiveWorkableMachine","importItems"}};
            for(var c:cases){try{physical(c[0],c[1],c[0].equals("MufflerPartMachine"));}catch(Throwable e){report.append("FAIL "+Arrays.toString(c)+" "+e+"\n");e.printStackTrace();player.level().setBlock(POS,Blocks.AIR.defaultBlockState(),3);}}
            physical("CrateMachine","inventory",true);circuits(true);circuits(false);ae();covers();
        }catch(Throwable e){report.append("FAIL suite "+e+"\n");e.printStackTrace();}
        finally{player.level().setBlock(POS,Blocks.AIR.defaultBlockState(),3);player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,original);MachineStateProbe.write("machine-conservation-results.txt",report.toString());}
    }
}
