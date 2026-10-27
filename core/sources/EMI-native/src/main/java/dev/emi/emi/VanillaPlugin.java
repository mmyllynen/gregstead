package dev.emi.emi;

import static dev.emi.emi.api.recipe.VanillaEmiRecipeCategories.ANVIL_REPAIRING;
import static dev.emi.emi.api.recipe.VanillaEmiRecipeCategories.BLASTING;
import static dev.emi.emi.api.recipe.VanillaEmiRecipeCategories.BREWING;
import static dev.emi.emi.api.recipe.VanillaEmiRecipeCategories.CAMPFIRE_COOKING;
import static dev.emi.emi.api.recipe.VanillaEmiRecipeCategories.COMPOSTING;
import static dev.emi.emi.api.recipe.VanillaEmiRecipeCategories.CRAFTING;
import static dev.emi.emi.api.recipe.VanillaEmiRecipeCategories.FUEL;
import static dev.emi.emi.api.recipe.VanillaEmiRecipeCategories.GRINDING;
import static dev.emi.emi.api.recipe.VanillaEmiRecipeCategories.INFO;
import static dev.emi.emi.api.recipe.VanillaEmiRecipeCategories.SMELTING;
import static dev.emi.emi.api.recipe.VanillaEmiRecipeCategories.SMITHING;
import static dev.emi.emi.api.recipe.VanillaEmiRecipeCategories.SMOKING;
import static dev.emi.emi.api.recipe.VanillaEmiRecipeCategories.STONECUTTING;
import static dev.emi.emi.api.recipe.VanillaEmiRecipeCategories.WORLD_INTERACTION;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.inventory.BlastFurnaceMenu;
import net.minecraft.world.inventory.FurnaceMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.SmokerMenu;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.HoneycombItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.crafting.DyeRecipe;
import net.minecraft.world.item.crafting.BannerDuplicateRecipe;
import net.minecraft.world.item.crafting.BlastingRecipe;
import net.minecraft.world.item.crafting.BookCloningRecipe;
import net.minecraft.world.item.crafting.CampfireCookingRecipe;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.FireworkRocketRecipe;
import net.minecraft.world.item.crafting.FireworkStarFadeRecipe;
import net.minecraft.world.item.crafting.FireworkStarRecipe;
import net.minecraft.world.item.crafting.MapExtendingRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.RepairItemRecipe;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraft.world.item.crafting.ShieldDecorationRecipe;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.item.crafting.SmithingRecipe;
import net.minecraft.world.item.crafting.SmokingRecipe;
import net.minecraft.world.item.crafting.StonecutterRecipe;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ComposterBlock;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.level.block.TallFlowerBlock;
import net.minecraft.world.level.block.WeatheringCopper;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mojang.datafixers.util.Pair;

import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiInitRegistry;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiCraftingRecipe;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.recipe.EmiRecipeSorting;
import dev.emi.emi.api.recipe.EmiWorldInteractionRecipe;
import dev.emi.emi.api.render.EmiRenderable;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.Comparison;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiRegistryAdapter;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.stack.FluidEmiStack;
import dev.emi.emi.api.stack.ItemEmiStack;
import dev.emi.emi.api.stack.ListEmiIngredient;
import dev.emi.emi.api.stack.TagEmiIngredient;
import dev.emi.emi.api.widget.Bounds;
import dev.emi.emi.api.widget.GeneratedSlotWidget;
import dev.emi.emi.config.EffectLocation;
import dev.emi.emi.config.EmiConfig;
import dev.emi.emi.config.FluidUnit;
import dev.emi.emi.handler.CookingRecipeHandler;
import dev.emi.emi.handler.CraftingRecipeHandler;
import dev.emi.emi.handler.InventoryRecipeHandler;
import dev.emi.emi.handler.StonecuttingRecipeHandler;
import dev.emi.emi.mixin.accessor.AxeItemAccessor;
import dev.emi.emi.mixin.accessor.HandledScreenAccessor;
import dev.emi.emi.mixin.accessor.HoeItemAccessor;
import dev.emi.emi.mixin.accessor.ShovelItemAccessor;
import dev.emi.emi.mixin.accessor.SmithingTransformRecipeAccessor;
import dev.emi.emi.mixin.accessor.SmithingTrimRecipeAccessor;
import dev.emi.emi.platform.EmiAgnos;
import dev.emi.emi.platform.EmiClient;
import dev.emi.emi.recipe.EmiAnvilRecipe;
import dev.emi.emi.recipe.EmiCompostingRecipe;
import dev.emi.emi.recipe.EmiCookingRecipe;
import dev.emi.emi.recipe.EmiFuelRecipe;
import dev.emi.emi.recipe.EmiGrindstoneRecipe;
import dev.emi.emi.recipe.EmiShapedRecipe;
import dev.emi.emi.recipe.EmiShapelessRecipe;
import dev.emi.emi.recipe.EmiSmithingRecipe;
import dev.emi.emi.recipe.EmiStonecuttingRecipe;
import dev.emi.emi.recipe.EmiTagRecipe;
import dev.emi.emi.recipe.special.EmiAnvilEnchantRecipe;
import dev.emi.emi.recipe.special.EmiAnvilRepairItemRecipe;
import dev.emi.emi.recipe.special.EmiArmorDyeRecipe;
import dev.emi.emi.recipe.special.EmiBannerDuplicateRecipe;
import dev.emi.emi.recipe.special.EmiBannerShieldRecipe;
import dev.emi.emi.recipe.special.EmiBookCloningRecipe;
import dev.emi.emi.recipe.special.EmiFireworkRocketRecipe;
import dev.emi.emi.recipe.special.EmiFireworkStarFadeRecipe;
import dev.emi.emi.recipe.special.EmiFireworkStarRecipe;
import dev.emi.emi.recipe.special.EmiGrindstoneDisenchantingBookRecipe;
import dev.emi.emi.recipe.special.EmiGrindstoneDisenchantingRecipe;
import dev.emi.emi.recipe.special.EmiMapCloningRecipe;
import dev.emi.emi.recipe.special.EmiRepairItemRecipe;
import dev.emi.emi.recipe.special.EmiSmithingTrimRecipe;
import dev.emi.emi.recipe.special.EmiSuspiciousStewRecipe;
import dev.emi.emi.registry.EmiTags;
import dev.emi.emi.runtime.EmiDrawContext;
import dev.emi.emi.runtime.EmiLog;
import dev.emi.emi.runtime.EmiReloadLog;
import dev.emi.emi.runtime.EmiTagKey;
import dev.emi.emi.stack.serializer.FluidEmiStackSerializer;
import dev.emi.emi.stack.serializer.ItemEmiStackSerializer;
import dev.emi.emi.stack.serializer.ListEmiIngredientSerializer;
import dev.emi.emi.stack.serializer.TagEmiIngredientSerializer;

