package dev.emi.emi.screen.widget.config;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.AbstractContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Lists;

/**
 * Shamelessly modified vanilla lists to support variable width.
 * This is the lesser of two evils, at least this way I have vanilla compat.
 */
public class ListWidget extends AbstractContainerEventHandler implements Renderable, NarratableEntry {
	private static final Identifier MENU_LIST_BACKGROUND_TEXTURE = Identifier.withDefaultNamespace("textures/gui/menu_list_background.png");
	private static final Identifier INWORLD_MENU_LIST_BACKGROUND_TEXTURE = Identifier.withDefaultNamespace("textures/gui/inworld_menu_list_background.png");

	protected final Minecraft client;
	private final List<Entry> children = Lists.newArrayList();
	protected int width;
	protected int height;
	protected int top;
	protected int bottom;
	protected int right;
	protected int left;
	private double scrollAmount;
	private boolean renderSelection = true;
	private boolean scrolling;
	private Entry selected;
	private Entry hoveredEntry;
	public int padding = 4;

	public ListWidget(Minecraft client, int width, int height, int top, int bottom) {
		this.client = client;
		this.width = width;
		this.height = height;
		this.top = top;
		this.bottom = bottom;
		this.left = 0;
		this.right = width;
	}

	public void setRenderSelection(boolean renderSelection) {
		this.renderSelection = renderSelection;
	}

	public int getRowWidth() {
		return Math.min(400, width - 60);
	}

	public int getLogicalHeight() {
		return bottom - top;
	}

	@Nullable
	public Entry getSelectedOrNull() {
		return this.selected;
	}

	public void setSelected(@Nullable Entry entry) {
		this.selected = entry;
	}

	public final List<Entry> children() {
		return this.children;
	}

	protected final void clearEntries() {
		this.children.clear();
	}

	protected void replaceEntries(Collection<Entry> newEntries) {
		this.children.clear();
		this.children.addAll(newEntries);
	}

	protected Entry getEntry(int index) {
		return this.children().get(index);
	}

	public int addEntry(Entry entry) {
		this.children.add(entry);
		entry.parentList = this;
		return this.children.size() - 1;
	}

	protected int getEntryCount() {
		return this.children().size();
	}

	protected boolean isSelectedEntry(int index) {
		return Objects.equals(this.getSelectedOrNull(), this.children().get(index));
	}

	@Nullable
	protected final Entry getEntryAtPosition(double x, double y) {
		int rowWidth = this.getRowWidth() / 2;
		int mid = this.left + this.width / 2;
		int rowLeft = mid - rowWidth;
		int rowRight = mid + rowWidth;
		int m = Mth.floor(y - (double)this.top) + (int)this.getScrollAmount() - 4;
		if (x < this.getScrollbarPositionX() && x >= rowLeft && x <= rowRight && m >= 0) {
			int h = 0;
			for (int i = 0; i < this.getEntryCount(); i++) {
				int eh = getEntryHeight(i);
				if (m >= h && m < h + eh - padding) {
					return this.getEntry(i);
				}
				h += eh;
			}
		}
		return null;
	}

	public void updateSize(int width, int height, int top, int bottom) {
		this.width = width;
		this.height = height;
		this.top = top;
		this.bottom = bottom;
		this.left = 0;
		this.right = width;
	}

	public void setLeftPos(int left) {
		this.left = left;
		this.right = left + this.width;
	}

	protected int getMaxPosition() {
		return this.getTotalHeight();
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor draw, int mouseX, int mouseY, float delta) {
		int i = getScrollbarPositionX(), j = i + 6;
		hoveredEntry = isMouseOver(mouseX, mouseY) ? getEntryAtPosition(mouseX, mouseY) : null;
		var pipeline = net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED;
		Identifier background = client.level == null ? MENU_LIST_BACKGROUND_TEXTURE : INWORLD_MENU_LIST_BACKGROUND_TEXTURE;
		draw.blit(pipeline, background, left, top, right, bottom + (int) scrollAmount, right - left, bottom - top, 32, 32);
		draw.enableScissor(left, top, right, bottom);
		try { renderList(draw, getRowLeft(), top + 4 - (int) getScrollAmount(), mouseX, mouseY, delta); }
		finally { draw.disableScissor(); }
		Identifier header = client.level == null ? Screen.HEADER_SEPARATOR : Screen.INWORLD_HEADER_SEPARATOR;
		Identifier footer = client.level == null ? Screen.FOOTER_SEPARATOR : Screen.INWORLD_FOOTER_SEPARATOR;
		draw.blit(pipeline, header, left, top - 2, 0, 0, width, 2, 32, 2);
		draw.blit(pipeline, footer, left, bottom, 0, 0, width, 2, 32, 2);
		int max = getMaxScroll();
		if (max > 0) {
			int bar = Mth.clamp((int) ((float) ((bottom - top) * (bottom - top)) / getMaxPosition()), 32, bottom - top - 8);
			int y = Math.max(top, (int) getScrollAmount() * (bottom - top - bar) / max + top);
			draw.fill(i, top, j, bottom, 0xff000000);
			draw.fill(i, y, j, y + bar, 0xff808080);
			draw.fill(i, y, j - 1, y + bar - 1, 0xffc0c0c0);
		}
	}

