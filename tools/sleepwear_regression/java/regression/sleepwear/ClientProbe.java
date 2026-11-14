package regression.sleepwear;

import java.nio.file.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.*;
import net.minecraft.client.multiplayer.*;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.common.NeoForge;

@Mod(value = "sleepwear_regression", dist = Dist.CLIENT)
public class ClientProbe {
  java.nio.channels.FileChannel sessionChannel;
  java.nio.channels.FileLock sessionLock;

  public ClientProbe() {
    try {
      var root = net.neoforged.fml.loading.FMLPaths.GAMEDIR.get().toRealPath();
      if (!root.endsWith(".test-client"))
        throw new IllegalStateException("Not the regression profile");
      sessionChannel =
          java.nio.channels.FileChannel.open(
              root.resolve("session.lock"), StandardOpenOption.CREATE, StandardOpenOption.WRITE);
      sessionLock = sessionChannel.tryLock();
      if (sessionLock == null) throw new IllegalStateException("Regression client already running");
    } catch (Exception ex) {
      throw new RuntimeException(ex);
    }
    NeoForge.EVENT_BUS.addListener(this::tick);
    NeoForge.EVENT_BUS.addListener(this::render);
  }

  void render(net.neoforged.neoforge.client.event.ScreenEvent.Render.Post event) {
    if (event.getScreen().getClass().getName().contains("AccessibilityOnboarding")) {
      Minecraft.getInstance()
          .execute(() -> Minecraft.getInstance().gui.setScreen(new TitleScreen()));
      return;
    }
    if (event.getScreen() instanceof TitleScreen) tick(null);
    if (!event.getScreen().getClass().getName().endsWith("LoadingErrorScreen")) return;
    for (var child : event.getScreen().children())
      if (child instanceof net.minecraft.client.gui.components.Button button
          && button
              .getMessage()
              .getString()
              .equals(
                  net.neoforged.fml.i18n.FMLTranslations.parseMessage(
                      "fml.button.continue.launch"))) {
        Minecraft.getInstance()
            .execute(() -> button.onPress(new net.minecraft.client.input.KeyEvent(257, 0, 0)));
        break;
      }
  }

  void tick(ClientTickEvent.Post e) {
    var mc = Minecraft.getInstance();
    try {
      var root = mc.gameDirectory.toPath();
      mc.options.pauseOnLostFocus = false;
      Files.writeString(
          root.resolve("sleepwear-screen.txt"),
          mc.gui.screen() == null ? "world" : mc.gui.screen().getClass().getName());
      var cmd = root.resolve("sleepwear-command.txt");
      if (!Files.exists(cmd)) return;
      String c = Files.readString(cmd).strip();
      if (c.equals("connect")
          && (mc.gui.screen() instanceof TitleScreen
              || mc.gui.screen() instanceof DisconnectedScreen)) {
        Files.delete(cmd);
        ConnectScreen.startConnecting(
            mc.gui.screen(),
            mc,
            ServerAddress.parseString("127.0.0.1:25575"),
            new ServerData("UI regression", "127.0.0.1:25575", ServerData.Type.OTHER),
            false,
            null);
      } else if (c.equals("open") && mc.gui.screen() instanceof TitleScreen) {
        Files.delete(cmd);
        if (!Files.exists(root.resolve("saves/Gregstead-Regression/level.dat")))
          throw new IllegalStateException("Missing persistent world");
        mc.createWorldOpenFlows().openWorld("Gregstead-Regression", () -> {});
      } else if (c.startsWith("click ") && mc.player != null) {
        Files.delete(cmd);
        String[] a = c.split(" ");
        mc.gameMode.handleContainerInput(
            mc.player.containerMenu.containerId,
            Integer.parseInt(a[1]),
            0,
            net.minecraft.world.inventory.ContainerInput.valueOf(a[2]),
            mc.player);
        Files.writeString(root.resolve("sleepwear-result.txt"), c);
      } else if (c.equals("reload")) {
        Files.delete(cmd);
        mc.reloadResourcePacks()
            .thenRun(() -> {
              try {
                Files.writeString(root.resolve("sleepwear-reload-complete.txt"), "done");
              } catch (Throwable ex) {
                try {
                  Files.writeString(
                      mc.gameDirectory.toPath().resolve("sleepwear-result.txt"), "ERROR: " + ex);
                } catch (Exception io) {
                  throw new RuntimeException(io);
                }
              }
            });
      } else if (c.startsWith("capture ")) {
        Files.delete(cmd);
        net.minecraft.client.Screenshot.grab(
            mc.gameDirectory,
            c.substring(8) + ".png",
            mc.gameRenderer.mainRenderTarget(),
            1,
            message -> {});
        Files.writeString(root.resolve("sleepwear-result.txt"), c);
      } else if (c.equals("disconnect") && mc.player != null) {
        Files.delete(cmd);
        mc.disconnectFromWorld(net.minecraft.client.multiplayer.ClientLevel.DEFAULT_QUIT_MESSAGE);
      } else if (c.equals("verifyDesign") && mc.player != null) {
        Files.delete(cmd);
        var shirt = mc.player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.CHEST);
        var pants = mc.player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.LEGS);
        if (!"split_light_blue_blue"
                .equals(shirt.get(io.github.mmyllynen.sleepwear.Sleepwear.DESIGN.get()))
            || !"stripes_blue_white"
                .equals(pants.get(io.github.mmyllynen.sleepwear.Sleepwear.DESIGN.get())))
          throw new AssertionError("Wrong synced design");
        Files.writeString(
            root.resolve("sleepwear-design-sync.txt"),
            "PASS item design synchronized and persisted\n");
      } else if (c.equals("thirdperson") && mc.player != null) {
        Files.delete(cmd);
        mc.options.setCameraType(net.minecraft.client.CameraType.THIRD_PERSON_FRONT);
      } else if (c.equals("stop")) {
        Files.delete(cmd);
        mc.stop();
      } else if (c.equals("close") && mc.player != null) {
        Files.delete(cmd);
        mc.player.closeContainer();
      } else if (c.equals("dump") && mc.player != null) {
        Files.delete(cmd);
        StringBuilder out = new StringBuilder();
        out.append(mc.player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.CHEST));
        Files.writeString(root.resolve("sleepwear-result.txt"), out);
      }
    } catch (Throwable ex) {
      try {
        Files.writeString(
            mc.gameDirectory.toPath().resolve("sleepwear-result.txt"), "ERROR: " + ex);
      } catch (Exception io) {
        throw new RuntimeException(io);
      }
    }
  }
}