@EmiEntrypoint
public class VanillaPlugin implements EmiPlugin {
	public static EmiRecipeCategory TAG = new EmiRecipeCategory(EmiPort.id("emi:tag"),
		EmiStack.of(Items.NAME_TAG), simplifiedRenderer(240, 208), EmiRecipeSorting.none());
	
	public static EmiRecipeCategory INGREDIENT = new EmiRecipeCategory(EmiPort.id("emi:ingredient"),
		EmiStack.of(Items.COMPASS), simplifiedRenderer(240, 208));
	public static EmiRecipeCategory RESOLUTION = new EmiRecipeCategory(EmiPort.id("emi:resolution"),
		EmiStack.of(Items.COMPASS), simplifiedRenderer(240, 208));

	static {
		CRAFTING = new EmiRecipeCategory(EmiPort.id("minecraft:crafting"),
			EmiStack.of(Items.CRAFTING_TABLE), simplifiedRenderer(240, 240), EmiRecipeSorting.compareOutputThenInput());
		SMELTING = new EmiRecipeCategory(EmiPort.id("minecraft:smelting"),
			EmiStack.of(Items.FURNACE), simplifiedRenderer(224, 240), EmiRecipeSorting.compareOutputThenInput());
		BLASTING = new EmiRecipeCategory(EmiPort.id("minecraft:blasting"),
			EmiStack.of(Items.BLAST_FURNACE), simplifiedRenderer(208, 240), EmiRecipeSorting.compareOutputThenInput());
		SMOKING = new EmiRecipeCategory(EmiPort.id("minecraft:smoking"),
			EmiStack.of(Items.SMOKER), simplifiedRenderer(192, 240), EmiRecipeSorting.compareOutputThenInput());
		CAMPFIRE_COOKING = new EmiRecipeCategory(EmiPort.id("minecraft:campfire_cooking"),
			EmiStack.of(Items.CAMPFIRE), simplifiedRenderer(176, 240), EmiRecipeSorting.compareOutputThenInput());
		STONECUTTING = new EmiRecipeCategory(EmiPort.id("minecraft:stonecutting"),
			EmiStack.of(Items.STONECUTTER), simplifiedRenderer(160, 240), EmiRecipeSorting.compareInputThenOutput());
		SMITHING = new EmiRecipeCategory(EmiPort.id("minecraft:smithing"),
			EmiStack.of(Items.SMITHING_TABLE), simplifiedRenderer(240, 224), EmiRecipeSorting.compareInputThenOutput());
		ANVIL_REPAIRING = new EmiRecipeCategory(EmiPort.id("emi:anvil_repairing"),
			EmiStack.of(Items.ANVIL), simplifiedRenderer(240, 224), EmiRecipeSorting.none());
		GRINDING = new EmiRecipeCategory(EmiPort.id("emi:grinding"),
			EmiStack.of(Items.GRINDSTONE), simplifiedRenderer(192, 224), EmiRecipeSorting.none());
		BREWING = new EmiRecipeCategory(EmiPort.id("minecraft:brewing"),
			EmiStack.of(Items.BREWING_STAND), simplifiedRenderer(224, 224), EmiRecipeSorting.none());
		WORLD_INTERACTION = new EmiRecipeCategory(EmiPort.id("emi:world_interaction"),
			EmiStack.of(Items.GRASS_BLOCK), simplifiedRenderer(208, 224), EmiRecipeSorting.none());
		EmiRenderable flame = (matrices, x, y, delta) -> {
			EmiTexture.FULL_FLAME.render(matrices, x + 1, y + 1, delta);
		};
		FUEL = new EmiRecipeCategory(EmiPort.id("emi:fuel"), flame, flame, EmiRecipeSorting.compareInputThenOutput());
		COMPOSTING = new EmiRecipeCategory(EmiPort.id("emi:composting"), EmiStack.of(Items.COMPOSTER),
			EmiStack.of(Items.COMPOSTER), EmiRecipeSorting.compareInputThenOutput());
		INFO = new EmiRecipeCategory(EmiPort.id("emi:info"),
			EmiStack.of(Items.WRITABLE_BOOK), simplifiedRenderer(208, 224), EmiRecipeSorting.none());
	}


	@Override
	public void initialize(EmiInitRegistry registry) {
		registry.addIngredientSerializer(ItemEmiStack.class, new ItemEmiStackSerializer());
		registry.addIngredientSerializer(FluidEmiStack.class, new FluidEmiStackSerializer());
		registry.addIngredientSerializer(TagEmiIngredient.class, new TagEmiIngredientSerializer());
		registry.addIngredientSerializer(ListEmiIngredient.class, new ListEmiIngredientSerializer());

		registry.addRegistryAdapter(EmiRegistryAdapter.simple(Item.class, EmiPort.getItemRegistry(), EmiStack::of));
		registry.addRegistryAdapter(EmiRegistryAdapter.simple(Fluid.class, EmiPort.getFluidRegistry(), EmiStack::of));
	}

