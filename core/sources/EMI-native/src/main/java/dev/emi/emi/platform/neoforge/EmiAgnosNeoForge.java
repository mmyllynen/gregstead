package dev.emi.emi.platform.neoforge;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

import net.neoforged.neoforge.client.ClientHooks;
import org.apache.commons.lang3.text.WordUtils;
import org.objectweb.asm.Type;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.emi.emi.EmiPort;
import dev.emi.emi.EmiRenderHelper;
import dev.emi.emi.EmiUtil;
import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.stack.FluidEmiStack;
import dev.emi.emi.platform.EmiAgnos;
import dev.emi.emi.recipe.EmiBrewingRecipe;
import dev.emi.emi.registry.EmiPluginContainer;
import dev.emi.emi.runtime.EmiLog;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.fluids.FluidStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforgespi.language.ModFileScanData;

public class EmiAgnosNeoForge extends EmiAgnos {
	static {
		EmiAgnos.delegate = new EmiAgnosNeoForge();
	}

	@Override
	protected boolean isForgeAgnos() {
		return true;
	}

	@SuppressWarnings("deprecation")
	@Override
	protected String getModNameAgnos(String namespace) {
		if (namespace.equals("c")) {
			return "Common";
		}
		Optional<? extends ModContainer> container = ModList.get().getModContainerById(namespace);
		if (container.isPresent()) {
			return container.get().getModInfo().getDisplayName();
		}
		container = ModList.get().getModContainerById(namespace.replace('_', '-'));
		if (container.isPresent()) {
			return container.get().getModInfo().getDisplayName();
		}
		return WordUtils.capitalizeFully(namespace.replace('_', ' '));
	}

	@Override
	protected Path getConfigDirectoryAgnos() {
		return FMLPaths.CONFIGDIR.get();
	}

	@Override
	protected boolean isDevelopmentEnvironmentAgnos() {
		return !FMLLoader.getCurrent().isProduction();
	}

	@Override
	protected boolean isModLoadedAgnos(String id) {
		return ModList.get().isLoaded(id);
	}

	@Override
	protected List<String> getAllModNamesAgnos() {
		return ModList.get().getMods().stream().map(m -> m.getDisplayName()).toList();
	}

	@Override
	protected List<String> getModsWithPluginsAgnos() {
		List<String> mods = Lists.newArrayList();
		Type entrypointType = Type.getType(EmiEntrypoint.class);
		for (ModFileScanData data : ModList.get().getAllScanData()) {
			for (ModFileScanData.AnnotationData annot : data.getAnnotations()) {
				try {
					if (entrypointType.equals(annot.annotationType())) {
						mods.add(data.getIModInfoData().get(0).getMods().get(0).getModId());
					}
				} catch (Throwable t) {
					EmiLog.error("Exception constructing entrypoint:", t);
				}
			}
		}
		return mods;
	}

	@Override
	protected List<EmiPluginContainer> getPluginsAgnos() {
		List<EmiPluginContainer> containers = Lists.newArrayList();
		Type entrypointType = Type.getType(EmiEntrypoint.class);
		for (ModFileScanData data : ModList.get().getAllScanData()) {
			for (ModFileScanData.AnnotationData annot : data.getAnnotations()) {
				try {
					if (entrypointType.equals(annot.annotationType())) {
						Class<?> clazz = Class.forName(annot.memberName());
						if (EmiPlugin.class.isAssignableFrom(clazz)) {
							Class<? extends EmiPlugin> pluginClass = clazz.asSubclass(EmiPlugin.class);
							EmiPlugin plugin = pluginClass.getConstructor().newInstance();
							String id = data.getIModInfoData().get(0).getMods().get(0).getModId();
							containers.add(new EmiPluginContainer(plugin, id));
						} else {
							EmiLog.error("EmiEntrypoint " + annot.memberName() + " does not implement EmiPlugin");
						}
					}
				} catch (Throwable t) {
					EmiLog.error("Exception constructing entrypoint:", t);
				}
			}
		}
		return containers;
	}

