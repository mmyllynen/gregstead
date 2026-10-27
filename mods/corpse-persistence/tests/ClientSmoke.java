package corpseprobe;
import de.maxhenkel.corpse.entities.CorpseEntity;
import net.minecraft.client.*;
import net.neoforged.fml.common.Mod;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import java.nio.file.*;
@Mod(value="corpse_client_smoke",dist=Dist.CLIENT) public class ClientSmoke {
 int ticks;boolean done;
 public ClientSmoke(){NeoForge.EVENT_BUS.addListener((ClientTickEvent.Post e)->{if(done)return;var m=Minecraft.getInstance();if(m.player==null||m.level==null)return;if(++ticks<180)return;try{int count=0;for(var entity:m.level.entitiesForRendering())if(entity instanceof CorpseEntity c&&c.isSkeleton())count++;if(count<2)throw new IllegalStateException("Expected two saved skeletons, found "+count);if(net.neoforged.fml.ModList.get().isLoaded("neogreg_corpse_persistence"))throw new IllegalStateException("Client unexpectedly has the server addon");Screenshot.grab(Path.of(".").toFile(),"persistent-skeletons.png",m.gameRenderer.mainRenderTarget(),1,c->{});Files.writeString(Path.of("client-proof.json"),"{\"passed\":true,\"skeletons\":"+count+",\"clientAddonRequired\":false}");done=true;m.stop();}catch(Throwable t){try{Files.writeString(Path.of("client-failure.txt"),t.toString());}catch(Exception ignored){}done=true;m.stop();}});}
}