	@Override
	public void register(EmiRegistry registry) {
		registry.addCategory(CRAFTING);
		registry.addCategory(SMELTING);
		registry.addCategory(BLASTING);
		registry.addCategory(SMOKING);
		registry.addCategory(CAMPFIRE_COOKING);
		registry.addCategory(STONECUTTING);
		registry.addCategory(SMITHING);
		registry.addCategory(ANVIL_REPAIRING);
		registry.addCategory(GRINDING);
		registry.addCategory(BREWING);
		registry.addCategory(WORLD_INTERACTION);
		registry.addCategory(FUEL);
		registry.addCategory(COMPOSTING);
		registry.addCategory(INFO);
		registry.addCategory(TAG);
		registry.addCategory(INGREDIENT);
		registry.addCategory(RESOLUTION);

		registry.addWorkstation(CRAFTING, EmiStack.of(Items.CRAFTING_TABLE));
		registry.addWorkstation(SMELTING, EmiStack.of(Items.FURNACE));
		registry.addWorkstation(BLASTING, EmiStack.of(Items.BLAST_FURNACE));
		registry.addWorkstation(SMOKING, EmiStack.of(Items.SMOKER));
		registry.addWorkstation(CAMPFIRE_COOKING, EmiStack.of(Items.CAMPFIRE));
		registry.addWorkstation(CAMPFIRE_COOKING, EmiStack.of(Items.SOUL_CAMPFIRE));
		registry.addWorkstation(STONECUTTING, EmiStack.of(Items.STONECUTTER));
		registry.addWorkstation(SMITHING, EmiStack.of(Items.SMITHING_TABLE));
		registry.addWorkstation(ANVIL_REPAIRING, EmiStack.of(Items.ANVIL));
		registry.addWorkstation(ANVIL_REPAIRING, EmiStack.of(Items.CHIPPED_ANVIL));
		registry.addWorkstation(ANVIL_REPAIRING, EmiStack.of(Items.DAMAGED_ANVIL));
		registry.addWorkstation(BREWING, EmiStack.of(Items.BREWING_STAND));
		registry.addWorkstation(GRINDING, EmiStack.of(Items.GRINDSTONE));
		registry.addWorkstation(COMPOSTING, EmiStack.of(Items.COMPOSTER));

		registry.addRecipeHandler(null, new InventoryRecipeHandler());
		registry.addRecipeHandler(MenuType.CRAFTING, new CraftingRecipeHandler());
		registry.addRecipeHandler(MenuType.FURNACE, new CookingRecipeHandler<FurnaceMenu>(SMELTING));
		registry.addRecipeHandler(MenuType.BLAST_FURNACE, new CookingRecipeHandler<BlastFurnaceMenu>(BLASTING));
		registry.addRecipeHandler(MenuType.SMOKER, new CookingRecipeHandler<SmokerMenu>(SMOKING));
		registry.addRecipeHandler(MenuType.STONECUTTER, new StonecuttingRecipeHandler());

		registry.addExclusionArea(CreativeModeInventoryScreen.class, (screen, consumer) -> {
			int left = ((HandledScreenAccessor) screen).getX();
			int top = ((HandledScreenAccessor) screen).getY();
			int width = ((HandledScreenAccessor) screen).getBackgroundWidth();
			int bottom = top + ((HandledScreenAccessor) screen).getBackgroundHeight();
			consumer.accept(new Bounds(left, top - 28, width, 28));
			consumer.accept(new Bounds(left, bottom, width, 28));
		});

		registry.addGenericExclusionArea((screen, consumer) -> {
            if(screen instanceof InventoryScreen || screen instanceof CreativeModeInventoryScreen) {
                var client=Minecraft.getInstance();
                var inv=(net.minecraft.client.gui.screens.inventory.AbstractContainerScreen<?>)screen;
                var access=(HandledScreenAccessor)inv;
                int count=(int)client.player.getActiveEffects().stream().filter(net.neoforged.neoforge.client.ClientHooks::shouldRenderEffect).count();
                dev.emi.emi.screen.widget.NativeEffectLayout.layout(EmiConfig.effectLocation.name(),screen.width,access.getX(),access.getY(),access.getBackgroundWidth(),count,
                    screen instanceof CreativeModeInventoryScreen,EmiAgnos.isModLoaded("inventorytabs"),EmiAgnos.isForge()).ifPresent(layout -> consumer.accept(layout.bounds()));
            }
		});

		Comparison potionComparison = Comparison.compareData(stack -> stack.get(DataComponents.POTION_CONTENTS));

		registry.setDefaultComparison(Items.POTION, potionComparison);
		registry.setDefaultComparison(Items.SPLASH_POTION, potionComparison);
		registry.setDefaultComparison(Items.LINGERING_POTION, potionComparison);
		registry.setDefaultComparison(Items.TIPPED_ARROW, potionComparison);
		registry.setDefaultComparison(Items.ENCHANTED_BOOK, EmiPort.compareStrict());

		Set<Item> hiddenItems = Stream.concat(
			EmiTagKey.of(EmiPort.getItemRegistry(), EmiTags.HIDDEN_FROM_RECIPE_VIEWERS).stream(),
			EmiPort.getDisabledItems()
		).collect(Collectors.toSet());


		for (CraftingRecipe recipe : getRecipes(registry, RecipeType.CRAFTING)) {
			Identifier id = EmiPort.getId(recipe);
			if (recipe instanceof MapExtendingRecipe map) {
				EmiStack paper = EmiStack.of(Items.PAPER);
				addRecipeSafe(registry, () -> new EmiCraftingRecipe(List.of(
						paper, paper, paper, paper,
						EmiStack.of(Items.FILLED_MAP),
						paper, paper, paper, paper
				), 
						EmiStack.of(Items.FILLED_MAP),
						id, false), recipe);
			} else if (recipe instanceof ShapedRecipe shaped && shaped.getWidth() <= 3 && shaped.getHeight() <= 3) {
				addRecipeSafe(registry, () -> new EmiShapedRecipe(shaped), recipe);
			} else if (recipe instanceof ShapelessRecipe shapeless && shapeless.placementInfo().ingredients().size() <= 9) {
				addRecipeSafe(registry, () -> new EmiShapelessRecipe(shapeless), recipe);
			} else if (recipe instanceof net.minecraft.world.item.crafting.TransmuteRecipe transmute) {
                addRecipeSafe(registry,() -> new dev.emi.emi.recipe.special.EmiTransmuteRecipe(
                    dev.emi.emi.recipe.NativeTransmuteCrafting.plan(transmute,net.minecraft.client.Minecraft.getInstance().level),id),recipe);
            } else if (recipe instanceof DyeRecipe dye) {
                for(ItemStack target:dev.emi.emi.recipe.NativeRecipeData.items(dye.placementInfo().ingredients().getFirst())) {
                    if(!hiddenItems.contains(target.getItem())) {
                        addRecipeSafe(registry,() -> new EmiArmorDyeRecipe(dye,target,
                            synthetic("crafting/dying",EmiUtil.subId(id)+"/"+EmiUtil.subId(target.getItem()))),recipe);
                    }
                }
            } else if (recipe instanceof ShieldDecorationRecipe shield) {
				addRecipeSafe(registry, () -> new EmiBannerShieldRecipe(id), recipe);
			} else if (recipe instanceof BookCloningRecipe book) {
				addRecipeSafe(registry, () -> new EmiBookCloningRecipe(id), recipe);
			} else if (recipe instanceof net.minecraft.world.item.crafting.ImbueRecipe imbue) {
                var level=Minecraft.getInstance().level;
                for(var example:dev.emi.emi.recipe.NativeImbueCrafting.examples(imbue,level)) {
                    addRecipeSafe(registry,() -> new EmiCraftingRecipe(example.inputs().stream().map(EmiStack::of).map(stack -> (EmiIngredient)stack).toList(),
                        EmiStack.of(example.output()),synthetic("crafting/imbue",EmiUtil.subId(id)+"/"+
                            dev.emi.emi.recipe.NativeImbueCrafting.variant(example,level)),false),recipe);
                }
            } else if (recipe instanceof FireworkStarRecipe star) {
				addRecipeSafe(registry, () -> new EmiFireworkStarRecipe(id), recipe);
			} else if (recipe instanceof FireworkStarFadeRecipe star) {
				addRecipeSafe(registry, () -> new EmiFireworkStarFadeRecipe(id), recipe);
			} else if (recipe instanceof FireworkRocketRecipe rocket) {
				addRecipeSafe(registry, () -> new EmiFireworkRocketRecipe(id), recipe);
			} else if (recipe instanceof BannerDuplicateRecipe banner) {
				for (ItemStack candidate : dev.emi.emi.recipe.NativeBannerCopies.inputs(banner, Minecraft.getInstance().level.registryAccess())) {
                    Item i = candidate.getItem();
					if (!hiddenItems.contains(i)) {
						addRecipeSafe(registry, () -> new EmiBannerDuplicateRecipe(banner, candidate, synthetic("crafting/banner_copying", EmiUtil.subId(id) + "/" + EmiUtil.subId(i))), recipe);
					}
				}
			} else if (recipe instanceof RepairItemRecipe tool) {
				for (Item i : EmiRepairItemRecipe.TOOLS) {
					if (!hiddenItems.contains(i)) {
						addRecipeSafe(registry, () -> new EmiRepairItemRecipe(i, synthetic("crafting/repairing", EmiUtil.subId(i))), recipe);
					}
				}
			} else if (!(recipe instanceof CustomRecipe)) {
				try {
                    var level=Minecraft.getInstance().level;
                    var views=dev.emi.emi.recipe.NativeCraftingDisplays.views(recipe,level);
                    for(int viewIndex=0;viewIndex<views.size();viewIndex++) {
                        var view=views.get(viewIndex);
                        var input=view.inputs().stream().<EmiIngredient>map(choices -> choices.isEmpty()?EmiStack.EMPTY:
                            EmiIngredient.of(choices.stream().map(EmiStack::of).map(stack -> (EmiIngredient)stack).toList())).toList();
                        EmiShapedRecipe.setRemainders(input,recipe);
                        Identifier viewId=views.size()==1?id:synthetic("crafting/native_display",EmiUtil.subId(id)+"/"+viewIndex);
                        addRecipeSafe(registry,() -> new EmiCraftingRecipe(input,EmiStack.of(view.output()),viewId,view.shapeless()),recipe);
                    }
				} catch (Exception e) {
					EmiReloadLog.warn("Exception when parsing vanilla crafting recipe " + id, e);
				}
			}
		}

		for (SmeltingRecipe recipe : getRecipes(registry, RecipeType.SMELTING)) {
			addRecipeSafe(registry, () -> new EmiCookingRecipe(recipe, SMELTING, 1, false), recipe);
		}
		for (BlastingRecipe recipe : getRecipes(registry, RecipeType.BLASTING)) {
			addRecipeSafe(registry, () -> new EmiCookingRecipe(recipe, BLASTING, 2, false), recipe);
		}
		for (SmokingRecipe recipe : getRecipes(registry, RecipeType.SMOKING)) {
			addRecipeSafe(registry, () -> new EmiCookingRecipe(recipe, SMOKING, 2, false), recipe);
		}
		for (CampfireCookingRecipe recipe : getRecipes(registry, RecipeType.CAMPFIRE_COOKING)) {
			addRecipeSafe(registry, () -> new EmiCookingRecipe(recipe, CAMPFIRE_COOKING, 1, true), recipe);
		}
		for (SmithingRecipe recipe : getRecipes(registry, RecipeType.SMITHING)) {
			//addRecipeSafe(registry, () -> new EmiSmithingRecipe(recipe), recipe);
			Minecraft client = Minecraft.getInstance();
			if (recipe instanceof SmithingTransformRecipeAccessor stra) {
				addRecipeSafe(registry, () -> new EmiSmithingRecipe(stra.getTemplate().map(EmiIngredient::of).orElse(EmiStack.EMPTY), EmiIngredient.of(stra.getBase()),
					stra.getAddition().map(EmiIngredient::of).orElse(EmiStack.EMPTY), EmiStack.of(EmiPort.getOutput(recipe)), EmiPort.getId(recipe)), recipe);
			} else if (recipe instanceof SmithingTrimRecipeAccessor stra) {
				addRecipeSafe(registry, () -> new EmiSmithingTrimRecipe(EmiIngredient.of(stra.getTemplate()), EmiIngredient.of(stra.getBase()),
					EmiIngredient.of(stra.getAddition()), EmiStack.of(EmiPort.getOutput(recipe)), recipe), recipe);
			}
		}
		for (StonecutterRecipe recipe : getRecipes(registry, RecipeType.STONECUTTING)) {
			addRecipeSafe(registry, () -> new EmiStonecuttingRecipe(recipe), recipe);
		}

		safely("repair", () -> addRepair(registry, hiddenItems));
		safely("brewing", () -> EmiAgnos.addBrewingRecipes(registry));
		safely("world interaction", () -> addWorldInteraction(registry, hiddenItems,
            EmiPort.getItemRegistry().stream().filter(item -> item.getDefaultInstance().is(ItemTags.CAULDRON_CAN_REMOVE_DYE)).toList()));
		safely("fuel", () -> addFuel(registry, hiddenItems));
		safely("composting", () -> addComposting(registry, hiddenItems));

		for (EmiTagKey<?> key : EmiTags.TAGS) {
			if (new TagEmiIngredient(key.raw(), 1).getEmiStacks().size() > 1) {
				addRecipeSafe(registry, () -> new EmiTagRecipe(key.raw()));
			}
		}
	}

