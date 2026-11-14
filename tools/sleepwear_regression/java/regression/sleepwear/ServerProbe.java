package regression.sleepwear;

import io.github.mmyllynen.sleepwear.*;
import java.nio.file.*;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.block.*;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

@Mod("sleepwear_regression")
public class ServerProbe {
  static final Path ROOT = Path.of("/home/myllymik/Projects/gregstead/.test-client");
  static final BlockPos BED = new BlockPos(16, 160, 8);
  static StringBuilder report;
  static int step = -1, ticks;
  static ServerPlayer player;
  static ItemStack oldShirt, oldPants;
  static long scans, checks;
  static boolean failed;

  public ServerProbe() {
    NeoForge.EVENT_BUS.addListener(this::tick);
  }

  static void check(boolean ok, String msg) {
    if (!ok) throw new AssertionError(msg);
    report.append("PASS ").append(msg).append('\n');
  }

  static void command(MinecraftServer s, String cmd) {
    s.getCommands().performPrefixedCommand(s.createCommandSourceStack(), cmd);
  }

  static CraftingInput grid(ItemStack... stacks) {
    return CraftingInput.of(3, 3, List.of(stacks));
  }

  static ItemStack E() {
    return ItemStack.EMPTY;
  }

  @SuppressWarnings("unchecked")
  static void recipes(MinecraftServer server) {
    var level = player.level();
    var manager = server.getRecipeManager();
    ColourRecipe colour = new ColourRecipe();
    int count = 0;
    for (DyeColor color : DyeColor.values())
      for (boolean shirt : new boolean[] {true, false}) {
        Item wool =
            BuiltInRegistries.ITEM.getValue(
                net.minecraft.resources.Identifier.withDefaultNamespace(color.getName() + "_wool"));
        List<ItemStack> cells = new ArrayList<>();
        for (int i = 0; i < 9; i++) cells.add(new ItemStack(wool));
        if (shirt) cells.set(1, E());
        else {
          cells.set(4, E());
          cells.set(7, E());
        }
        var in = CraftingInput.of(3, 3, cells);
        var recipe = manager.getRecipeFor(RecipeType.CRAFTING, in, level);
        check(recipe.isPresent(), "wool recipe " + color + " " + shirt);
        ItemStack out = recipe.orElseThrow().value().assemble(in);
        check(out.is(shirt ? Sleepwear.SHIRT.get() : Sleepwear.PANTS.get()), "correct garment");
        check(
            out.get(Sleepwear.DESIGN.get())
                .equals("solid_" + color.getName() + "_" + color.getName()),
            "wool colour preserved");
        cells.set(
            0,
            new ItemStack(
                BuiltInRegistries.ITEM.getValue(
                    net.minecraft.resources.Identifier.withDefaultNamespace(
                        color == DyeColor.WHITE ? "black_wool" : "white_wool"))));
        check(
            manager
                .getRecipeFor(RecipeType.CRAFTING, CraftingInput.of(3, 3, cells), level)
                .isEmpty(),
            "mixed wool rejected");
        count++;
      }
    for (Item garment : List.of(Sleepwear.SHIRT.get(), Sleepwear.PANTS.get()))
      for (DyeColor a : DyeColor.values())
        for (DyeColor b : DyeColor.values()) {
          ItemStack s = new ItemStack(garment);
          s.set(DataComponents.CUSTOM_NAME, Component.literal("Keepsake"));
          ItemStack
              da =
                  new ItemStack(
                      BuiltInRegistries.ITEM.getValue(
                          net.minecraft.resources.Identifier.withDefaultNamespace(
                              a.getName() + "_dye"))),
              db =
                  new ItemStack(
                      BuiltInRegistries.ITEM.getValue(
                          net.minecraft.resources.Identifier.withDefaultNamespace(
                              b.getName() + "_dye")));
          var vertical = grid(E(), da, E(), E(), s, E(), E(), db, E());
          ItemStack out = colour.assemble(vertical);
          check(
              out.get(Sleepwear.DESIGN.get()).equals("split_" + a.getName() + "_" + b.getName()),
              "split dye");
          check(
              out.get(DataComponents.CUSTOM_NAME).getString().equals("Keepsake"),
              "custom components retained");
          out = colour.assemble(grid(E(), E(), E(), da, s, db, E(), E(), E()));
          check(
              out.get(Sleepwear.DESIGN.get()).equals("stripes_" + a.getName() + "_" + b.getName()),
              "stripe dye");
          out = colour.assemble(grid(out, E(), da, E(), E(), E(), E(), E(), E()));
          check(
              out.get(Sleepwear.DESIGN.get()).equals("solid_" + a.getName() + "_" + a.getName()),
              "single dye replaces pattern");
        }
    ItemStack s = new ItemStack(Sleepwear.SHIRT.get()),
        d =
            new ItemStack(
                BuiltInRegistries.ITEM.getValue(
                    net.minecraft.resources.Identifier.withDefaultNamespace("red_dye")));
    check(
        colour.assemble(grid(d, E(), E(), E(), s, E(), E(), E(), d)).isEmpty(),
        "diagonal rejected");
    check(
        colour.assemble(grid(E(), d, E(), d, s, d, E(), E(), E())).isEmpty(), "extra dye rejected");
    check(s.getAttributeModifiers().modifiers().isEmpty(), "no armour attributes");
    report.append("Wool variants: " + count + "; dye pairs: 512\n");
  }

