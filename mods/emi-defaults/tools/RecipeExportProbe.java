package neogreg.emidefaultsexport;
import java.nio.file.*;
import java.util.*;
import com.google.gson.*;
import net.minecraft.client.Minecraft;
import net.neoforged.fml.common.Mod;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import dev.emi.emi.api.EmiApi;
import dev.emi.emi.api.recipe.*;
import dev.emi.emi.api.stack.*;
import dev.emi.emi.api.stack.serializer.EmiIngredientSerializer;
import dev.emi.emi.bom.BoM;
import dev.emi.emi.runtime.EmiReloadManager;
@Mod(value="neogreg_recipe_export",dist=Dist.CLIENT)
public class RecipeExportProbe {
 final Gson gson=new Gson();final JsonObject ingredients=new JsonObject();boolean done;int ticks;
 public RecipeExportProbe(){NeoForge.EVENT_BUS.addListener(this::tick);}
 final Map<EmiIngredient,String> canonical=new HashMap<>();final JsonObject aliases=new JsonObject();
 EmiIngredient normalized(EmiIngredient in){var copy=in.copy().setAmount(1).setChance(1);if(copy instanceof EmiStack s)s.setRemainder(EmiStack.EMPTY);return copy;}
 String identity(EmiIngredient in){var copy=normalized(in);var value=EmiIngredientSerializer.getSerialized(copy);if(value==null)return null;String raw=value.toString();String key=canonical.get(copy);if(key==null){key=raw;canonical.put(copy,key);}aliases.addProperty(raw,key);return key;}
 String atom(EmiStack in){String key=identity(in);if(key==null)return null;if(!ingredients.has(key)){JsonObject data=new JsonObject();data.add("value",JsonParser.parseString(key));JsonArray stacks=new JsonArray();stacks.add(key);data.add("stacks",stacks);data.addProperty("tag",false);ingredients.add(key,data);}return key;}
 String ingredient(EmiIngredient in){if(in instanceof EmiStack s)return atom(s);String key=identity(in);if(key==null)return null;if(!ingredients.has(key)){
  JsonObject data=new JsonObject();data.add("value",JsonParser.parseString(key));JsonArray stacks=new JsonArray();Set<String> seen=new HashSet<>();for(var s:in.getEmiStacks()){String sk=atom(s);if(sk!=null&&seen.add(sk))stacks.add(sk);}data.add("stacks",stacks);data.addProperty("tag",in instanceof TagEmiIngredient);ingredients.add(key,data);
 }return key;}
 JsonObject member(EmiIngredient in){JsonObject j=new JsonObject();j.addProperty("key",ingredient(in));j.addProperty("amount",in.getAmount());j.addProperty("chance",in.getChance());j.addProperty("catalyst",!in.getEmiStacks().isEmpty()&&in.getEmiStacks().stream().allMatch(s->s.getRemainder().isEqual(s)));return j;}
 void tick(ClientTickEvent.Post event){if(done)return;var m=Minecraft.getInstance();if(m.player==null||!EmiReloadManager.isLoaded())return;if(++ticks<120)return;done=true;try{
  if(net.neoforged.fml.ModList.get().isLoaded("neogreg_emi_defaults"))throw new IllegalStateException("Exclude neogreg_emi_defaults from the export instance; do not train defaults on their own previous output");
  JsonObject result=new JsonObject();result.addProperty("schemaVersion",1);result.addProperty("minecraft","26.3");result.addProperty("neoforge","26.3.0.77");JsonArray recipes=new JsonArray();JsonArray skipped=new JsonArray();
  var ordered=new ArrayList<>(EmiApi.getRecipeManager().getRecipes());ordered.sort(Comparator.comparing(r->r.getId()==null?"":r.getId().toString()));for(var r:ordered){
   if(r.getId()==null){skipped.add("null-id:"+r.getClass().getName());continue;}
   if(EmiApi.getRecipeManager().getRecipe(r.getId())!=r){skipped.add("duplicate-id:"+r.getId());continue;}
   JsonObject j=new JsonObject();j.addProperty("id",r.getId().toString());j.addProperty("category",r.getCategory().getId().toString());j.addProperty("class",r.getClass().getName());j.addProperty("supported",r.supportsRecipeTree());JsonArray inputs=new JsonArray(),outputs=new JsonArray();for(var i:r.getInputs())if(!i.isEmpty())inputs.add(member(i));for(var o:r.getOutputs())if(!o.isEmpty())outputs.add(member(o));j.add("inputs",inputs);j.add("outputs",outputs);
   if(r.getClass().getName().endsWith("GTEmiRecipe"))try{var f=r.getClass().getDeclaredField("recipe");f.setAccessible(true);Object gt=f.get(r);Object energy=gt.getClass().getMethod("getInputEUt").invoke(gt);long voltage=((Number)energy.getClass().getMethod("voltage").invoke(energy)).longValue();j.addProperty("voltage",voltage);}catch(ReflectiveOperationException ex){throw new RuntimeException("Cannot export GregTech voltage for "+r.getId(),ex);}
   recipes.add(j);
  }
  JsonArray defaults=new JsonArray();for(var e:BoM.defaultRecipes.entrySet())if(e.getValue().getId()!=null){JsonObject d=new JsonObject();d.addProperty("key",ingredient(e.getKey()));d.addProperty("recipe",e.getValue().getId().toString());defaults.add(d);}
  result.add("semanticAliases",aliases);result.add("recipes",recipes);result.add("ingredients",ingredients);result.add("existingDefaults",defaults);result.add("skippedWithoutId",skipped);Files.writeString(Path.of("recipe-export.json"),gson.toJson(result));System.out.println("NEOGREG_EXPORT recipes="+recipes.size()+" ingredients="+ingredients.size());
 }catch(Throwable e){e.printStackTrace();try{Files.writeString(Path.of("export-error.txt"),e.toString());}catch(Exception x){x.printStackTrace();}}m.stop();}
}