	private static void addRepair(EmiRegistry registry, Set<Item> hiddenItems) {
		List<Enchantment> targetedEnchantments = Lists.newArrayList();
		List<Enchantment> universalEnchantments = Lists.newArrayList();
		for (Enchantment enchantment : EmiPort.getEnchantmentRegistry().stream().toList()) {
			try {
				if (enchantment.canEnchant(ItemStack.EMPTY)) {
					universalEnchantments.add(enchantment);
					continue;
				}
			} catch (Throwable t) {
			}
			targetedEnchantments.add(enchantment);
		}
		for (Item i : EmiPort.getItemRegistry()) {
			if (hiddenItems.contains(i)) {
				continue;
			}
			try {
				if (i.components().getOrDefault(DataComponents.MAX_DAMAGE, 0) > 0) {
                    dev.emi.emi.recipe.NativeRepairData.material(i.getDefaultInstance()).ifPresent(material -> {
                        var alternatives=dev.emi.emi.recipe.NativeRecipeData.items(material);
                        if(alternatives.length>0) {
                            Identifier id=synthetic("anvil/repairing/material",EmiUtil.subId(i)+"/"+EmiUtil.subId(alternatives[0].getItem()));
                            addRecipeSafe(registry,() -> new EmiAnvilRecipe(EmiStack.of(i),EmiIngredient.of(material),id));
                        }
                    });
				}
				// TODO used to be isDamageable, the 20.1 impl of that method appears to just be maxDamage > 0 though
				if (i.components().getOrDefault(DataComponents.MAX_DAMAGE, 0) > 0) {
					addRecipeSafe(registry, () -> new EmiAnvilRepairItemRecipe(i, synthetic("anvil/repairing/tool", EmiUtil.subId(i))));
					addRecipeSafe(registry, () -> new EmiGrindstoneRecipe(i, synthetic("grindstone/repairing", EmiUtil.subId(i))));
				}
			} catch (Throwable t) {
				EmiLog.error("Exception thrown registering repair recipes", t);
			}
			try {
				ItemStack defaultStack = i.getDefaultInstance();
				int acceptableEnchantments = 0;
				Consumer<Enchantment> consumer = e -> {
					int max = e.getMaxLevel();
					addRecipeSafe(registry, () -> new EmiAnvilEnchantRecipe(i, e, max,
						synthetic("anvil/enchanting", EmiUtil.subId(i) + "/" + EmiUtil.subId(EmiPort.getEnchantmentRegistry().getKey(e)) + "/" + max)));
				};
				for (Enchantment e : targetedEnchantments) {
					if (e.canEnchant(defaultStack) && defaultStack.isEnchantable()
							&& defaultStack.isEnchantable()
							&& EmiAgnos.isEnchantable(defaultStack, e)) {
						consumer.accept(e);
						acceptableEnchantments++;
					}
				}
				if (acceptableEnchantments > 0) {
					for (Enchantment e : universalEnchantments) {
						if (e.canEnchant(defaultStack)) {
							consumer.accept(e);
							acceptableEnchantments++;
						}
					}
					addRecipeSafe(registry, () -> new EmiGrindstoneDisenchantingRecipe(i, synthetic("grindstone/disenchanting/tool", EmiUtil.subId(i))));
				}
			} catch (Throwable t) {
				EmiReloadLog.warn("Exception thrown registering enchantment recipes", t);
			}
			if (i instanceof BlockItem bi && bi.getBlock() instanceof TallFlowerBlock tf && EmiPort.canTallFlowerDuplicate(tf)) {
				addRecipeSafe(registry, () -> basicWorld(EmiStack.of(bi).setRemainder(EmiStack.of(bi)), EmiStack.of(Items.BONE_MEAL), EmiStack.of(i),
						synthetic("world/flower_duping", EmiUtil.subId(i)), false));
			}
		}


		for (Enchantment e : EmiPort.getEnchantmentRegistry().stream().toList()) {
			if (!EmiPort.getEnchantmentRegistry().wrapAsHolder(e).is(EnchantmentTags.CURSE)) {
				int max = Math.min(10, e.getMaxLevel());
				int min = e.getMinLevel();
				while (min <= max) {
					int level = min;
					addRecipeSafe(registry, () -> new EmiGrindstoneDisenchantingBookRecipe(e, level,
						synthetic("grindstone/disenchanting/book", EmiUtil.subId(EmiPort.getEnchantmentRegistry().getKey(e)) + "/" + level)));
					min++;
				}
			}
		}
	}

