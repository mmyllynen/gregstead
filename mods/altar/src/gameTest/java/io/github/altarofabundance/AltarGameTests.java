package io.github.altarofabundance;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.FunctionGameTestInstance;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

@Mod(AltarOfAbundance.MOD_ID)
public final class AltarGameTests {
    private static final BlockPos OFFERING = new BlockPos(8, 2, 8);
    private static final BlockPos MATERIAL = new BlockPos(6, 2, 8);
    private static final BlockPos OUTPUT = new BlockPos(10, 2, 8);
    private static final Map<String, Consumer<GameTestHelper>> TESTS = new LinkedHashMap<>();

    static {
        TESTS.put("one_to_sixty_four", AltarGameTests::oneToSixtyFour);
        TESTS.put("material_allowlist", AltarGameTests::materialAllowlist);
        TESTS.put("rejected_offering_and_occupied_slot", AltarGameTests::rejectedOffering);
        TESTS.put("missing_pedestal", h -> invalidStructure(h, "missing"));
        TESTS.put("ambiguous_pedestals", h -> invalidStructure(h, "ambiguous"));
        TESTS.put("shared_pedestal", AltarGameTests::sharedPedestal);
        TESTS.put("radius_three_inclusive", h -> distance(h, new BlockPos(5, 2, 8), true));
        TESTS.put("radius_three_dimensional", h -> distance(h, new BlockPos(5, 3, 8), false));
        TESTS.put("blocked_output", h -> invalidStructure(h, "blocked"));
        TESTS.put("removal_cancels_without_consuming", AltarGameTests::removalCancels);
        TESTS.put("reinsert_restarts_ritual", AltarGameTests::reinsertRestarts);
        TESTS.put("breaking_drops_contents", AltarGameTests::breakingDropsContents);
        TESTS.put("save_reload_restarts_ritual", AltarGameTests::saveReload);
        TESTS.put("two_players_cannot_duplicate", AltarGameTests::twoPlayers);
        TESTS.put("preserves_item_components", AltarGameTests::preservesComponents);
        TESTS.put("output_and_structure_rechecked", AltarGameTests::recheckAtCompletion);
        TESTS.put("three_survival_recipes", AltarGameTests::recipes);
        TESTS.put("chunk_unload_callback_resets", AltarGameTests::chunkUnload);
        TESTS.put("output_can_be_picked_up", AltarGameTests::pickup);
    }

    public AltarGameTests(IEventBus bus) {
        DeferredRegister<Consumer<GameTestHelper>> functions = DeferredRegister.create(Registries.TEST_FUNCTION, AltarOfAbundance.MOD_ID);
        TESTS.forEach((name, function) -> functions.register(name, () -> function));
        functions.register(bus);
        bus.addListener(AltarGameTests::registerTests);
    }

    private static void registerTests(RegisterGameTestsEvent event) {
        Holder<TestEnvironmentDefinition<?>> environment = event.registerEnvironment(AltarOfAbundance.id("environment"));
        TESTS.forEach((name, function) -> event.registerTest(AltarOfAbundance.id(name),
            data -> new FunctionGameTestInstance(ResourceKey.create(Registries.TEST_FUNCTION, AltarOfAbundance.id(name)), data),
            new TestData<>(environment, AltarOfAbundance.id("empty"), 260, 0, true)));
    }

    private static PedestalBlockEntity at(GameTestHelper h, BlockPos pos) {
        return (PedestalBlockEntity) h.getLevel().getBlockEntity(h.absolutePos(pos));
    }

    private static void setup(GameTestHelper h) {
        h.setBlock(OFFERING, AltarOfAbundance.OFFERING.get());
        h.setBlock(MATERIAL, AltarOfAbundance.MATERIAL.get());
        h.setBlock(OUTPUT, AltarOfAbundance.OUTPUT.get());
    }

    private static void fill(GameTestHelper h) {
        at(h, MATERIAL).insert(new ItemStack(Items.COBBLESTONE), false);
        at(h, OFFERING).insert(new ItemStack(Items.DIAMOND), false);
    }

    private static void check(GameTestHelper h, boolean condition, String message) {
        h.assertTrue(condition, message);
    }

    private static List<ItemEntity> drops(GameTestHelper h) {
        return h.getLevel().getEntitiesOfClass(ItemEntity.class,
            new AABB(Vec3.atLowerCornerOf(h.absolutePos(new BlockPos(0, 0, 0))), Vec3.atLowerCornerOf(h.absolutePos(new BlockPos(16, 8, 16)))));
    }

