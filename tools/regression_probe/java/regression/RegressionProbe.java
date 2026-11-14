package regression;
import java.nio.file.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.WorldDataConfiguration;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.neoforged.fml.common.Mod;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.client.event.ClientTickEvent;
@Mod(value="gregstead_regression_probe",dist=Dist.CLIENT)
public class RegressionProbe {
 boolean started=false;int ticks=0;boolean fixtures=false;String lastScreen="";boolean queued=false;java.nio.channels.FileChannel sessionChannel;java.nio.channels.FileLock sessionLock;
 public RegressionProbe(){try{sessionChannel=java.nio.channels.FileChannel.open(net.neoforged.fml.loading.FMLPaths.GAMEDIR.get().resolve("session.lock"),StandardOpenOption.CREATE,StandardOpenOption.WRITE);sessionLock=sessionChannel.tryLock();if(sessionLock==null)throw new IllegalStateException("Regression profile already running");}catch(Exception ex){throw new RuntimeException(ex);}System.out.println("REGRESSION_PROBE_ENABLED");NeoForge.EVENT_BUS.addListener(this::tick);NeoForge.EVENT_BUS.addListener(this::render);}
 void render(net.neoforged.neoforge.client.event.ScreenEvent.Render.Post event){var mc=Minecraft.getInstance();if(started||mc.level!=null)return;String name=event.getScreen().getClass().getName();
  if(!name.equals(lastScreen)){lastScreen=name;try{Files.writeString(mc.gameDirectory.toPath().resolve("probe-current-screen.txt"),name);}catch(Exception ex){throw new RuntimeException(ex);}}
  if(!queued&&name.endsWith("LoadingErrorScreen"))for(var child:event.getScreen().children())if(child instanceof net.minecraft.client.gui.components.Button button&&button.getMessage().getString().equals(net.neoforged.fml.i18n.FMLTranslations.parseMessage("fml.button.continue.launch"))){queued=true;mc.execute(()->{button.onPress(new net.minecraft.client.input.KeyEvent(257,0,0));queued=false;});return;}
  if(!queued&&(event.getScreen() instanceof TitleScreen||name.contains("AccessibilityOnboarding"))){queued=true;mc.execute(()->{queued=false;if(name.contains("AccessibilityOnboarding"))mc.gui.setScreen(new TitleScreen());else tick(null);});}
 }