    private static void addNativeToolRecipes(EmiRegistry registry, EmiIngredient tool,
            net.minecraft.resources.ResourceKey<net.minecraft.core.component.BlockTransformer> key, String path) {
        var client = Minecraft.getInstance();
        if(client.level==null || client.player==null)return;
        var holder=client.level.registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.BLOCK_TRANSFORMER).getOrThrow(key);
        for(var block:EmiPort.getBlockRegistry()) {
            var preview=dev.emi.emi.recipe.NativeToolTransformPreview.preview(holder,client.level,client.player.blockPosition(),
                block.defaultBlockState(),Blocks.AIR.defaultBlockState(),net.minecraft.core.Direction.UP);
            if(preview.isEmpty() || preview.get().output().is(block))continue;
            var result=preview.get();
            var loot=result.transform().loot().flatMap(keyValue->dev.emi.emi.loot.ToolLootProfiles.get(keyValue.identifier()));
            if(result.transform().loot().isPresent() && loot.isEmpty())
                EmiReloadLog.warn("Native tool recipe loot display unavailable or nonconstant: " + result.transform().loot().get().identifier());
            addRecipeSafe(registry,()-> {
                var builder=EmiWorldInteractionRecipe.builder().id(synthetic(path,EmiUtil.subId(block)))
                    .leftInput(EmiStack.of(block)).rightInput(tool,true).output(EmiStack.of(result.output().getBlock()));
                loot.ifPresent(stacks->stacks.forEach(stack->builder.output(EmiStack.of(stack))));
                if(result.transform().loot().isPresent() && loot.isEmpty())builder.supportsRecipeTree(false);
                return builder.build();
            });
        }
    }

	private static void addWorldInteraction(EmiRegistry registry, Set<Item> hiddenItems, List<Item> dyeableItems) {
		EmiStack concreteWater = EmiStack.of(Fluids.WATER);
		concreteWater.setRemainder(concreteWater);
        for(DyeColor color:DyeColor.values()) {
            addConcreteRecipe(registry,Blocks.CONCRETE_POWDER.pick(color),concreteWater,Blocks.CONCRETE.pick(color));
        }

		EmiIngredient axes = damagedTool(getPreferredTag(List.of(
				"minecraft:axes", "c:axes", "c:tools/axes", "fabric:axes", "forge:tools/axes"
			), EmiStack.of(Items.IRON_AXE)), 1);
        addNativeToolRecipes(registry, axes, net.minecraft.world.item.component.BlockTransformers.AXE, "world/stripping");

		EmiIngredient shears = damagedTool(EmiStack.of(Items.SHEARS), 1);
		addRecipeSafe(registry, () -> EmiWorldInteractionRecipe.builder()
			.id(synthetic("world/shearing", "minecraft/pumpkin"))
			.leftInput(EmiStack.of(Items.PUMPKIN))
			.rightInput(shears, true)
			.output(EmiStack.of(Items.PUMPKIN_SEEDS, 4))
			.output(EmiStack.of(Items.CARVED_PUMPKIN))
			.build());
		EmiIngredient hoes = damagedTool(getPreferredTag(List.of(
				"minecraft:hoes", "c:hoes", "c:tools/hoes", "fabric:hoes", "forge:tools/hoes"
			), EmiStack.of(Items.IRON_HOE)), 1);
        addNativeToolRecipes(registry, hoes, net.minecraft.world.item.component.BlockTransformers.HOE, "world/tilling");

		EmiIngredient shovels = damagedTool(getPreferredTag(List.of(
				"minecraft:shovels", "c:shovels", "c:tools/shovels", "fabric:shovels", "forge:tools/shovels"
			), EmiStack.of(Items.IRON_SHOVEL)), 1);
        addNativeToolRecipes(registry, shovels, net.minecraft.world.item.component.BlockTransformers.SHOVEL, "world/flattening");

		EmiIngredient honeycomb = EmiStack.of(Items.HONEYCOMB);
		for (Map.Entry<Block, Block> entry : HoneycombItem.WAXABLES.get().entrySet()) {
			Identifier id = synthetic("world/waxing", EmiUtil.subId(entry.getKey()));
			addRecipeSafe(registry, () -> basicWorld(EmiStack.of(entry.getKey()), honeycomb, EmiStack.of(entry.getValue()), id, false));
		}

		for (Item i : dyeableItems) {
			if (hiddenItems.contains(i)) {
				continue;
			}
			EmiStack cauldron = EmiStack.of(Items.CAULDRON);
			EmiStack waterThird = EmiStack.of(Fluids.WATER, FluidUnit.BOTTLE);
			int uniq = EmiUtil.RANDOM.nextInt();
			addRecipeSafe(registry, () -> EmiWorldInteractionRecipe.builder()
				.id(synthetic("world/cauldron_washing", EmiUtil.subId(i)))
				.leftInput(EmiStack.EMPTY, s -> new GeneratedSlotWidget(r -> {
					ItemStack stack = i.getDefaultInstance();
					stack.set(DataComponents.DYED_COLOR, new DyedItemColor(r.nextInt(0xFFFFFF + 1)));
					return EmiStack.of(stack);
				}, uniq, s.getBounds().x(), s.getBounds().y()))
				.rightInput(cauldron, true)
				.rightInput(waterThird, false)
				.output(EmiStack.of(i))
				.supportsRecipeTree(false)
				.build());
		}

		EmiStack water = EmiStack.of(Fluids.WATER, FluidUnit.BUCKET);
		EmiStack lava = EmiStack.of(Fluids.LAVA, FluidUnit.BUCKET);
		EmiStack waterCatalyst = water.copy().setRemainder(water);
		EmiStack lavaCatalyst = lava.copy().setRemainder(lava);

		addRecipeSafe(registry, () -> EmiWorldInteractionRecipe.builder()
			.id(synthetic("world/fluid_spring", "minecraft/water"))
			.leftInput(waterCatalyst)
			.rightInput(waterCatalyst, false)
			.output(EmiStack.of(Fluids.WATER, FluidUnit.BUCKET))
			.build());
		addRecipeSafe(registry, () -> EmiWorldInteractionRecipe.builder()
			.id(synthetic("world/fluid_interaction", "minecraft/cobblestone"))
			.leftInput(waterCatalyst)
			.rightInput(lavaCatalyst, false)
			.output(EmiStack.of(Items.COBBLESTONE))
			.build());
		addRecipeSafe(registry, () -> EmiWorldInteractionRecipe.builder()
			.id(synthetic("world/fluid_interaction", "minecraft/stone"))
			.leftInput(waterCatalyst)
			.rightInput(lavaCatalyst, false)
			.output(EmiStack.of(Items.STONE))
			.build());
		addRecipeSafe(registry, () -> EmiWorldInteractionRecipe.builder()
			.id(synthetic("world/fluid_interaction", "minecraft/obsidian"))
			.leftInput(lava)
			.rightInput(waterCatalyst, false)
			.output(EmiStack.of(Items.OBSIDIAN))
			.build());
	
		EmiStack soulSoil = EmiStack.of(Items.SOUL_SOIL);
		soulSoil.setRemainder(soulSoil);
		EmiStack blueIce = EmiStack.of(Items.BLUE_ICE);
		blueIce.setRemainder(blueIce);

		addRecipeSafe(registry, () -> EmiWorldInteractionRecipe.builder()
			.id(synthetic("world/fluid_interaction", "minecraft/basalt"))
			.leftInput(lavaCatalyst)
			.rightInput(soulSoil, false, s -> s.appendTooltip(EmiPort
				.translatable("tooltip.emi.fluid_interaction.basalt.soul_soil", ChatFormatting.GREEN)))
			.rightInput(blueIce, false, s -> s.appendTooltip(EmiPort
				.translatable("tooltip.emi.fluid_interaction.basalt.blue_ice", ChatFormatting.GREEN)))
			.output(EmiStack.of(Items.BASALT))
			.build());

		EmiPort.getFluidRegistry().listElements().forEach(entry -> {
			Fluid fluid = entry.value();
			Item bucket = fluid.getBucket();
			if (fluid.isSource(fluid.defaultFluidState()) && !fluid.defaultFluidState().createLegacyBlock().isAir() && bucket != Items.AIR && fluid instanceof FlowingFluid) {
				addRecipeSafe(registry, () -> basicWorld(EmiStack.of(Items.BUCKET), EmiStack.of(fluid, FluidUnit.BUCKET), EmiStack.of(bucket),
					synthetic("emi", "bucket_filling/" + EmiUtil.subId(fluid)), false));
			}
		});

		addRecipeSafe(registry, () -> basicWorld(EmiStack.of(Items.GLASS_BOTTLE), water,
			EmiStack.of(EmiPort.setPotion(new ItemStack(Items.POTION), Potions.WATER.value())),
			synthetic("world/unique", "minecraft/water_bottle")));

		EmiStack waterBottle = EmiStack.of(EmiPort.setPotion(new ItemStack(Items.POTION), Potions.WATER.value()))
			.setRemainder(EmiStack.of(Items.GLASS_BOTTLE));
		EmiStack mud = EmiStack.of(Items.MUD);
		addRecipeSafe(registry, () -> basicWorld(EmiStack.of(Items.DIRT), waterBottle, mud, synthetic("world/unique", "minecraft/mud"), false));
	}

	private static EmiIngredient damagedTool(EmiIngredient tool, int damage) {
		for (EmiStack stack : tool.getEmiStacks()) {
			ItemStack is = stack.getItemStack().copy();
			is.setDamageValue(1);
			stack.setRemainder(EmiStack.of(is));
		}
		return tool;
	}

	private static EmiIngredient getPreferredTag(List<String> candidates, EmiIngredient fallback) {
		for (String id : candidates) {
			EmiIngredient potential = EmiIngredient.of(TagKey.create(EmiPort.getItemRegistry().key(), EmiPort.id(id)));
			if (!potential.isEmpty()) {
				return potential;
			}
		}
		return fallback;
	}

	private static void addFuel(EmiRegistry registry, Set<Item> hiddenItems) {
		Map<Item, net.minecraft.world.item.component.CookingFuel> fuelMap = EmiAgnos.getFuelMap();
		compressRecipesToTags(fuelMap.keySet().stream().collect(Collectors.toSet()), (a, b) -> {
				return net.minecraft.world.item.component.CookingFuel.CODEC.encodeStart(com.mojang.serialization.JsonOps.INSTANCE, fuelMap.get(a)).getOrThrow().toString()
                    .compareTo(net.minecraft.world.item.component.CookingFuel.CODEC.encodeStart(com.mojang.serialization.JsonOps.INSTANCE, fuelMap.get(b)).getOrThrow().toString());
			}, tag -> {
				EmiIngredient stack = EmiIngredient.of(tag.raw());
				Item item = stack.getEmiStacks().get(0).getItemStack().getItem();
				var fuel = fuelMap.get(item);
				registry.addRecipe(new EmiFuelRecipe(stack, fuel, synthetic("fuel/tag", EmiUtil.subId(tag.id()))));
			}, item -> {
				if (!hiddenItems.contains(item)) {
					var fuel = fuelMap.get(item);
					registry.addRecipe(new EmiFuelRecipe(EmiStack.of(item), fuel, synthetic("fuel/item", EmiUtil.subId(item))));
				}
			});
	}

	private static void addComposting(EmiRegistry registry, Set<Item> hiddenItems) {
        var items=EmiPort.getItemRegistry().stream().filter(item -> item.components().has(DataComponents.COMPOSTABLE)).collect(Collectors.toSet());
        compressRecipesToTags(items,java.util.Comparator.comparing(item -> item.components().get(DataComponents.COMPOSTABLE).layers().toString()),tag -> {
            EmiIngredient stack=EmiIngredient.of(tag.raw());
            var layers=stack.getEmiStacks().getFirst().getItemStack().get(DataComponents.COMPOSTABLE).layers();
            registry.addRecipe(new EmiCompostingRecipe(stack,layers,synthetic("composting/tag",EmiUtil.subId(tag.id()))));
        },item -> {
            if(!hiddenItems.contains(item))registry.addRecipe(new EmiCompostingRecipe(EmiStack.of(item),item.components().get(DataComponents.COMPOSTABLE).layers(),
                synthetic("composting/item",EmiUtil.subId(item))));
        });
	}

	private static void compressRecipesToTags(Set<Item> stacks, Comparator<Item> comparator, Consumer<EmiTagKey<Item>> tagConsumer, Consumer<Item> itemConsumer) {
		Set<Item> handled = Sets.newHashSet();
		outer:
		for (EmiTagKey<Item> key : EmiTags.getTags(EmiPort.getItemRegistry())) {
			List<Item> items = key.getList();
			if (items.size() < 2) {
				continue;
			}
			Item base = items.get(0);
			if (!stacks.contains(base)) {
				continue;
			}
			for (int i = 1; i < items.size(); i++) {
				Item item = items.get(i);
				if (!stacks.contains(item) || comparator.compare(base, item) != 0) {
					continue outer;
				}
			}
			if (handled.containsAll(items)) {
				continue;
			}
			handled.addAll(items);
			tagConsumer.accept(key);
		}
		for (Item item : stacks) {
			if (handled.contains(item)) {
				continue;
			}
			itemConsumer.accept(item);
		}
	}

	private static Identifier synthetic(String type, String name) {
		return EmiPort.id("emi", "/" + type + "/" + name);
	}

	private static <C extends RecipeInput, T extends Recipe<C>> Iterable<T> getRecipes(EmiRegistry registry, RecipeType<T> type) {
		return registry.getRecipeManager().byType(type).stream().map(e -> e.value())::iterator;
	}

	private static void safely(String name, Runnable runnable) {
		try {
			runnable.run();
		} catch (Throwable t) {
			EmiReloadLog.warn("Exception thrown when reloading " + name  + " step in vanilla EMI plugin", t);
		}
	}

	private static void addRecipeSafe(EmiRegistry registry, Supplier<EmiRecipe> supplier) {
		try {
			registry.addRecipe(supplier.get());
		} catch (Throwable e) {
			EmiReloadLog.warn("Exception thrown when parsing EMI recipe (no ID available)", e);
		}
	}

	private static void addRecipeSafe(EmiRegistry registry, Supplier<EmiRecipe> supplier, Recipe<?> recipe) {
		try {
			registry.addRecipe(supplier.get());
		} catch (Throwable e) {
			EmiReloadLog.warn("Exception thrown when parsing vanilla recipe " + EmiPort.getId(recipe), e);
		}
	}

	private static EmiRenderable simplifiedRenderer(int u, int v) {
		return (raw, x, y, delta) -> {
			EmiDrawContext context = EmiDrawContext.wrap(raw);
			context.drawTexture(EmiRenderHelper.WIDGETS, x, y, u, v, 16, 16);
		};
	}

	private static void addConcreteRecipe(EmiRegistry registry, Block powder, EmiStack water, Block result) {
		addRecipeSafe(registry, () -> basicWorld(EmiStack.of(powder), water, EmiStack.of(result),
			synthetic("world/concrete", EmiUtil.subId(result))));
	}

	private static EmiRecipe basicWorld(EmiIngredient left, EmiIngredient right, EmiStack output, Identifier id) {
		return basicWorld(left, right, output, id, true);
	}

	private static EmiRecipe basicWorld(EmiIngredient left, EmiIngredient right, EmiStack output, Identifier id, boolean catalyst) {
		return EmiWorldInteractionRecipe.builder()
			.id(id)
			.leftInput(left)
			.rightInput(right, catalyst)
			.output(output)
			.build();
	}
}