package dev.emi.emi;

import java.text.DecimalFormat;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTextTooltip;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.item.ItemStack;
import org.joml.Vector2i;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.emi.emi.api.EmiApi;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.handler.EmiCraftContext;
import dev.emi.emi.api.recipe.handler.EmiRecipeHandler;
import dev.emi.emi.api.recipe.handler.StandardRecipeHandler;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.Widget;
import dev.emi.emi.api.widget.WidgetHolder;
import dev.emi.emi.config.EmiConfig;
import dev.emi.emi.mixin.accessor.DrawContextAccessor;
import dev.emi.emi.mixin.accessor.OrderedTextTooltipComponentAccessor;
import dev.emi.emi.registry.EmiRecipeFiller;
import dev.emi.emi.runtime.EmiDrawContext;
import dev.emi.emi.runtime.EmiLog;
import dev.emi.emi.screen.EmiScreenManager;

public class EmiRenderHelper {
	public static final DecimalFormat TEXT_FORMAT = new DecimalFormat("#,###.##");
	public static final Component EMPTY_TEXT = EmiPort.literal("");
	public static final Minecraft CLIENT = Minecraft.getInstance();
	public static final Identifier WIDGETS = EmiPort.id("emi", "textures/gui/widgets.png");
	public static final Identifier BUTTONS = EmiPort.id("emi", "textures/gui/buttons.png");
	public static final Identifier BACKGROUND = EmiPort.id("emi", "textures/gui/background.png");
	public static final Identifier GRID = EmiPort.id("emi", "textures/gui/grid.png");
	public static final Identifier DASH = EmiPort.id("emi", "textures/gui/dash.png");
	public static final Identifier CONFIG = EmiPort.id("emi", "textures/gui/config.png");
	public static final Identifier PIECES = EmiPort.id("emi", "textures/gui/pieces.png");

	public static void drawNinePatch(EmiDrawContext context, Identifier texture, int x, int y, int w, int h, int u, int v, int cornerLength, int centerLength) {
		int cor = cornerLength;
		int cen = centerLength;
		int corcen = cor + cen;
		int innerWidth = w - cornerLength * 2;
		int innerHeight = h - cornerLength * 2;
		int coriw = cor + innerWidth;
		int corih = cor + innerHeight;
		// TL
		context.drawTexture(texture, x,         y,         cor,        cor,         u,          v,          cor, cor, 256, 256);
		// T
		context.drawTexture(texture, x + cor,   y,         innerWidth, cor,         u + cor,    v,          cen, cor, 256, 256);
		// TR
		context.drawTexture(texture, x + coriw, y,         cor,        cor,         u + corcen, v,          cor, cor, 256, 256);
		// L
		context.drawTexture(texture, x,         y + cor,   cor,        innerHeight, u,          v + cor,    cor, cen, 256, 256);
		// C
		context.drawTexture(texture, x + cor,   y + cor,   innerWidth, innerHeight, u + cor,    v + cor,    cen, cen, 256, 256);
		// R
		context.drawTexture(texture, x + coriw, y + cor,   cor,        innerHeight, u + corcen, v + cor,    cor, cen, 256, 256);
		// BL
		context.drawTexture(texture, x,         y + corih, cor,        cor,         u,          v + corcen, cor, cor, 256, 256);
		// B
		context.drawTexture(texture, x + cor,   y + corih, innerWidth, cor,         u + cor,    v + corcen, cen, cor, 256, 256);
		// BR
		context.drawTexture(texture, x + coriw, y + corih, cor,        cor,         u + corcen, v + corcen, cor, cor, 256, 256);
	}

	public static void drawTintedSprite(net.minecraft.client.gui.GuiGraphicsExtractor graphics, TextureAtlasSprite sprite,
			int color, int x, int y, int xOff, int yOff, int width, int height) {
		dev.emi.emi.recipe.NativeFluidSprite.draw(graphics, sprite, color, x, y, xOff, yOff, width, height, 1);
	}

	public static void drawScroll(EmiDrawContext context, int x, int y, int width, int height, int progress, int total, int color) {
		if (total <= 1) {
			return;
		}
		int start = x + width * progress / total;
		int end = start + Math.max(width / total, 1);
		if (progress == total - 1) {
			end = x + width;
			start = end - Math.max(width / total, 1);
		}
		context.fill(start, y, end - start, height, color);
	}

	public static Component getEmiText() {
		return
			EmiPort.append(
				EmiPort.append(
					EmiPort.literal("E", Style.EMPTY.withColor(0xeb7bfc)),
					EmiPort.literal("M", Style.EMPTY.withColor(0x7bfca2))),
				EmiPort.literal("I", Style.EMPTY.withColor(0x7bebfc)));
	}