    private static int count(GameTestHelper h, net.minecraft.world.item.Item item) {
        return drops(h).stream().filter(e -> e.getItem().is(item)).mapToInt(e -> e.getItem().getCount()).sum();
    }

    private static void unchanged(GameTestHelper h) {
        check(h, at(h, MATERIAL).displayedItem().is(Items.COBBLESTONE), "Material must survive a failed ritual");
        check(h, at(h, OFFERING).displayedItem().is(Items.DIAMOND), "Diamond must survive a failed ritual");
        check(h, drops(h).isEmpty(), "Failed ritual must not produce output");
    }

    private static void oneToSixtyFour(GameTestHelper h) {
        setup(h);
        ItemStack material = new ItemStack(Items.COBBLESTONE, 5);
        ItemStack diamonds = new ItemStack(Items.DIAMOND, 3);
        at(h, MATERIAL).insert(material, false);
        at(h, OFFERING).insert(diamonds, false);
        check(h, material.getCount() == 4 && diamonds.getCount() == 2, "Insert must move one item only");
        h.runAtTickTime(40, () -> unchanged(h));
        h.runAtTickTime(100, () -> {
            check(h, count(h, Items.COBBLESTONE) == 64, "One material must produce exactly 64 blocks");
            check(h, at(h, MATERIAL).displayedItem().isEmpty() && at(h, OFFERING).displayedItem().isEmpty(), "Ingredients must be consumed");
            Vec3 expected = Vec3.atCenterOf(h.absolutePos(OUTPUT));
            check(h, drops(h).size() == 1 && drops(h).getFirst().position().distanceToSqr(expected) < 3, "Output must appear over output pedestal");
        });
        h.runAtTickTime(180, () -> {
            check(h, count(h, Items.COBBLESTONE) == 64, "Empty altar must not run again");
            h.succeed();
        });
    }

    private static void materialAllowlist(GameTestHelper h) {
        List<ItemStack> allowed = BuiltInRegistries.ITEM.stream().map(ItemStack::new).filter(s -> s.is(AltarOfAbundance.MATERIALS)).toList();
        check(h, allowed.size() == 82, "All 82 default material tag entries must resolve");
        for (ItemStack stack : allowed) {
            check(h, PedestalBlockEntity.validMaterial(stack), "Tag entry must be a 64-stack block: " + stack);
            String name = BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath();
            check(h, !name.contains("ore") && !name.startsWith("stripped_") && !name.endsWith("_planks"), "Processed blocks and ores must not enter allowlist");
        }
        for (var item : List.of(Items.DIAMOND_ORE, Items.DEEPSLATE_IRON_ORE, Items.ANCIENT_DEBRIS, Items.DIAMOND_BLOCK,
                Items.IRON_BLOCK, Items.NETHERITE_BLOCK, Items.FURNACE, Items.CHEST, Items.OAK_PLANKS, Items.STONE_BRICKS,
                Items.OAK_STAIRS, Items.STONE_SLAB, Items.GLASS, Items.STRIPPED_OAK_LOG, Items.DIAMOND)) {
            check(h, !PedestalBlockEntity.validMaterial(new ItemStack(item)), "Disallowed item: " + item);
        }
        setup(h);
        check(h, at(h, MATERIAL).insert(new ItemStack(Items.DIAMOND_ORE), false).equals("invalid"), "Ore must be rejected by interaction");
        check(h, at(h, OUTPUT).insert(new ItemStack(Items.COBBLESTONE), false).equals("output"), "Output pedestal must reject insertion");
        h.succeed();
    }

    private static void rejectedOffering(GameTestHelper h) {
        setup(h);
        ItemStack emerald = new ItemStack(Items.EMERALD);
        check(h, at(h, OFFERING).insert(emerald, false).equals("invalid") && emerald.getCount() == 1, "Only diamond may fuel default altar");
        ItemStack diamonds = new ItemStack(Items.DIAMOND, 2);
        at(h, OFFERING).insert(diamonds, false);
        check(h, at(h, OFFERING).insert(diamonds, false).equals("occupied") && diamonds.getCount() == 1, "Occupied insertion must not consume item");
        h.succeed();
    }

    private static void invalidStructure(GameTestHelper h, String kind) {
        setup(h);
        fill(h);
        switch (kind) {
            case "missing" -> h.setBlock(OUTPUT, Blocks.AIR);
            case "ambiguous" -> h.setBlock(new BlockPos(8, 2, 10), AltarOfAbundance.OUTPUT.get());
            case "blocked" -> h.setBlock(OUTPUT.above(), Blocks.STONE);
        }
        check(h, at(h, OFFERING).status().equals(kind), "Status should explain invalid structure");
        h.runAtTickTime(100, () -> { unchanged(h); h.succeed(); });
    }