 void tick(ClientTickEvent.Post e){var mc=Minecraft.getInstance();try{
  if(!started&&mc.gui.screen() instanceof TitleScreen){
   mc.options.pauseOnLostFocus = false;
   Path root=mc.gameDirectory.toPath().toRealPath();
   if(!root.endsWith(".test-client"))throw new IllegalStateException("Not the regression profile");
   Path save=root.resolve("saves/Gregstead-Regression"),marker=root.resolve("world-created-once");
   started=true;
   if(Files.exists(save.resolve("level.dat")))mc.createWorldOpenFlows().openWorld("Gregstead-Regression",()->{});
   else{
    if(Files.exists(marker)||Files.exists(save))throw new IllegalStateException("Refusing to recreate regression world");
    Files.writeString(marker,"Only world creation authorized; do not delete this marker.\n",StandardOpenOption.CREATE_NEW);
    mc.createWorldOpenFlows().createFreshLevel("Gregstead-Regression",new LevelSettings("Gregstead-Regression",GameType.CREATIVE,LevelSettings.DifficultySettings.DEFAULT,true,WorldDataConfiguration.DEFAULT),new WorldOptions(987654321L,true,false),WorldPresets::createNormalWorldDimensions,mc.gui.screen());
   }
  }
  Path commandFile=mc.gameDirectory.toPath().resolve("probe-command.txt");
  if(Files.exists(commandFile)&&mc.level!=null&&mc.player!=null&&mc.player.tickCount>120){
   String command=Files.readString(commandFile).strip();Files.delete(commandFile);
   String[] parts=command.split(" ",2);String arg=parts.length>1?parts[1]:"";
   switch(parts[0]){
    case "machine-client-prepare" -> {var server=mc.getSingleplayerServer();server.execute(()->MachineClientProbe.prepare(server));}
    case "machine-client-verify" -> MachineClientProbe.verify(mc,arg);
    case "shaders" -> MachineClientProbe.shaders(Boolean.parseBoolean(arg));
    case "machine-storage" -> {var server=mc.getSingleplayerServer();server.execute(()->MachineStorageProbe.run(server));}
    case "miner-active-start" -> {var server=mc.getSingleplayerServer();server.execute(()->MachineActiveMinerProbe.start(server));}
    case "miner-active-verify" -> {var server=mc.getSingleplayerServer();server.execute(()->MachineActiveMinerProbe.verify(server));}
    case "machine-resources" -> {var server=mc.getSingleplayerServer();server.execute(()->MachineConservationProbe.run(server));}
    case "machines" -> {var server=mc.getSingleplayerServer();server.execute(()->MachineStateProbe.run(server));}
    case "machines-persist" -> {var server=mc.getSingleplayerServer();server.execute(()->MachineStateProbe.verify(server));}
    case "drums-persist" -> {var server=mc.getSingleplayerServer();server.execute(()->DrumProbe.verifyPersistence(server));}
    case "drums" -> {var server=mc.getSingleplayerServer();server.execute(()->DrumProbe.run(server));}
    case "opac" -> {var server=mc.getSingleplayerServer();server.execute(()->{try{Class.forName("regression.OpacBlueMapProbe").getMethod("run",net.minecraft.server.MinecraftServer.class).invoke(null,server);}catch(Exception ex){throw new RuntimeException(ex);}});}
    case "status" -> Files.writeString(mc.gameDirectory.toPath().resolve("probe-render-status.txt"),"strictValidation="+com.mojang.renderpearl.frontend.FrontendGpuDevice.STRICT_VALIDATION+"\n");
    case "stop" -> mc.stop();
    case "capture" -> Screenshot.grab(mc.gameDirectory,arg+".png",mc.gameRenderer.mainRenderTarget(),1,c->{});
    case "close" -> {if(mc.gui.screen()!=null)mc.gui.screen().onClose();}
    case "server" -> {var server=mc.getSingleplayerServer();server.execute(()->server.getCommands().performPrefixedCommand(server.createCommandSourceStack(),arg));}
    case "sign" -> {String[] xyz=arg.split(" ");var pos=new net.minecraft.core.BlockPos(Integer.parseInt(xyz[0]),Integer.parseInt(xyz[1]),Integer.parseInt(xyz[2]));var sign=(net.minecraft.world.level.block.entity.SignBlockEntity)mc.level.getBlockEntity(pos);var server=mc.getSingleplayerServer();server.execute(()->((net.minecraft.world.level.block.entity.SignBlockEntity)server.overworld().getBlockEntity(pos)).setAllowedPlayerEditor(mc.player.getUUID()));mc.gui.setScreen(sign instanceof net.minecraft.world.level.block.entity.HangingSignBlockEntity ? new net.minecraft.client.gui.screens.inventory.HangingSignEditScreen(sign,net.minecraft.world.level.block.entity.SignTextSlot.FRONT,false) : new net.minecraft.client.gui.screens.inventory.SignEditScreen(sign,net.minecraft.world.level.block.entity.SignTextSlot.FRONT,false));}
    case "text" -> {for(int cp:arg.codePoints().toArray())mc.gui.screen().charTyped(new net.minecraft.client.input.CharacterEvent(cp));}
    case "use" -> {String[] xyz=arg.split(" ");var pos=new net.minecraft.core.BlockPos(Integer.parseInt(xyz[0]),Integer.parseInt(xyz[1]),Integer.parseInt(xyz[2]));mc.gameMode.useItemOn(mc.player,net.minecraft.world.InteractionHand.MAIN_HAND,new net.minecraft.world.phys.BlockHitResult(net.minecraft.world.phys.Vec3.atCenterOf(pos),net.minecraft.core.Direction.SOUTH,pos,false));}
    case "emi" -> {dev.emi.emi.screen.EmiScreenManager.search.setValue(arg.equals("on")?"gregsteadnomatchxyz":"");dev.emi.emi.screen.EmiScreenManager.search.highlight=arg.equals("on");}
    default -> throw new IllegalArgumentException(command);
   }
   Files.writeString(mc.gameDirectory.toPath().resolve("probe-result.txt"),command+"\n"+(mc.gui.screen()==null?"world":mc.gui.screen().getClass().getName()));
  }
  if(mc.level!=null&&mc.player!=null&&!fixtures&&mc.getSingleplayerServer()!=null){
   fixtures=true;var server=mc.getSingleplayerServer();Path marker=mc.gameDirectory.toPath().resolve("regression-fixtures-v1");
   if(!Files.exists(marker))server.execute(()->{try{
    for(int x=-1;x<=1;x++)for(int z=-1;z<=0;z++)server.overworld().getChunk(x,z);
    var commands=server.getCommands();var source=server.createCommandSourceStack();
    commands.performPrefixedCommand(source,"fill -4 159 -5 20 159 12 minecraft:stone");
    commands.performPrefixedCommand(source,"setblock 0 160 0 gtceu:rubber_sign[rotation=0]");
    commands.performPrefixedCommand(source,"setblock 3 160 0 minecraft:oak_sign[rotation=0]");
    commands.performPrefixedCommand(source,"setblock 6 160 0 gtceu:treated_wood_sign[rotation=0]");
    commands.performPrefixedCommand(source,"setblock 9 160 -1 minecraft:stone");
    commands.performPrefixedCommand(source,"setblock 9 160 0 gtceu:rubber_wall_sign[facing=south]");
    commands.performPrefixedCommand(source,"setblock 12 161 0 minecraft:stone");
    commands.performPrefixedCommand(source,"setblock 12 160 0 gtceu:rubber_hanging_sign[rotation=0,attached=false]");
    commands.performPrefixedCommand(source,"time set day");
    commands.performPrefixedCommand(source,"setworldspawn 5 160 7");
    commands.performPrefixedCommand(source,"tp GregsteadTest 5.5 160 7.5 180 15");
    Files.writeString(marker,"Persistent fixtures: rubber/oak/treated signs, rubber wall/hanging sign; keep across releases.\n");
   }catch(Exception ex){throw new RuntimeException(ex);}});
  }
  if(mc.level!=null&&mc.player!=null&&Files.exists(mc.gameDirectory.toPath().resolve("regression-fixtures-v1"))&&++ticks==100){
   Screenshot.grab(mc.gameDirectory,"regression-world.png",mc.gameRenderer.mainRenderTarget(),1,c->{});
   Files.writeString(mc.gameDirectory.toPath().resolve("probe-world-loaded.txt"),mc.level.dimension().identifier().toString());
  }
 }catch(Exception ex){throw new RuntimeException(ex);}}
}