	@Override
	protected void addBrewingRecipesAgnos(EmiRegistry registry) {
		var level = Minecraft.getInstance().level;
		if (level == null) return;
		for (var holder : dev.emi.emi.runtime.NativeRecipeContents.map().byType(net.minecraft.world.item.crafting.RecipeType.BREWING)) {
			var recipe = holder.value();
			var inputs = dev.emi.emi.recipe.NativeBrewingData.choices(recipe.getInput(), level.registryAccess());
			var reagents = dev.emi.emi.recipe.NativeBrewingData.choices(recipe.getReagent(), level.registryAccess());
			if (inputs.isEmpty() || reagents.isEmpty()) {
				throw new IllegalStateException("No display representatives for native brewing recipe " + holder.id());
			}
			registry.addRecipe(new EmiBrewingRecipe(
				EmiIngredient.of(inputs.stream().map(EmiStack::of).toList()),
				EmiIngredient.of(reagents.stream().map(EmiStack::of).toList()),
				EmiStack.of(recipe.getOutput().create()), holder.id().identifier()));
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	protected List<String> getAllModAuthorsAgnos() {
		return ModList.get().getMods().stream().flatMap(m -> {
			Optional<Object> opt = m.getConfig().getConfigElement("authors");
			if (opt.isPresent()) {
				Object obj = opt.get();
				if (obj instanceof String authors) {
					return Lists.newArrayList(authors.split("\\,")).stream().map(s -> s.trim());
				} else if (obj instanceof List<?> list) {
					if (list.size() > 0 && list.get(0) instanceof String) {
						List<String> authors = (List<String>) list;
						return authors.stream();
					}
				}
			}
			return Stream.empty();
		}).distinct().toList();
	}

	@Override
	protected List<ClientTooltipComponent> getItemTooltipAgnos(ItemStack stack) {
		Minecraft client = Minecraft.getInstance();
		return ClientHooks.gatherTooltipComponents(stack, Screen.getTooltipFromItem(client, stack), stack.getTooltipImage(), 0, Integer.MAX_VALUE, Integer.MAX_VALUE, client.font);
	}

	@Override
	protected Component getFluidNameAgnos(Fluid fluid, DataComponentPatch componentChanges) {
		return new FluidStack(fluid.builtInRegistryHolder(), 1000, componentChanges).getHoverName();
	}

	@Override
	protected List<Component> getFluidTooltipAgnos(Fluid fluid, DataComponentPatch componentChanges) {
		List<Component> tooltip = Lists.newArrayList();
		tooltip.add(getFluidName(fluid, componentChanges));
		Minecraft client = Minecraft.getInstance();
		if (client.options.advancedItemTooltips) {
			tooltip.add(EmiPort.literal(EmiPort.getFluidRegistry().getKey(fluid).toString()).withStyle(ChatFormatting.DARK_GRAY));
		}
		return tooltip;
	}

	@Override
	protected boolean isFloatyFluidAgnos(FluidEmiStack stack) {
		FluidStack fs = new FluidStack(stack.getKeyOfType(Fluid.class).builtInRegistryHolder(), 1000, stack.getComponentChanges());
		return fs.getFluid().getFluidType().isLighterThanAir();
	}

	@Override
    protected void renderFluidAgnos(FluidEmiStack stack,net.minecraft.client.gui.GuiGraphicsExtractor graphics,int x,int y,float delta,int xOff,int yOff,int width,int height,float depth) {
        FluidStack fluid=new FluidStack(stack.getKeyOfType(Fluid.class).builtInRegistryHolder(),1000,stack.getComponentChanges());
        dev.emi.emi.recipe.NativeFluidSprite.draw(graphics,fluid,x,y,xOff,yOff,width,height,depth);
    }

	@Override
	protected EmiStack createFluidStackAgnos(Object object) {
		if (object instanceof FluidStack f) {
			return EmiStack.of(f.getFluid(), f.getComponentsPatch(), f.getAmount());
		}
		return EmiStack.EMPTY;
	}


	@Override
	protected Map<Item, net.minecraft.world.item.component.CookingFuel> getFuelMapAgnos() {
		var fuelMap = new java.util.HashMap<Item, net.minecraft.world.item.component.CookingFuel>();
        for (Item item : EmiPort.getItemRegistry()) {
            var fuel = item.components().get(net.minecraft.core.component.DataComponents.COOKING_FUEL);
            if (fuel != null) fuelMap.put(item, fuel);
        }
        return Map.copyOf(fuelMap);
	}

	@Override
	protected ItemModel getBakedTagModelAgnos(Identifier id) {
		return dev.emi.emi.recipe.NativeTagModels.get(id);
	}

	@Override
	protected boolean isEnchantableAgnos(ItemStack stack, Enchantment enchantment) {
		var level = Minecraft.getInstance().level;
		if (level == null) return false;
		return dev.emi.emi.recipe.NativeEnchantmentSupport.supports(stack, enchantment, level.registryAccess());
	}
}