    private static void sharedPedestal(GameTestHelper h) {
        setup(h);
        fill(h);
        h.setBlock(new BlockPos(6, 2, 6), AltarOfAbundance.OFFERING.get());
        at(h, new BlockPos(6, 2, 6)).insert(new ItemStack(Items.DIAMOND), false);
        check(h, at(h, OFFERING).status().equals("ambiguous"), "Shared material pedestal must invalidate altar");
        h.runAtTickTime(100, () -> { unchanged(h); h.succeed(); });
    }

    private static void distance(GameTestHelper h, BlockPos materialPos, boolean valid) {
        setup(h);
        h.setBlock(MATERIAL, Blocks.AIR);
        h.setBlock(materialPos, AltarOfAbundance.MATERIAL.get());
        at(h, materialPos).insert(new ItemStack(Items.COBBLESTONE), false);
        at(h, OFFERING).insert(new ItemStack(Items.DIAMOND), false);
        h.runAtTickTime(100, () -> {
            check(h, count(h, Items.COBBLESTONE) == (valid ? 64 : 0), "Radius must use Euclidean distance including height");
            check(h, at(h, materialPos).displayedItem().isEmpty() == valid, "Material consumed only within radius");
            h.succeed();
        });
    }

    private static void removalCancels(GameTestHelper h) {
        setup(h); fill(h);
        h.runAtTickTime(30, () -> {
            check(h, at(h, OFFERING).progress() > 0, "Ritual should have started");
            check(h, at(h, MATERIAL).take().is(Items.COBBLESTONE), "Material must be retrievable during ritual");
            check(h, at(h, OFFERING).progress() == 0, "Taking material must cancel immediately");
        });
        h.runAtTickTime(100, () -> {
            check(h, drops(h).isEmpty() && at(h, OFFERING).displayedItem().is(Items.DIAMOND), "Cancelled ritual must not consume diamond or create output");
            h.succeed();
        });
    }

    private static void reinsertRestarts(GameTestHelper h) {
        setup(h); fill(h);
        h.runAtTickTime(40, () -> {
            ItemStack stack = at(h, MATERIAL).take();
            at(h, MATERIAL).insert(stack, false);
        });
        h.runAtTickTime(85, () -> unchanged(h));
        h.runAtTickTime(120, () -> { check(h, count(h, Items.COBBLESTONE) == 64, "Reinsert must restart a full ritual"); h.succeed(); });
    }

    private static void breakingDropsContents(GameTestHelper h) {
        setup(h); fill(h);
        h.runAtTickTime(30, () -> h.getLevel().destroyBlock(h.absolutePos(MATERIAL), true));
        h.runAtTickTime(100, () -> {
            check(h, count(h, Items.COBBLESTONE) == 1, "Breaking must return exactly the one stored material");
            check(h, count(h, AltarOfAbundance.MATERIAL.asItem()) == 1, "Breaking must drop pedestal item");
            check(h, at(h, OFFERING).displayedItem().is(Items.DIAMOND), "Broken structure must preserve offering");
            h.succeed();
        });
    }

    private static void saveReload(GameTestHelper h) {
        setup(h); fill(h);
        h.runAtTickTime(40, () -> {
            ServerLevel level = h.getLevel();
            for (BlockPos pos : List.of(MATERIAL, OFFERING)) {
                PedestalBlockEntity original = at(h, pos);
                CompoundTag saved = original.saveWithFullMetadata(level.registryAccess());
                BlockEntity loaded = BlockEntity.loadStatic(h.absolutePos(pos), original.getBlockState(), saved, level.registryAccess());
                check(h, loaded instanceof PedestalBlockEntity, "Saved pedestal must deserialize");
                level.removeBlockEntity(h.absolutePos(pos));
                level.setBlockEntity(loaded);
                check(h, at(h, pos).progress() == 0 && at(h, pos).displayedItem().getCount() == 1, "Reload must retain item and discard progress");
            }
        });
        h.runAtTickTime(85, () -> unchanged(h));
        h.runAtTickTime(125, () -> { check(h, count(h, Items.COBBLESTONE) == 64, "Reloaded altar must finish exactly once"); h.succeed(); });
    }

