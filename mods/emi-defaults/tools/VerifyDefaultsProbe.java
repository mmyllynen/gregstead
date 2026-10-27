package neogreg.emidefaultsverify;
import com.google.gson.*;
import java.nio.file.*;
import java.util.*;
import net.minecraft.client.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Items;
import net.neoforged.fml.common.Mod;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import dev.emi.emi.api.EmiApi;
import dev.emi.emi.api.recipe.*;
import dev.emi.emi.api.stack.*;
import dev.emi.emi.api.stack.serializer.EmiIngredientSerializer;
import dev.emi.emi.bom.*;
import dev.emi.emi.runtime.EmiReloadManager;
import dev.emi.emi.screen.BoMScreen;
@Mod(value="neogreg_defaults_verify",dist=Dist.CLIENT)
public class VerifyDefaultsProbe {
 int ticks,phase;boolean done;String mode=System.getProperty("defaults.mode","first");JsonObject result=new JsonObject();Gson gson=new GsonBuilder().setPrettyPrinting().create();
 public VerifyDefaultsProbe(){NeoForge.EVENT_BUS.addListener(this::tick);}
 void check(boolean v,String s){if(!v)throw new IllegalStateException(s);}
 void finish(Minecraft m,boolean pass,String why){done=true;result.addProperty("passed",pass);result.addProperty("reason",why);try{Files.writeString(Path.of("proof-"+mode+".json"),gson.toJson(result));}catch(Exception e){throw new RuntimeException(e);}m.stop();}
 int count(MaterialNode n){int sum=1;if(n.children!=null)for(var child:n.children)sum+=count(child);return sum;}
 int depth(MaterialNode n){int d=0;if(n.children!=null)for(var child:n.children)d=Math.max(d,depth(child));return d+1;}
 JsonObject tree(String item){var stack=EmiStack.of(BuiltInRegistries.ITEM.getValue(Identifier.parse(item)));var r=BoM.getRecipe(stack);check(r!=null,"No default recipe for "+item);long start=System.nanoTime();var tree=new MaterialTree(r);tree.calculateCost();JsonObject j=new JsonObject();j.addProperty("recipe",r.getId().toString());j.addProperty("nodes",count(tree.goal));j.addProperty("depth",depth(tree.goal));j.addProperty("milliseconds",(System.nanoTime()-start)/1000000.0);j.addProperty("baseResources",tree.cost.costs.size());for(var cost:tree.cost.costs.values())check(cost.amount>=0,"Negative material cost for "+item);return j;}
 EmiRecipe batchRecipe(String id,EmiStack in,EmiStack out,EmiStack extra){return new BasicEmiRecipe(dev.emi.emi.api.recipe.VanillaEmiRecipeCategories.CRAFTING,Identifier.parse("neogreg_test:"+id),0,0){ {inputs.add(in);outputs.add(out);if(!extra.isEmpty())outputs.add(extra);}public void addWidgets(dev.emi.emi.api.widget.WidgetHolder holder){} };}
 void checkQuantities(){var stage=batchRecipe("stage",EmiStack.of(Items.COAL).setAmount(2),EmiStack.of(Items.EMERALD).setAmount(3),EmiStack.EMPTY);var root=batchRecipe("root",EmiStack.of(Items.EMERALD).setAmount(3),EmiStack.of(Items.DIAMOND).setAmount(2),EmiStack.of(Items.REDSTONE));var tree=new MaterialTree(root);tree.resolutions.put(EmiStack.of(Items.EMERALD),stage);tree.resolutions.put(EmiStack.of(Items.COAL),null);tree.goal.amount=1;tree.recalculate();tree.calculateCost();check(tree.cost.costs.get(EmiStack.of(Items.COAL)).amount==2,"Batch input quantity");check(tree.cost.remainders.get(EmiStack.of(Items.DIAMOND)).amount==1,"Batch target leftover");check(tree.cost.remainders.get(EmiStack.of(Items.REDSTONE)).amount==1,"Multi-output byproduct");result.addProperty("batchQuantitiesAndLeftoversVerified",true);}
 void tick(ClientTickEvent.Post e){if(done)return;var m=Minecraft.getInstance();if(m.player==null||!EmiReloadManager.isLoaded())return;ticks++;try{
 if(phase==0&&ticks>140){
  JsonObject data;try(var stream=m.getResourceManager().getResource(Identifier.parse("emi:recipe/defaults/neogreg.json")).orElseThrow().openAsReader()){data=JsonParser.parseReader(stream).getAsJsonObject();}
  int verified=0;for(var entry:data.getAsJsonObject("resolutions").entrySet())for(var value:entry.getValue().getAsJsonArray()){var ingredient=EmiIngredientSerializer.getDeserialized(value);var recipe=BoM.defaultRecipes.get(ingredient);check(recipe!=null&&entry.getKey().equals(recipe.getId().toString()),"Default resolution missing: "+value);verified++;}
  int tags=0;for(var entry:data.getAsJsonObject("tags").entrySet()){var ingredient=EmiIngredientSerializer.getDeserialized(new JsonPrimitive(entry.getKey()));var actual=BoM.defaultRecipes.get(ingredient);var value=EmiIngredientSerializer.getDeserialized(entry.getValue());check(actual instanceof EmiResolutionRecipe&&((EmiResolutionRecipe)actual).stack.equals(value),"Tag resolution missing: "+entry.getKey());tags++;}
  result.addProperty("verifiedOutputDefaults",verified);result.addProperty("verifiedTagDefaults",tags);result.addProperty("defaultMapSize",BoM.defaultRecipes.size());
  checkQuantities();JsonObject cases=new JsonObject();for(String item:new String[]{"ae2:wireless_terminal","ae2:wireless_crafting_terminal","minecraft:ender_chest","gtceu:lv_assembler","gtceu:max_substation_output_hatch_64a"})cases.add(item,tree(item));result.add("trees",cases);check(cases.getAsJsonObject("ae2:wireless_terminal").get("depth").getAsInt()>3,"AE2 tree did not expand");
  var stick=EmiStack.of(Items.STICK);
  if(mode.equals("restart")){var stored=JsonParser.parseString(Files.readString(Path.of("user-override.json"))).getAsJsonObject();check(BoM.getRecipe(stick).getId().toString().equals(stored.get("recipe").getAsString()),"Personal choice did not survive restart");result.addProperty("personalOverridePersisted",true);}
  else {var before=BoM.getRecipe(stick);var alt=EmiApi.getRecipeManager().getRecipesByOutput(stick).stream().filter(r->r.supportsRecipeTree()&&r.getId()!=null&&r!=before).findFirst().orElseThrow();BoM.addRecipe(alt);check(BoM.getRecipe(stick)==alt,"Personal choice did not override resource");JsonObject stored=new JsonObject();stored.addProperty("recipe",alt.getId().toString());Files.writeString(Path.of("user-override.json"),stored.toString());result.addProperty("personalOverrideWorks",true);}
  var stack=EmiStack.of(BuiltInRegistries.ITEM.getValue(Identifier.parse("ae2:wireless_terminal")));BoM.setGoal(BoM.getRecipe(stack));m.gui.setScreen(new BoMScreen(null));phase=1;ticks=0;
 }else if(phase==1&&ticks>80){Screenshot.grab(Path.of(".").toFile(),"ae2-expanded-"+mode+".png",m.gameRenderer.mainRenderTarget(),1,c->{});phase=2;ticks=0;
 }else if(phase==2&&ticks>40){finish(m,true,"All generated resolutions loaded; recipe trees and personal override verified");}
 }catch(Throwable t){t.printStackTrace();finish(m,false,t.toString());}}
}
