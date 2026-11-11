package regression.ui;
import com.lowdragmc.lowdraglib.gui.widget.*;
import net.minecraft.world.inventory.AbstractContainerMenu;
public class ClientIndependentDump {
 static void dump(Widget w,StringBuilder out,String indent){
  out.append(indent).append(w.getClass().getName()).append(" id=").append(w.getId());
  if(w instanceof SlotWidget slot && slot.getHandler()!=null)out.append(" menuIndex=").append(slot.getHandler().index).append(" storageIndex=").append(slot.getHandler().getContainerSlot());
  out.append('\n');if(w instanceof WidgetGroup g)for(var child:g.widgets)dump(child,out,indent+"  ");
 }
 static void menu(AbstractContainerMenu menu,StringBuilder out){
  out.append("slots=").append(menu.slots.size()).append('\n');
  for(var slot:menu.slots)out.append(slot.index).append(" storageIndex=").append(slot.getContainerSlot()).append(" class=").append(slot.getClass().getName()).append(" item=").append(slot.getItem()).append('\n');
  if(menu instanceof com.lowdragmc.lowdraglib.gui.modular.ModularUIContainer m)dump(m.getModularUI().mainGroup,out,"");
 }
}