	public void centerScrollOn(Entry entry) {
		int i = 0;
		for (Entry e : this.children()) {
			if (e == entry) {
				this.setScrollAmount(i - 42);
				return;
			}
			i += getEntryHeight(e);
		}
	}

	protected void ensureVisible(Entry entry) {
		int i = this.getRowTop(this.children().indexOf(entry));
		int j = i - this.top - 4 - entry.getHeight();
		int k = this.bottom - i - entry.getHeight() * 2;
		if (j < 0) {
			this.scroll(j);
		}
		if (k < 0) {
			this.scroll(-k);
		}
	}

	private void scroll(int amount) {
		this.setScrollAmount(this.getScrollAmount() + (double)amount);
	}

	public double getScrollAmount() {
		return this.scrollAmount;
	}

	public void setScrollAmount(double amount) {
		this.scrollAmount = Mth.clamp(amount, 0.0, (double)this.getMaxScroll());
	}

	public int getMaxScroll() {
		return Math.max(0, this.getMaxPosition() - (this.bottom - this.top - 4) + 40);
	}

	protected void updateScrollingState(double mouseX, double mouseY, int button) {
		this.scrolling = button == com.mojang.blaze3d.platform.InputConstants.MOUSE_BUTTON_LEFT && mouseX >= (double)this.getScrollbarPositionX() && mouseX < (double)(this.getScrollbarPositionX() + 6);
	}

	protected int getScrollbarPositionX() {
		return this.width - 6;
	}

	public void unfocusTextField() {
		for (Entry e : this.children) {
			for (GuiEventListener el : e.children()) {
				if (el instanceof EditBox tfw) {
					dev.emi.emi.input.EmiInput.focus(tfw, false);
				}
			}
		}
	}

	public EditBox getFocusedTextField() {
		for (Entry e : this.children) {
			for (GuiEventListener el : e.children()) {
				if (el instanceof EditBox tfw) {
					if (tfw.isFocused()) {
						return tfw;
					}
				}
			}
		}
		return null;
	}

	@Override
	public boolean mouseClicked(net.minecraft.client.input.MouseButtonEvent event, boolean doubleClick) {
		updateScrollingState(event.x(), event.y(), event.button());
		unfocusTextField();
		if (!isMouseOver(event.x(), event.y())) return false;
		Entry entry = getEntryAtPosition(event.x(), event.y());
		if (entry != null && entry.mouseClicked(event, doubleClick)) {
			setFocused(entry); setDragging(true); return true;
		}
		return scrolling;
	}
	@Override
	public boolean mouseReleased(net.minecraft.client.input.MouseButtonEvent event) {
		if (getFocused() != null) getFocused().mouseReleased(event);
		setDragging(false); scrolling = false;
		return false;
	}
	@Override
	public boolean mouseDragged(net.minecraft.client.input.MouseButtonEvent event, double deltaX, double deltaY) {
		if (super.mouseDragged(event, deltaX, deltaY)) return true;
		if (event.button() != com.mojang.blaze3d.platform.InputConstants.MOUSE_BUTTON_LEFT || !scrolling) return false;
		if (event.y() < top) setScrollAmount(0);
		else if (event.y() > bottom) setScrollAmount(getMaxScroll());
		else {
			double max = Math.max(1, getMaxScroll());
			int height = bottom - top;
			int bar = Mth.clamp((int) ((float) (height * height) / getMaxPosition()), 32, height - 8);
			setScrollAmount(getScrollAmount() + deltaY * Math.max(1, max / (height - bar)));
		}
		return true;
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double horizontal, double amount) {
		this.setScrollAmount(this.getScrollAmount() - amount * 22);
		return true;
	}

	@Override
	public boolean keyPressed(net.minecraft.client.input.KeyEvent event) {
		return super.keyPressed(event);
	}

	protected void moveSelection(MoveDirection direction) {
		this.moveSelectionIf(direction, entry -> true);
	}

	protected void ensureSelectedEntryVisible() {
		Entry entry = this.getSelectedOrNull();
		if (entry != null) {
			this.setSelected(entry);
			this.ensureVisible(entry);
		}
	}

