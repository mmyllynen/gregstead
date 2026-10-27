import org.joml.Matrix3x2fStack;
import fi.neogreg.uifixes.mixin.InventoryProfilesForegroundMixin;
public class UiStackContract {
 public static void main(String[] args)throws Exception {
  var fix=new InventoryProfilesForegroundMixin(){};
  var method=InventoryProfilesForegroundMixin.class.getDeclaredMethod("neogreg$omitUnmatchedPush",Matrix3x2fStack.class);method.setAccessible(true);
  var pose=new Matrix3x2fStack(8);pose.translate(111,77);pose.pushMatrix();pose.translate(35,21);
  if(method.invoke(fix,pose)!=pose)throw new AssertionError("Changed matrix instance");
  if(pose.m20()!=146||pose.m21()!=98)throw new AssertionError("Changed transform");
  pose.popMatrix();
  if(pose.m20()!=111||pose.m21()!=77)throw new AssertionError("Leaked matrix scope");
  for(int i=0;i<100;i++)method.invoke(fix,pose);
  try{pose.popMatrix();throw new AssertionError("Unmatched push remains");}catch(IllegalStateException expected){}
  System.out.println("IPN scope preserved for nested and repeated frames");
 }
}
