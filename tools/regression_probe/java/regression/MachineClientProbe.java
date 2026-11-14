package regression;
import net.minecraft.core.BlockPos;
import com.gregtechceu.gtceu.api.machine.*;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.api.machine.property.GTMachineModelProperties;
import net.minecraft.world.level.block.Blocks;

public final class MachineClientProbe {
    static final BlockPos POS=new BlockPos(126,160,8);
    public static void prepare(net.minecraft.server.MinecraftServer server){
        try{
            var p=server.getPlayerList().getPlayers().getFirst();var level=p.level();
            MachineStateProbe.check(server.getWorldData().getLevelName().equals("Gregstead-Regression"),"world");MachineStateProbe.check(level.isEmptyBlock(POS) || (level.getBlockEntity(POS) instanceof SimpleTieredMachine prior && prior.getPaintingColor()==0x336699 && prior.energyContainer.getEnergyStored()==1234),"client fixture occupied");
            var def=GTRegistries.MACHINES.getValue(net.minecraft.resources.Identifier.parse("gtceu:lv_macerator"));
            level.setBlock(POS,def.defaultBlockState(),3);var m=(SimpleTieredMachine)level.getBlockEntity(POS);
            m.setPaintingColor(0x336699);m.energyContainer.setEnergyStored(1234);m.getRecipeLogic().setWorkingEnabled(false);
            var stack=def.asStack();m.saveToItem(stack,level.registryAccess());level.setBlock(POS,Blocks.AIR.defaultBlockState(),3);
            MachineStateProbe.place(p,stack,POS);
            MachineStateProbe.write("machine-client-prepare-results.txt","PASS painted and charged machine moved through actual item placement\n");
        }catch(Throwable ex){MachineStateProbe.write("machine-client-prepare-results.txt","FAIL "+ex);ex.printStackTrace();}
    }
    public static void verify(net.minecraft.client.Minecraft mc,String label){
        try{
            var m=(SimpleTieredMachine)mc.level.getBlockEntity(POS);
            MachineStateProbe.check(m!=null&&m.getPaintingColor()==0x336699,"client paint");
            MachineStateProbe.check(m.isPainted(),"client paint status");
            if(m.getRenderState().hasProperty(GTMachineModelProperties.IS_PAINTED))
                MachineStateProbe.check(m.getRenderState().getValue(GTMachineModelProperties.IS_PAINTED),"client painted model state");
            var iris=Class.forName("net.irisshaders.iris.Iris");var config=iris.getMethod("getIrisConfig").invoke(null);
            boolean shaders=(boolean)config.getClass().getMethod("areShadersEnabled").invoke(config);
            MachineStateProbe.check(shaders==label.equals("shaders-on"),"shader mode "+label);
            MachineStateProbe.check(m.energyContainer.getEnergyStored()==1234,"client energy");
            MachineStateProbe.check(!m.getRecipeLogic().isWorkingEnabled(),"client pause");
            MachineStateProbe.write("machine-client-"+label+"-results.txt","PASS client immediately receives stored paint/model, 1234EU and pause after actual item placement\n");
        }catch(Throwable ex){MachineStateProbe.write("machine-client-"+label+"-results.txt","FAIL "+ex);ex.printStackTrace();}
    }
    public static void shaders(boolean enabled)throws Exception{
        var iris=Class.forName("net.irisshaders.iris.Iris");var config=iris.getMethod("getIrisConfig").invoke(null);config.getClass().getMethod("setShadersEnabled",boolean.class).invoke(config,enabled);config.getClass().getMethod("save").invoke(config);iris.getMethod("reload").invoke(null);
    }
}