    private static void twoPlayers(GameTestHelper h) {
        setup(h);
        Player first = h.makeMockPlayer(GameType.SURVIVAL);
        Player second = h.makeMockPlayer(GameType.SURVIVAL);
        for (Player player : List.of(first, second)) {
            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.COBBLESTONE, 2));
            h.useBlock(MATERIAL, player);
        }
        check(h, first.getMainHandItem().getCount() == 1 && second.getMainHandItem().getCount() == 2, "Only one player's insertion may consume material");
        first.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        second.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        h.useBlock(MATERIAL, first);
        h.useBlock(MATERIAL, second);
        check(h, first.getInventory().countItem(Items.COBBLESTONE) == 1 && second.getInventory().countItem(Items.COBBLESTONE) == 0, "Only one player may retrieve stored item");
        fill(h);
        h.runAtTickTime(100, () -> { check(h, count(h, Items.COBBLESTONE) == 64, "Interactions must not duplicate ritual output"); h.succeed(); });
    }

    private static void preservesComponents(GameTestHelper h) {
        setup(h);
        ItemStack stack = new ItemStack(Items.OBSIDIAN);
        stack.set(DataComponents.CUSTOM_NAME, Component.literal("Sacred stone"));
        at(h, MATERIAL).insert(stack, false);
        at(h, OFFERING).insert(new ItemStack(Items.DIAMOND), false);
        h.runAtTickTime(100, () -> {
            check(h, count(h, Items.OBSIDIAN) == 64, "Obsidian should produce full stack");
            check(h, drops(h).getFirst().getItem().getHoverName().getString().equals("Sacred stone"), "Item components must be preserved");
            h.succeed();
        });
    }

    private static void recheckAtCompletion(GameTestHelper h) {
        setup(h); fill(h);
        h.runAtTickTime(50, () -> h.setBlock(OUTPUT.above(), Blocks.STONE));
        h.runAtTickTime(100, () -> { unchanged(h); h.setBlock(OUTPUT.above(), Blocks.AIR); });
        h.runAtTickTime(140, () -> unchanged(h));
        h.runAtTickTime(180, () -> { check(h, count(h, Items.COBBLESTONE) == 64, "Unblocking must start a fresh ritual"); h.succeed(); });
    }

    private static void recipes(GameTestHelper h) {
        for (String role : List.of("material", "offering", "output")) {
            var key = ResourceKey.<Recipe<?>>create(Registries.RECIPE, AltarOfAbundance.id(role + "_pedestal"));
            RecipeHolder<?> holder = h.getLevel().recipeAccess().byKey(key).orElseThrow();
            ItemStack special = new ItemStack(switch (role) {
                case "material" -> Items.SMOOTH_STONE;
                case "offering" -> Items.DIAMOND;
                default -> Items.CHEST;
            });
            List<ItemStack> grid = List.of(new ItemStack(Items.GOLD_INGOT), special, new ItemStack(Items.GOLD_INGOT),
                new ItemStack(Items.COBBLESTONE),new ItemStack(Items.COBBLESTONE),new ItemStack(Items.COBBLESTONE),
                new ItemStack(Items.COBBLESTONE),new ItemStack(Items.COBBLESTONE),new ItemStack(Items.COBBLESTONE));
            var recipe = (net.minecraft.world.item.crafting.ShapedRecipe) holder.value();
            CraftingInput input = CraftingInput.of(3, 3, grid);
            check(h, recipe.matches(input, h.getLevel()), "Survival recipe must match " + role);
            ItemStack output = recipe.assemble(input);
            check(h, output.getCount() == 1 && BuiltInRegistries.ITEM.getKey(output.getItem()).equals(AltarOfAbundance.id(role + "_pedestal")), "Recipe must produce exactly its pedestal");
        }
        h.succeed();
    }

    private static void chunkUnload(GameTestHelper h) {
        setup(h); fill(h);
        h.runAtTickTime(40, () -> {
            check(h, at(h, OFFERING).progress() > 0, "Ritual must be active before chunk lifecycle callback");
            at(h, MATERIAL).onChunkUnloaded();
            check(h, at(h, OFFERING).progress() == 0, "Participant unloading must reset offering progress");
        });
        h.runAtTickTime(85, () -> unchanged(h));
        h.runAtTickTime(125, () -> { check(h, count(h, Items.COBBLESTONE) == 64, "Resumed ritual must start from zero"); h.succeed(); });
    }

    private static void pickup(GameTestHelper h) {
        setup(h); fill(h);
        h.runAtTickTime(100, () -> {
            Player player = h.makeMockPlayer(GameType.SURVIVAL);
            for (ItemEntity drop : drops(h)) drop.playerTouch(player);
            check(h, player.getInventory().countItem(Items.COBBLESTONE) == 64, "Finished stack must be collectible");
            check(h, count(h, Items.COBBLESTONE) == 0, "Pickup must remove item entity");
            h.succeed();
        });
    }
}
