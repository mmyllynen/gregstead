package dev.emi.emi.runtime;

import org.joml.Matrix3x2fStack;
import dev.emi.emi.EmiPort;
import dev.emi.emi.api.stack.EmiIngredient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;

public class EmiDrawContext {
	private final Minecraft client = Minecraft.getInstance();
	private final GuiGraphicsExtractor context;
	
	private EmiDrawContext(GuiGraphicsExtractor context) {
		this.context = context;
	}

	public static EmiDrawContext wrap(GuiGraphicsExtractor context) {
		return new EmiDrawContext(context);
	}

	public GuiGraphicsExtractor raw() {
		return context;
	}

	public Matrix3x2fStack matrices() {
		return context.pose();
	}

	public void push() {
		matrices().pushMatrix();
	}

	public void pop() {
		matrices().popMatrix();
	}

	public void drawTexture(Identifier texture, int x, int y, int u, int v, int width, int height) {
		drawTexture(texture, x, y, width, height, u, v, width, height, 256, 256);
	}

	public void drawTexture(Identifier texture, int x, int y, float u, float v, int width, int height) {
		drawTexture(texture, x, y, u, v, width, height, 256, 256);
	}

	public void drawTexture(Identifier texture, int x, int y, float u, float v, int width, int height, int textureWidth, int textureHeight) {
		context.nextStratum();
		drawTexture(texture, x, y, width, height, u, v, width, height, textureWidth, textureHeight);
	}

	public void drawTexture(Identifier texture, int x, int y, int width, int height, float u, float v, int regionWidth, int regionHeight, int textureWidth, int textureHeight) {
		NativeGuiTint.texture(context, texture, x, y, width, height, u, v,
			regionWidth, regionHeight, textureWidth, textureHeight);
	}

	public void fill(int x, int y, int width, int height, int color) {
		context.fill(x, y, x + width, y + height, color);
	}

	private static int nativeTextColor(int color) {
		return (color & 0xff000000) == 0 ? color | 0xff000000 : color;
	}

	public void drawText(Component text, int x, int y) {
		drawText(text, x, y, -1);
	}

	public void drawText(Component text, int x, int y, int color) {
		context.text(client.font, text, x, y, nativeTextColor(color), false);
	}

	public void drawText(FormattedCharSequence text, int x, int y, int color) {
		context.text(client.font, text, x, y, nativeTextColor(color), false);
	}

	public void drawTextWithShadow(Component text, int x, int y) {
		drawTextWithShadow(text, x, y, -1);
	}

	public void drawTextWithShadow(Component text, int x, int y, int color) {
		context.text(client.font, text, x, y, nativeTextColor(color), true);
	}

	public void drawTextWithShadow(FormattedCharSequence text, int x, int y, int color) {
		context.text(client.font, text, x, y, nativeTextColor(color), true);
	}

	public void drawCenteredText(Component text, int x, int y) {
		drawCenteredText(text, x, y, -1);
	}

	public void drawCenteredText(Component text, int x, int y, int color) {
		context.text(client.font, text, x - client.font.width(text) / 2, y, nativeTextColor(color), false);
	}

	public void drawCenteredTextWithShadow(Component text, int x, int y) {
		drawCenteredTextWithShadow(text, x, y, -1);
	}

	public void drawCenteredTextWithShadow(Component text, int x, int y, int color) {
		context.centeredText(client.font, text.getVisualOrderText(), x, y, nativeTextColor(color));
	}

	public void resetColor() {
		setColor(1f, 1f, 1f, 1f);
	}

	public void setColor(float r, float g, float b) {
		setColor(r, g, b, 1f);
	}

	public void setColor(float r, float g, float b, float a) {
		NativeGuiTint.set(raw(), r, g, b, a);
	}

	public void drawStack(EmiIngredient stack, int x, int y) {
		NativeGuiTint.extract(raw(), () -> stack.render(raw(), x, y, client.getDeltaTracker().getGameTimeDeltaPartialTick(false)));
	}

	public void drawStack(EmiIngredient stack, int x, int y, int flags) {
		drawStack(stack, x, y, client.getDeltaTracker().getGameTimeDeltaPartialTick(false), flags);
	}

	public void drawStack(EmiIngredient stack, int x, int y, float delta, int flags) {
		NativeGuiTint.extract(raw(), () -> stack.render(raw(), x, y, delta, flags));
	}
}