	public static Component getPageText(int page, int total, int maxWidth) {
		Component text = EmiPort.translatable("emi.page", page, total);
		if (CLIENT.font.width(text) > maxWidth) {
			text = EmiPort.translatable("emi.page.short", page, total);
			if (CLIENT.font.width(text) > maxWidth) {
				text = EmiPort.literal("" + page);
				if (CLIENT.font.width(text) > maxWidth) {
					text = EmiPort.literal("");
				}
			}
		}
		return text;
	}

	public static void drawLeftTooltip(Screen screen, EmiDrawContext context, List<ClientTooltipComponent> components, int x, int y) {
		drawTooltip(screen, context, components, x, y, screen.width / 2 - 16,
			(screenWidth, screenHeight, mouseX, mouseY, tooltipWidth, tooltipHeight) -> {
				Vector2i pos = new Vector2i(mouseX, mouseY).add(12, -12);
				pos.x = Math.max(pos.x - 24 - tooltipWidth, 4);
				if (pos.y + tooltipHeight + 3 > screenHeight) {
					pos.y = screenHeight - tooltipHeight - 3;
				}
				return pos;
		});
	}

	public static void drawTooltip(Screen screen, EmiDrawContext context, List<ClientTooltipComponent> components, int x, int y) {
		drawTooltip(screen, context, components, x, y, screen.width / 2 - 16);
	}

	public static void drawTooltip(Screen screen, EmiDrawContext context, List<ClientTooltipComponent> components, int x, int y, int maxWidth) {
		drawTooltip(screen, context, components, x, y, maxWidth, DefaultTooltipPositioner.INSTANCE);
	}

	public static void drawTooltip(Screen screen, EmiDrawContext context, List<ClientTooltipComponent> components, int x, int y, int maxWidth, ClientTooltipPositioner positioner) {
		if (components.isEmpty()) {
			return;
		}
		y = Math.max(16, y);
		// Some mods assume this list will be mutable, oblige them
		List<ClientTooltipComponent> mutable = Lists.newArrayList();
		int wrapWidth = Math.max(components.stream()
			.map(c -> c instanceof ClientTextTooltip ? 0 : c.getWidth(CLIENT.font))
			.max(Integer::compare).orElse(0), maxWidth);
		for (ClientTooltipComponent comp : components) {
			if (comp instanceof ClientTextTooltip ottc && ottc.getWidth(CLIENT.font) > wrapWidth) {
				try {
					FormattedCharSequence ordered = ((OrderedTextTooltipComponentAccessor) ottc).getText();
					MutableComponent text = Component.empty();
					// Mojang, what is this??? Please give me some other way to wrap
					ordered.accept(((var1, style, codepoint) -> {
						text.append(EmiPort.literal(String.valueOf(Character.toChars(codepoint)), style));
						return true;
					}));
					for (FormattedCharSequence o : CLIENT.font.split(text, wrapWidth)) {
						mutable.add(ClientTooltipComponent.create(o));
					}
				} catch (Exception e) {
					EmiLog.error("Error converting text", e);
					mutable.add(comp);
				}
			} else {
				mutable.add(comp);
			}
		}
        ItemStack tooltipStack=ItemStack.EMPTY;
        if(components.getFirst() instanceof dev.emi.emi.screen.tooltip.EmiTextTooltipWrapper wrapper && !wrapper.stack.getEmiStacks().isEmpty())
            tooltipStack=wrapper.stack.getEmiStacks().getFirst().getItemStack();
        dev.emi.emi.screen.tooltip.NativeTooltipQueue.queue(context.raw(),CLIENT.font,mutable,x,y,positioner,tooltipStack);
	}

	public static void drawSlotHightlight(EmiDrawContext context, int x, int y, int w, int h) {
		dev.emi.emi.runtime.NativeGuiComposition.highlight(context.raw(), x, y, w, h);
	}

	public static Component getAmountText(EmiIngredient stack) {
		return getAmountText(stack, stack.getAmount());
	}

	public static Component getAmountText(EmiIngredient stack, long amount) {
		if (stack.isEmpty() || amount == 0) {
			return EMPTY_TEXT;
		}
		if (stack.getEmiStacks().get(0).getKey() instanceof Fluid) {
			return getFluidAmount(amount);
		}
		return EmiPort.literal(TEXT_FORMAT.format(amount));
	}

