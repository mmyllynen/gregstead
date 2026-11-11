package regression.ui;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.lowdragmc.lowdraglib.editor.data.NativeUIProjectLoader;
import com.lowdragmc.lowdraglib.gui.widget.*;
import net.minecraft.nbt.*;
import java.util.*;
final class TemplateChecks {
 static String run(){
  StringBuilder report=new StringBuilder();
  for(String name:List.of("lathe","forge_hammer","assembly_line","distillation_tower","research_station")){
   var type=Objects.requireNonNull(GTRecipeTypes.get(name));var raw=type.getRecipeUI().getCustomUI();var before=raw.copy();
   var expected=new ArrayList<String>();collectNBT(raw.getCompoundOrEmpty("root"),expected);
   var group=new WidgetGroup();NativeUIProjectLoader.load(group,raw,GTRegistries.builtinRegistry());
   var actual=ids(group);if(!actual.equals(expected))throw new AssertionError(name+" expected="+expected+" actual="+actual);
   if(!raw.equals(before))throw new AssertionError("Mutated cached project");
   // Independent current-format fixture: no editor serialization or textures required.
   var modern=raw.copy();var currentRoot=new CompoundTag();var additional=new CompoundTag();var children=new ListTag();
   var wrapper=new CompoundTag();wrapper.putString("type","gtm_item_slot");var slot=new CompoundTag();slot.putString("id","current_slot");wrapper.put("data",slot);children.add(wrapper);additional.put("children",children);currentRoot.put("_additional",additional);modern.put("root",currentRoot);
   var roundtrip=new WidgetGroup();NativeUIProjectLoader.load(roundtrip,modern,GTRegistries.builtinRegistry());
   if(!ids(roundtrip).equals(List.of("SlotWidget:current_slot")))throw new AssertionError(name+" current format failed");
   var mixed=modern.copy();mixed.getCompoundOrEmpty("root").put("children",raw.getCompoundOrEmpty("root").getListOrEmpty("children").copy());
   var duplicate=new WidgetGroup();NativeUIProjectLoader.load(duplicate,mixed,GTRegistries.builtinRegistry());
   if(!ids(duplicate).equals(List.of("SlotWidget:current_slot")))throw new AssertionError(name+" mixed format duplicated children");
   report.append(name).append(" legacy/current/mixed/cache PASS ").append(actual).append('\n');
  }
  return report.toString();
 }
 static List<String> ids(WidgetGroup group){var result=new ArrayList<String>();collect(group,result);return result;}
 static void collect(Widget w,List<String> result){if(w instanceof SlotWidget || w instanceof com.gregtechceu.gtceu.api.gui.widget.TankWidget)result.add(w.getClass().getSimpleName()+":"+w.getId());if(w instanceof WidgetGroup g)for(var child:g.widgets)collect(child,result);}
 static void collectNBT(CompoundTag data,List<String> result){
  for(var child:data.getListOrEmpty("children"))if(child instanceof CompoundTag wrapper){var d=wrapper.getCompoundOrEmpty("data");String type=wrapper.getStringOr("type","");
   if(type.equals("gtm_item_slot"))result.add("SlotWidget:"+d.getStringOr("id",""));
   if(type.equals("gtm_fluid_slot"))result.add("TankWidget:"+d.getStringOr("id",""));
   collectNBT(d,result);
  }
 }
}
