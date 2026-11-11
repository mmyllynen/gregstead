package regression.ui;
import java.nio.file.*;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.gui.factory.MachineUIFactory;
import net.minecraft.core.BlockPos;
@Mod("gregstead_ui_regression")
public class ServerProbe {
 public ServerProbe(){NeoForge.EVENT_BUS.addListener(this::tick);}
 void tick(ServerTickEvent.Post e){
  var server=e.getServer();var root=server.getServerDirectory();
  if(!root.toAbsolutePath().normalize().endsWith(".build/lathe-server"))return;
  Path cmd=root.resolve("ui-command.txt");if(!Files.exists(cmd))return;
  try{
   String c=Files.readString(cmd).strip();Files.delete(cmd);StringBuilder out=new StringBuilder(c+"\n");
   var player=server.getPlayerList().getPlayerByName("GregsteadTest");
   if(c.equals("stop")){server.halt(false);return;}
   if(c.equals("templates")){
    for(var type:java.util.List.of(GTRecipeTypes.LATHE_RECIPES,GTRecipeTypes.MIXER_RECIPES)){
     out.append(type).append(" custom=").append(type.getRecipeUI().hasCustomUI()).append('\n');
     ClientIndependentDump.dump(type.getRecipeUI().createEditableUITemplate(false,false).createDefault(),out,"");
    }
   }else if(c.startsWith("command "))server.getCommands().performPrefixedCommand(server.createCommandSourceStack(),c.substring(8));
   else if(c.equals("dump")){ClientIndependentDump.menu(player.containerMenu,out);}
   else if(c.startsWith("seed ")){
    String[] a=c.split(" ");int index=Integer.parseInt(a[1]),count=Integer.parseInt(a[3]);
    var item=net.minecraft.core.registries.BuiltInRegistries.ITEM.getValue(net.minecraft.resources.Identifier.parse(a[2]));
    if(item==null)throw new IllegalArgumentException(a[2]);
    if(!player.containerMenu.getSlot(index).getItem().isEmpty())throw new IllegalStateException("Refusing to overwrite test items");
    player.containerMenu.getSlot(index).set(new net.minecraft.world.item.ItemStack(item,count));player.containerMenu.broadcastChanges();ClientIndependentDump.menu(player.containerMenu,out);
   }else if(c.equals("power") || c.equals("disable")){
    var machine=(com.gregtechceu.gtceu.api.machine.WorkableTieredMachine)MetaMachine.getMachine(server.overworld(),new BlockPos(1,160,10));
    if(c.equals("power"))machine.energyContainer.setEnergyStored(machine.energyContainer.getEnergyCapacity());machine.recipeLogic.setWorkingEnabled(c.equals("power"));
    out.append("energy=").append(machine.energyContainer.getEnergyStored());
   }else if(c.startsWith("open ")){
    String[] a=c.split(" ");var pos=new BlockPos(Integer.parseInt(a[1]),Integer.parseInt(a[2]),Integer.parseInt(a[3]));
    var machine=MetaMachine.getMachine(server.overworld(),pos);
    if(player==null || machine==null)throw new IllegalStateException("Missing player/machine");
    MachineUIFactory.INSTANCE.openUI(machine,player);
    ClientIndependentDump.menu(player.containerMenu,out);
   }else throw new IllegalArgumentException(c);
   Files.writeString(root.resolve("ui-result.txt"),out);
  }catch(Exception ex){try{Files.writeString(root.resolve("ui-result.txt"),"ERROR: "+ex);}catch(Exception io){throw new RuntimeException(io);}}
 }
}