	public static Component getAmountText(EmiIngredient stack, double amount) {
		if (stack.isEmpty() || amount == 0) {
			return EMPTY_TEXT;
		}
		if (stack.getEmiStacks().get(0).getKey() instanceof Fluid) {
			return EmiConfig.fluidUnit.translate(amount);
		}
		return EmiPort.literal(TEXT_FORMAT.format(amount));
	}

	public static Component getFluidAmount(long amount) {
		return EmiConfig.fluidUnit.translate(amount);
	}

	public static int getAmountOverflow(Component amount) {
		int width = CLIENT.font.width(amount);
		if (width > 14) {
			return width - 14;
		} else {
			return 0;
		}
	}

	public static void renderAmount(EmiDrawContext context, int x, int y, Component amount) {
		dev.emi.emi.screen.NativeIngredientOverlay.amount(context.raw(), x, y, amount);
	}

	public static void renderIngredient(EmiIngredient ingredient, EmiDrawContext context, int x, int y) {
		dev.emi.emi.screen.NativeIngredientOverlay.badge(context.raw(), x, y, 8);
	}

	public static void renderTag(EmiIngredient ingredient, EmiDrawContext context, int x, int y) {
		if (ingredient.getEmiStacks().size() > 1) {
			dev.emi.emi.screen.NativeIngredientOverlay.badge(context.raw(), x, y + 12, 0);
		}
	}

	public static void renderRemainder(EmiIngredient ingredient, EmiDrawContext context, int x, int y) {
		for (EmiStack stack : ingredient.getEmiStacks()) {
			EmiStack remainder = stack.getRemainder();
			if (!remainder.isEmpty()) {
				if (remainder.equals(ingredient)) {
					renderCatalyst(ingredient, context, x, y);
				} else {
					dev.emi.emi.screen.NativeIngredientOverlay.badge(context.raw(), x + 12, y, 4);
				}
				return;
			}
		}
	}

	public static void renderCatalyst(EmiIngredient ingredient, EmiDrawContext context, int x, int y) {
		dev.emi.emi.screen.NativeIngredientOverlay.badge(context.raw(), x + 12, y, 12);
	}

	public static void renderRecipeFavorite(EmiIngredient ingredient, EmiDrawContext context, int x, int y) {
		dev.emi.emi.screen.NativeIngredientOverlay.badge(context.raw(), x + 12, y, 16);
	}

	public static void renderRecipeBackground(EmiRecipe recipe, EmiDrawContext context, int x, int y) {
		context.resetColor();
		EmiRenderHelper.drawNinePatch(context, BACKGROUND, x, y, recipe.getDisplayWidth() + 8, recipe.getDisplayHeight() + 8, 27, 0, 4, 1);
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	public static void renderRecipe(EmiRecipe recipe, EmiDrawContext context, int x, int y, boolean showMissing, int overlayColor) {
		try {
			renderRecipeBackground(recipe, context, x, y);

			List<Widget> widgets = Lists.newArrayList();
			WidgetHolder holder = new WidgetHolder() {

				public int getWidth() {
					return recipe.getDisplayWidth();
				}

				public int getHeight() {
					return recipe.getDisplayHeight();
				}

				public <T extends Widget> T add(T widget) {
					widgets.add(widget);
					return widget;
				}
			};

			context.push();
			context.matrices().translate(x + 4, y + 4);

			recipe.addWidgets(holder);
			float delta = Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(false);
			for (Widget widget : widgets) {
				widget.extractRenderState(context.raw(), -1000, -1000, delta);
			}
			if (overlayColor != -1) {
				context.fill(-1, -1, recipe.getDisplayWidth() + 2, recipe.getDisplayHeight() + 2, overlayColor);
			}

			if (showMissing) {
				AbstractContainerScreen hs = EmiApi.getHandledScreen();
				EmiRecipeHandler handler = EmiRecipeFiller.getFirstValidHandler(recipe, hs);
				if (handler != null) {
					handler.render(recipe, new EmiCraftContext(hs, handler.getInventory(hs), EmiCraftContext.Type.FILL_BUTTON), widgets, context.raw());
				} else if (EmiScreenManager.lastPlayerInventory != null) {
					StandardRecipeHandler.renderMissing(recipe, EmiScreenManager.lastPlayerInventory, widgets, context.raw());
				}
			}

			context.pop();

			dev.emi.emi.runtime.NativeGuiComposition.alphaMask(context.raw(),
				() -> renderRecipeBackground(recipe, context, x, y));
		} catch (Throwable e) {
			EmiLog.error("Error rendering recipe", e);
		}
	}
}
