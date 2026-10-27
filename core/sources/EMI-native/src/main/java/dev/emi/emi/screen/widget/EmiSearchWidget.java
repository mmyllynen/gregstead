package dev.emi.emi.screen.widget;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Style;
import net.minecraft.util.Mth;
import com.mojang.datafixers.util.Pair;

import com.google.common.collect.Lists;
import dev.emi.emi.EmiPort;
import dev.emi.emi.config.EmiConfig;
import dev.emi.emi.screen.EmiScreenManager;
import dev.emi.emi.search.EmiSearch;
import dev.emi.emi.search.QueryType;

public class EmiSearchWidget extends EditBox {
	private static final Pattern ESCAPE = Pattern.compile("\\\\.");
	private List<String> searchHistory = Lists.newArrayList();
	private int searchHistoryIndex = 0;
	private List<Pair<Integer, Style>> styles = List.of();
	private long lastClick = 0;
	private String last = "";
	private long lastRender = System.currentTimeMillis();
	private long accumulatedSpin = 0;
	public boolean highlight = false;
	// Reimplement focus because other mods keep breaking it
	public boolean isFocused;

	public EmiSearchWidget(Font textRenderer, int x, int y, int width, int height) {
		super(textRenderer, x, y, width, height, EmiPort.literal(""));
		this.setCanLoseFocus(true);
		this.setTextColor(-1);
		this.setTextColorUneditable(-1);
		this.setMaxLength(256);
		this.addFormatter((string, offset) -> NativeSearchText.format(string, offset, styles));
		this.setResponder(string -> {
			if (string.isEmpty()) {
				this.setSuggestion(I18n.get("emi.search"));
			} else {
				this.setSuggestion("");
			}
			EmiScreenManager.updateSearchSidebar();
			Matcher matcher = EmiSearch.TOKENS.matcher(string);
			List<Pair<Integer, Style>> styles = Lists.newArrayList();
			int last = 0;
			while (matcher.find()) {
				int start = matcher.start();
				int end = matcher.end();
				if (last < start) {
					styles.add(Pair.of(start, Style.EMPTY.applyFormat(ChatFormatting.WHITE)));
				}
				String group = matcher.group();
				if (group.startsWith("-")) {
					styles.add(Pair.of(start + 1, Style.EMPTY.applyFormat(ChatFormatting.RED)));
					start++;
					group = group.substring(1);
				}
				QueryType type = QueryType.fromString(group);
				int subStart = type.prefix.length();
				if (group.length() > 1 + subStart && group.substring(subStart).startsWith("/") && group.endsWith("/")) {
					int rOff = start + subStart + 1;
					styles.add(Pair.of(rOff, type.slashColor));
					Matcher rMatcher = ESCAPE.matcher(string.substring(rOff, end - 1));
					int rLast = 0;
					while (rMatcher.find()) {
						int rStart = rMatcher.start();
						int rEnd = rMatcher.end();
						if (rLast < rStart) {
							styles.add(Pair.of(rStart + rOff, type.regexColor));
						}
						styles.add(Pair.of(rEnd + rOff, type.escapeColor));
						rLast = rEnd;
					}
					if (rLast < end - 1) {
						styles.add(Pair.of(end - 1, type.regexColor));
					}
					styles.add(Pair.of(end, type.slashColor));
				} else {
					styles.add(Pair.of(end, type.color));
				}

				last = end;
			}
			if (last < string.length()) {
				styles.add(Pair.of(string.length(), Style.EMPTY.applyFormat(ChatFormatting.WHITE)));
			}
			this.styles = styles;
			EmiSearch.search(string);
		});
	}

	public void update() {
		setValue(getValue());
	}

	public void swap() {
		String last = this.getValue();
		this.setValue(this.last);
		this.last = last;
	}

	@Override
	public void setFocused(boolean focused) {
		if (focused == isFocused) {
			return;
		}
		if (!focused) {
			searchHistoryIndex = 0;
			String currentSearch = getValue();
			if (!currentSearch.isBlank() && !currentSearch.isEmpty()) {
				searchHistory.removeIf(String::isBlank);
				searchHistory.remove(currentSearch);
				searchHistory.add(0, currentSearch);
				if (searchHistory.size() > 36) {
					searchHistory.remove(searchHistory.size() - 1);
				}
			}
		}
		isFocused = focused;
		super.setFocused(focused);
		EmiScreenManager.updateSearchSidebar();
	}

