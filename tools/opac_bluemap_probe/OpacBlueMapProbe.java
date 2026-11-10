package regression;

import java.nio.file.*;
import java.util.*;
import java.lang.reflect.Proxy;
import de.bluecolored.bluemap.api.*;
import de.bluecolored.bluemap.api.markers.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.resources.*;
import net.minecraft.core.registries.Registries;
import xaero.pac.common.server.api.OpenPACServerAPI;
import io.github.gaming32.opacbluemapintegration.OpacBluemapIntegration;

/** Local-only probe, run exclusively inside the persistent regression world. */
public class OpacBlueMapProbe {
 static void check(boolean value, String message) { if (!value) throw new AssertionError(message); }
 public static void run(MinecraftServer server) {
  var report = new ArrayList<String>();
  try {
   var root = net.neoforged.fml.loading.FMLPaths.GAMEDIR.get().toRealPath();
   check(root.endsWith(".test-client"), "Wrong profile");
   var pac=OpenPACServerAPI.get(server); var claims=pac.getServerClaimsManager();
   var owner=UUID.fromString("201243e0-0ba8-4779-a40a-a0b8a030be01");
   var other=UUID.fromString("201243e0-0ba8-4779-a40a-a0b8a030be02");
   var overworld=Identifier.parse("minecraft:overworld"); var nether=Identifier.parse("minecraft:the_nether");
   check(claims.getPlayerBaseForceloadLimit(UUID.fromString("201243e0-0ba8-4779-a40a-a0b8a030be03"))==100,"Base limit"); report.add("PASS base forceload limit 100");
   var parties=pac.getPartyManager(); var party=parties.getPartyByOwner(owner);
   if(party==null) party=parties.createPartyForOwner(new net.minecraft.server.players.NameAndId(owner,"OPACRegression"));
   if(party.getMemberInfo(other)==null)party.addMember(other,xaero.pac.common.parties.party.member.PartyMemberRank.MEMBER,"OPACRegressionMember");
   check(claims.getPlayerFullForceloadLimit(owner)==200,"Party limit: actual="+claims.getPlayerFullForceloadLimit(owner)); report.add("PASS two-member party limit 200");
   var solo=UUID.fromString("201243e0-0ba8-4779-a40a-a0b8a030be03");
   var positions=new ArrayList<net.minecraft.world.level.ChunkPos>();
   for(int x=-5;x<=5 && positions.size()<101;x++) for(int z=-5;z<=4 && positions.size()<101;z++) {
    if(x==0&&z==0)continue;
    var existing=claims.get(overworld,x,z);
    check(existing==null||existing.getPlayerId().equals(solo),"Budget fixture occupied");
    positions.add(new net.minecraft.world.level.ChunkPos(x,z));
   }
   // Offline synthetic owner: no terrain loading or exploration is required.
   for(int i=0;i<positions.size();i++) {var pos=positions.get(i);claims.claim(overworld,solo,0,pos.x(),pos.z(),i<99);}
   var hundred=positions.get(99); var extra=positions.get(100);
   var accepted=claims.tryToForceload(overworld,solo,hundred.x(),hundred.z(),hundred.x(),hundred.z(),true,false);
   check(accepted.getResultType()==xaero.pac.common.claims.result.api.ClaimResult.Type.SUCCESSFUL_FORCELOAD,"100th: "+accepted.getResultType());
   var rejected=claims.tryToForceload(overworld,solo,extra.x(),extra.z(),extra.x(),extra.z(),true,false);
   check(rejected.getResultType()==xaero.pac.common.claims.result.api.ClaimResult.Type.FORCELOAD_LIMIT_REACHED,"101st: "+rejected.getResultType());
   report.add("PASS 100th forceload accepted and 101st rejected (offline metadata fixture)");
   // Persist tiny claim fixtures; claim metadata alone does not generate terrain.
   for(var dim:List.of(overworld,nether)) {
    var existing=claims.get(dim,0,0);
    check(existing==null||existing.getPlayerId().equals(owner),"Fixture already owned by someone else");
    claims.claim(dim,owner,0,0,0,false);
   }
   var fake = new TestAPI(); OpacBluemapIntegration.updateClaims(fake);
   check(fake.count(overworld)>0&&fake.count(nether)>0,"Dimension markers");
   report.add("PASS same-coordinate claims in overworld and nether");
   claims.unclaim(nether,0,0); OpacBluemapIntegration.updateClaims(fake);
   check(fake.count(nether)==0&&fake.count(overworld)>0,"Stale dimension marker");
   report.add("PASS unclaim removes nether marker and preserves overworld");
   claims.claim(nether,owner,0,0,0,false);
   var restarted=new TestAPI(); OpacBluemapIntegration.updateClaims(restarted);
   check(restarted.count(overworld)>0&&restarted.count(nether)>0,"Initial resync");
   report.add("PASS fresh map API resync reads existing claims");
   BlueMapAPI.getInstance().ifPresent(api -> { OpacBluemapIntegration.updateClaims(api); report.add("PASS real BlueMap service update: "+api.getMaps().size()+" maps"); });
   Files.write(root.resolve("opac-bluemap-test.txt"),report);
  } catch(Throwable e) {
   report.add("FAIL "+e); e.printStackTrace();
   try { Files.write(net.neoforged.fml.loading.FMLPaths.GAMEDIR.get().resolve("opac-bluemap-test.txt"),report); } catch(Exception ignored) {}
  }
 }
 static class TestAPI extends BlueMapAPI {
  final Map<Object,BlueMapWorld> worlds=new HashMap<>(); final List<BlueMapMap> maps=new ArrayList<>();
  TestAPI() { for(String name:List.of("minecraft:overworld","minecraft:the_nether")) {
   var sets=new HashMap<String,MarkerSet>(); final BlueMapMap[] map={null};
   BlueMapWorld world=(BlueMapWorld)Proxy.newProxyInstance(getClass().getClassLoader(),new Class[]{BlueMapWorld.class},(p,m,a)->switch(m.getName()) {
    case "hashCode" -> System.identityHashCode(p); case "equals" -> p==a[0]; case "getId" -> name; case "getMaps" -> List.of(map[0]); case "getSaveFolder" -> Path.of("."); default -> null;
   });
   map[0]=(BlueMapMap)Proxy.newProxyInstance(getClass().getClassLoader(),new Class[]{BlueMapMap.class},(p,m,a)->switch(m.getName()) {
    case "hashCode" -> System.identityHashCode(p); case "equals" -> p==a[0]; case "getId","getName" -> name; case "getWorld" -> world; case "getMarkerSets" -> sets; default -> null;
   });
   worlds.put(ResourceKey.create(Registries.DIMENSION,Identifier.parse(name)),world); maps.add(map[0]);
  }}
  int count(Identifier dim) { return getWorld(ResourceKey.create(Registries.DIMENSION,dim)).orElseThrow().getMaps().iterator().next().getMarkerSets().values().stream().mapToInt(s->s.getMarkers().size()).sum(); }
  public RenderManager getRenderManager(){return null;} public WebApp getWebApp(){return null;} public de.bluecolored.bluemap.api.plugin.Plugin getPlugin(){return null;}
  public Collection<BlueMapMap> getMaps(){return maps;} public Collection<BlueMapWorld> getWorlds(){return worlds.values();}
  public Optional<BlueMapWorld> getWorld(Object id){return Optional.ofNullable(worlds.get(id));}
  public Optional<BlueMapMap> getMap(String id){return maps.stream().filter(m->m.getId().equals(id)).findFirst();}
  public String getBlueMapVersion(){return "test-5.29";}
 }
}
