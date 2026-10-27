package dev.emi.emi.screen.widget.config;

import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import dev.emi.emi.EmiPort;
import dev.emi.emi.input.EmiInput;
import dev.emi.emi.screen.widget.SizedButtonWidget;

public class IntEdit {
	public final EditBox text;
	public final Button up, down;
	
	public IntEdit(int width, IntSupplier getter, IntConsumer setter) {
		Minecraft client = Minecraft.getInstance();
		text = new NumericEditBox(client.font, 0, 0, width - 14, 18, EmiPort.literal(""));
		text.setValue("" + getter.getAsInt());
		text.setResponder(string -> {
			try {
				if (string.isBlank()) {
					setter.accept(0);
				} else {
					setter.accept(Integer.parseInt(string));
				}
			} catch (Exception e) {
			}
		});

		up = new SizedButtonWidget(150, 0, 12, 10, 232, 48, () -> true, button -> {
			setter.accept(getter.getAsInt() + getInc());
			text.setValue("" + getter.getAsInt());
		});
		down = new SizedButtonWidget(150, 10, 12, 10, 244, 48, () -> true, button -> {
			setter.accept(getter.getAsInt() - getInc());
			text.setValue("" + getter.getAsInt());
		});
	}

	public boolean contains(int x, int y) {
		return x > text.getX() && x < up.getX() + up.getWidth() && y > text.getY() && y < text.getY() + text.getHeight();
	}

	public int getInc() {
		if (EmiInput.isShiftDown()) {
			return 10;
		} else if (EmiInput.isControlDown()) {
			return 5;
		}
		return 1;
	}

	public void setPosition(int x, int y) {
		text.setX(x + 1);
		text.setY(y + 1);
		up.setX(x + text.getWidth() + 2);
		up.setY(y);
		down.setX(up.getX());
		down.setY(y + 10);
	}
}