  void tick(ServerTickEvent.Post event) {
    try {
      var server = event.getServer();
      Path cmd = ROOT.resolve("sleepwear-server-command.txt");
      if (Files.exists(cmd)) {
        String c = Files.readString(cmd).strip();
        Files.delete(cmd);
        if (c.equals("test")) {
          player =
              server.getPlayerList().getPlayers().stream()
                  .filter(p -> p.getGameProfile().name().equals("GregsteadTest"))
                  .findFirst()
                  .orElseThrow();
          report = new StringBuilder();
          failed = false;
          oldShirt = player.getItemBySlot(EquipmentSlot.CHEST).copy();
          oldPants = player.getItemBySlot(EquipmentSlot.LEGS).copy();
          recipes(server);
          step = 0;
          ticks = 0;
        } else if (c.equals("shutdown")) server.halt(false);
        else if (c.startsWith("command ")) command(server, c.substring(8));
        else if (c.equals("verifyReconnect")) {
          player = server.getPlayerList().getPlayers().getFirst();
          check(
              player.getPersistentData().getBooleanOr("sleepwear:pause", false),
              "wake pause survives reconnect");
          check(!player.isSleeping(), "reconnect does not resume paused automation");
          Files.writeString(
              ROOT.resolve("sleepwear-reconnect.txt"),
              "PASS pause retained after disconnect/reconnect\n");
        } else if (c.equals("prepareReconnect")) {
          player = server.getPlayerList().getPlayers().getFirst();
          player.setItemSlot(
              EquipmentSlot.CHEST,
              Sleepwear.colour(
                  new ItemStack(Sleepwear.SHIRT.get()),
                  "split",
                  DyeColor.LIGHT_BLUE,
                  DyeColor.BLUE));
          player.setItemSlot(
              EquipmentSlot.LEGS,
              Sleepwear.colour(
                  new ItemStack(Sleepwear.PANTS.get()), "stripes", DyeColor.BLUE, DyeColor.WHITE));
          command(server, "time set night");
        } else if (c.equals("wake")) {
          player = server.getPlayerList().getPlayers().getFirst();
          player.stopSleepInBed(true, true);
        } else if (c.equals("display")) {
          player = server.getPlayerList().getPlayers().getFirst();
          player.setItemSlot(
              EquipmentSlot.CHEST,
              Sleepwear.colour(
                  new ItemStack(Sleepwear.SHIRT.get()),
                  "split",
                  DyeColor.LIGHT_BLUE,
                  DyeColor.BLUE));
          player.setItemSlot(
              EquipmentSlot.LEGS,
              Sleepwear.colour(
                  new ItemStack(Sleepwear.PANTS.get()), "stripes", DyeColor.BLUE, DyeColor.WHITE));
          command(server, "tp GregsteadTest 17 160 11 180 0");
          command(server, "time set day");
        }
      }
      if (step < 0 || ++ticks % 20 != 0) return;
      switch (step++) {
        case 0 -> {
          command(server, "gamemode creative GregsteadTest");
          command(server, "time set day");
          command(server, "setblock 16 161 8 minecraft:air");
          command(server, "fill 15 160 10 17 162 10 minecraft:air");
          command(server, "fill 18 161 8 18 161 9 minecraft:stone");
          command(server, "weather clear");
          command(server, "fill 14 159 6 19 159 12 minecraft:stone");
          command(
              server, "setblock 16 160 8 minecraft:red_bed[part=head,facing=north,occupied=false]");
          command(
              server, "setblock 16 160 9 minecraft:red_bed[part=foot,facing=north,occupied=false]");
          command(server, "tp GregsteadTest 16.5 160 11 180 0");
          player.setItemSlot(EquipmentSlot.CHEST, new ItemStack(Sleepwear.SHIRT.get()));
          player.setItemSlot(EquipmentSlot.LEGS, new ItemStack(Sleepwear.PANTS.get()));
        }
        case 1 -> {
          scans = SleepScheduler.bedScans;
          checks = SleepScheduler.equipmentChecks;
        }
        case 2 -> {
          check(SleepScheduler.bedScans == scans, "no daytime bed scans");
          check(SleepScheduler.equipmentChecks == checks, "no periodic equipment checks");
          check(!player.isSleeping(), "no daytime sleep");
          player.setItemSlot(EquipmentSlot.LEGS, E());
          command(server, "time set night");
        }
        case 3 -> {
          check(!player.isSleeping(), "one garment does not sleep");
          player.setItemSlot(EquipmentSlot.LEGS, new ItemStack(Sleepwear.PANTS.get()));
        }
        case 4 -> {
          check(player.isSleeping(), "both garments automatically sleep at night");
          player.stopSleepInBed(true, true);
        }
        case 5 -> {
          check(!player.isSleeping(), "manual wake pauses current night");
          player.setItemSlot(EquipmentSlot.LEGS, E());
        }
        case 6 -> player.setItemSlot(EquipmentSlot.LEGS, new ItemStack(Sleepwear.PANTS.get()));
        case 7 -> {
          check(player.isSleeping(), "re-equipping resumes automation");
          command(server, "time set day");
          player.stopSleepInBed(true, true);
        }
        case 8 -> command(server, "time set night");
        case 9 -> {
          check(player.isSleeping(), "next night automatically sleeps");
          command(server, "time set day");
          player.stopSleepInBed(true, true);
          command(server, "setblock 16 161 8 minecraft:stone");
        }
        case 10 -> command(server, "time set night");
        case 11 -> {
          check(!player.isSleeping(), "obstructed bed rejected");
          command(server, "setblock 16 161 8 minecraft:air");
          command(server, "time set day");
        }
        case 12 -> {
          command(server, "tp GregsteadTest 22.5 160 11 180 0");
          command(server, "time set night");
        }
        case 13 -> {
          check(!player.isSleeping(), "out-of-reach bed rejected");
          command(server, "time set day");
          command(server, "tp GregsteadTest 16.5 160 11 180 0");
          command(server, "fill 15 160 10 17 162 10 minecraft:stone");
        }
        case 14 -> command(server, "time set night");
        case 15 -> {
          check(!player.isSleeping(), "wall blocks automatic use");
          command(server, "fill 15 160 10 17 162 10 minecraft:air");
          command(server, "time set day");
        }
        case 16 -> {
          command(server, "time pause");
          scans = SleepScheduler.bedScans;
        }
        case 17 -> {
          check(SleepScheduler.bedScans == scans, "paused daytime clock performs no bed searches");
          command(server, "time set night");
        }
        case 18 -> {
          check(player.isSleeping(), "time command activates sleep even with paused clock");
          command(server, "time set day");
          player.stopSleepInBed(true, true);
          command(server, "time resume");
          command(server, "gamemode survival GregsteadTest");
          command(server, "difficulty normal");
          command(
              server,
              "summon minecraft:zombie 16 160 7"
                  + " {NoAI:1b,PersistenceRequired:1b,Tags:[\"sleepwear_test\"]}");
        }
        case 19 -> command(server, "time set night");
        case 20 -> {
          check(!player.isSleeping(), "hostile monster blocks normal sleep");
          command(server, "kill @e[tag=sleepwear_test]");
          command(server, "gamemode creative GregsteadTest");
          command(server, "time set day");
          command(
              server, "setblock 16 160 8 minecraft:red_bed[part=head,facing=north,occupied=true]");
          command(
              server, "setblock 16 160 9 minecraft:red_bed[part=foot,facing=north,occupied=true]");
        }
        case 21 -> command(server, "time set night");
        case 22 -> {
          check(!player.isSleeping(), "occupied bed ignored");
          command(
              server, "setblock 16 160 8 minecraft:red_bed[part=head,facing=north,occupied=false]");
          command(
              server, "setblock 16 160 9 minecraft:red_bed[part=foot,facing=north,occupied=false]");
          command(server, "time set day");
        }
        case 23 -> {
          command(server, "fill 18 161 8 18 161 9 minecraft:air");
          command(
              server,
              "setblock 18 160 8 minecraft:blue_bed[part=head,facing=north,occupied=false]");
          command(
              server,
              "setblock 18 160 9 minecraft:blue_bed[part=foot,facing=north,occupied=false]");
          command(server, "tp GregsteadTest 17 160 11 180 0");
          command(server, "time set night");
        }
        case 24 -> {
          check(
              player.isSleeping() && player.getSleepingPos().orElseThrow().equals(BED),
              "nearest valid bed selected");
          command(server, "time set day");
          player.stopSleepInBed(true, true);
        }
        case 25 -> {
          command(server, "weather thunder");
          player.level().setRainLevel(1);
          player.level().setThunderLevel(1);
        }
        case 26 -> {
          check(player.isSleeping(), "daytime thunderstorm activates normal sleep");
          command(server, "weather clear");
          player.level().setRainLevel(0);
          player.level().setThunderLevel(0);
          player.stopSleepInBed(true, true);
        }
        case 27, 28, 29, 30 -> {}
        case 31 -> {
          player.setItemSlot(EquipmentSlot.CHEST, oldShirt);
          player.setItemSlot(EquipmentSlot.LEGS, oldPants);
          report.append("PASS suite completed\n");
          Files.writeString(
              ROOT.resolve(
                  "sleepwear-tests-"
                      + (server.isDedicatedServer() ? "dedicated" : "integrated")
                      + ".txt"),
              report);
          step = -1;
        }
      }
    } catch (Throwable ex) {
      step = -1;
      failed = true;

      try {
        Files.writeString(
            ROOT.resolve("sleepwear-tests-error.txt"),
            (report == null ? "" : report.toString())
                + "FAIL "
                + ex
                + "\n"
                + Arrays.toString(ex.getStackTrace()));
      } catch (Exception ignored) {
      }
      ex.printStackTrace();
    }
  }
}