	@Override
	public boolean isFocused() {
		return isFocused;
	}

	@Override
	public boolean mouseClicked(net.minecraft.client.input.MouseButtonEvent event, boolean doubleClick) {
        double mouseX = event.x(), mouseY = event.y();
        int button = event.button();
		if (!isMouseOver(mouseX, mouseY) || !EmiConfig.enabled) {
			EmiPort.focus(this, false);
			return false;
		} else {
			boolean b = super.mouseClicked(button == com.mojang.blaze3d.platform.InputConstants.MOUSE_BUTTON_RIGHT
                ? new net.minecraft.client.input.MouseButtonEvent(mouseX, mouseY, new net.minecraft.client.input.MouseButtonInfo(com.mojang.blaze3d.platform.InputConstants.MOUSE_BUTTON_LEFT, event.modifiers()))
                : event, doubleClick);
			if (isMouseOver(mouseX, mouseY)) {
				EmiPort.focus(this, true);
			}
			if (this.isFocused()) {
				if (button == com.mojang.blaze3d.platform.InputConstants.MOUSE_BUTTON_LEFT) {
					if (System.currentTimeMillis() - lastClick < 500) {
						highlight = !highlight;
						lastClick = 0;
					} else {
						lastClick = System.currentTimeMillis();
					}
				} else if (button == com.mojang.blaze3d.platform.InputConstants.MOUSE_BUTTON_RIGHT) {
					this.setValue("");
					EmiPort.focus(this, true);
				}
			}
			return b;
		}
	}

	@Override
	public boolean keyPressed(net.minecraft.client.input.KeyEvent event) {
		if (this.isFocused()) {
			if (EmiConfig.clearSearch.matchesKey(event)) {
				setValue("");
				return true;
			}
			if ((EmiConfig.focusSearch.matchesKey(event)
					|| event.key() == com.mojang.blaze3d.platform.InputConstants.KEY_RETURN || event.isEscape())) {
				EmiPort.focus(this, false);
				return true;
			}
			if (event.key() == com.mojang.blaze3d.platform.InputConstants.KEY_UP || event.key() == com.mojang.blaze3d.platform.InputConstants.KEY_DOWN) {
				int offset = event.key() == com.mojang.blaze3d.platform.InputConstants.KEY_UP ? 1 : -1;
				if (searchHistoryIndex + offset >= 0 && searchHistoryIndex + offset < searchHistory.size()) {
					if (searchHistoryIndex >= 0 && searchHistoryIndex < searchHistory.size()) {
						searchHistory.set(searchHistoryIndex, getValue());
					}
					searchHistoryIndex += offset;
					setValue(searchHistory.get(searchHistoryIndex));
				}
			}
		}
		return super.keyPressed(event);
	}

	@Override
    public void extractWidgetRenderState(GuiGraphicsExtractor raw, int mouseX, int mouseY, float delta) {
        this.setEditable(EmiConfig.enabled);
        String lower = getValue().toLowerCase(java.util.Locale.ROOT);
        boolean dinnerbone = lower.contains("dinnerbone");
        long now = System.currentTimeMillis();
        accumulatedSpin = Math.clamp(accumulatedSpin + (dinnerbone ? 1 : -1) * Math.abs(now - lastRender), 0, 500);
        lastRender = now;
        int tint = -1;
        if (lower.contains("jeb_")) {
            int amount = 0x3ff;
            float hue = ((lastRender & amount) % (float) amount) / amount;
            tint = 0xff000000 | Mth.hsvToRgb(hue, 1, 1);
        }
        if (EmiConfig.enabled) {
            NativeSearchEffect.extract(raw, getX(), getY(), getWidth(), getHeight(),
                (float) Math.toRadians(accumulatedSpin * 180 / 500), tint, highlight,
                () -> super.extractWidgetRenderState(raw, mouseX, mouseY, delta));
        }
    }
}