	/**
	 * Moves the selection in the specified direction until the predicate returns true.
	 * 
	 * @param direction the direction to move the selection
	 */
	protected void moveSelectionIf(MoveDirection direction, Predicate<Entry> predicate) {
		int i = direction == MoveDirection.UP ? -1 : 1;
		if (!this.children().isEmpty()) {
			int k;
			int j = this.children().indexOf(this.getSelectedOrNull());
			while (j != (k = Mth.clamp(j + i, 0, this.getEntryCount() - 1))) {
				Entry entry = (Entry)this.children().get(k);
				if (predicate.test(entry)) {
					this.setSelected(entry);
					this.ensureVisible(entry);
					break;
				}
				j = k;
			}
		}
	}

	@Override
	public boolean isMouseOver(double mouseX, double mouseY) {
		return mouseY >= (double)this.top && mouseY <= (double)this.bottom && mouseX >= (double)this.left && mouseX <= (double)this.right;
	}

	protected void renderList(GuiGraphicsExtractor draw, int x, int y, int mouseX, int mouseY, float delta) {
		for (int index = 0; index < getEntryCount(); index++) {
			int rowTop = getRowTop(index), rowBottom = getRowBottom(index);
			if (rowBottom < top || rowTop > bottom) continue;
			int height = getEntryHeight(index);
			if (height == 0) continue;
			height -= 4;
			Entry entry = getEntry(index);
			int rowWidth = getRowWidth();
			if (renderSelection && isSelectedEntry(index)) {
				int x0 = left + width / 2 - rowWidth / 2, x1 = left + width / 2 + rowWidth / 2;
				draw.fill(x0, rowTop - 2, x1, rowTop + height + 2, isFocused() ? 0xffffffff : 0xff808080);
				draw.fill(x0 + 1, rowTop - 1, x1 - 1, rowTop + height + 1, 0xff000000);
			}
			entry.render(draw, index, rowTop, getRowLeft(), rowWidth - 3, height, mouseX, mouseY, Objects.equals(hoveredEntry, entry), delta);
		}
	}

	public int getRowLeft() {
		return this.left + this.width / 2 - this.getRowWidth() / 2 + 2;
	}

	public int getRowRight() {
		return this.getRowLeft() + this.getRowWidth();
	}

	private int getEntryHeight(int i) {
		return getEntryHeight(this.getEntry(i));
	}

	private int getEntryHeight(Entry entry) {
		int h = entry.getHeight();
		if (h == 0) {
			return 0;
		}
		return h + padding;
	}

	protected int getRowTop(int index) {
		int height = 0;
		for (int i = 0; i < index; i++) {
			height += getEntryHeight(i);
		}
		return this.top + 4 - (int)this.getScrollAmount() + height;
	}

	private int getRowBottom(int index) {
		return this.getRowTop(index) + this.getEntry(index).getHeight();
	}

	public boolean isFocused() {
		return false;
	}

	@Override
	public NarratableEntry.NarrationPriority narrationPriority() {
		if (this.isFocused()) {
			return NarratableEntry.NarrationPriority.FOCUSED;
		}
		if (this.hoveredEntry != null) {
			return NarratableEntry.NarrationPriority.HOVERED;
		}
		return NarratableEntry.NarrationPriority.NONE;
	}

	@Nullable
	public Entry getHoveredEntry() {
		return this.hoveredEntry;
	}

	void setEntryParentList(Entry entry) {
		entry.parentList = this;
	}

	protected void appendNarrations(NarrationElementOutput builder, Entry entry) {
		int i;
		List<Entry> list = this.children();
		if (list.size() > 1 && (i = list.indexOf(entry)) != -1) {
			builder.add(NarratedElementType.POSITION, (Component)Component.translatable("narrator.position.list", i + 1, list.size()));
		}
	}

	public int getTotalHeight() {
		int height = 0;
		for (int i = 0; i < this.getEntryCount(); i++) {
			height += getEntryHeight(i);
		}
		if (height > 0) {
			height -= padding;
		}
		return height;
	}

	@Override
	public void updateNarration(NarrationElementOutput var1) {
	}

	public static abstract class Entry extends AbstractContainerEventHandler {
		public ListWidget parentList;

		public abstract void render(GuiGraphicsExtractor draw, int index, int y, int x, int width, int height, int mouseX, int mouseY,
			boolean hovered, float delta);

		@Override
		public boolean isMouseOver(double mouseX, double mouseY) {
			return Objects.equals(this.parentList.getEntryAtPosition(mouseX, mouseY), this);
		}

		public List<ClientTooltipComponent> getTooltip(int mouseX, int mouseY) {
			return List.of();
		}

		public abstract int getHeight();
	}

	protected static enum MoveDirection {
		UP,
		DOWN;

	}
}