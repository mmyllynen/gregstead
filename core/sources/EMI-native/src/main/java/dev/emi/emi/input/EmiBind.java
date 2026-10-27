package dev.emi.emi.input;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.InputQuirks;

public class EmiBind {
	public static final EmiBind LEFT_CLICK = new EmiBind("", new EmiBind.ModifiedKey(InputConstants.Type.MOUSE.getOrCreate(InputConstants.MOUSE_BUTTON_LEFT), 0));
	public static final int MAX_BINDS = 4;
	public final String translationKey;
	public final List<ModifiedKey> defaultKeys;
	public List<ModifiedKey> boundKeys;
	
	public EmiBind(String translationKey, int code) {
		this(translationKey, 0, code);
	}
	
	public EmiBind(String translationKey, int modifiers, int code) {
		this(translationKey, ModifiedKey.of(code, modifiers));
	}

	public EmiBind(String translationKey, ModifiedKey... defaultKeys) {
		this.translationKey = translationKey;
		this.defaultKeys = Arrays.asList(defaultKeys);
		this.boundKeys = this.defaultKeys.stream().collect(Collectors.toCollection(ArrayList::new));
		updateBinds();
	}

	public void updateBinds() {
		if (boundKeys.size() == 0) {
			boundKeys.add(new ModifiedKey(InputConstants.UNKNOWN, 0));
		}
		for (int i = 0; i < boundKeys.size() - 1; i++) {
			if (boundKeys.get(i).isUnbound()) {
				boundKeys.remove(i);
				i--;
			}
		}
		if (!boundKeys.get(boundKeys.size() - 1).isUnbound() && boundKeys.size() < MAX_BINDS) {
			boundKeys.add(new ModifiedKey(InputConstants.UNKNOWN, 0));
		}
	}

	public boolean isBound() {
		return boundKeys.size() > 0 && !boundKeys.get(0).isUnbound();
	}

	public Component getBindText() {
		if (!isBound()) {
			return Component.literal("[]").withStyle(ChatFormatting.GOLD);
		} else {
			ModifiedKey bind = boundKeys.get(0);
			for (ModifiedKey key : boundKeys) {
				if (key.key.getType() == InputConstants.Type.MOUSE) {
					bind = key;
					break;
				}
			}
			return Component.literal("[").withStyle(ChatFormatting.GOLD)
				.append(bind.getKeyText(ChatFormatting.GOLD))
				.append(Component.literal("]").withStyle(ChatFormatting.GOLD));
		}
	}

	public void setToDefault() {
		this.boundKeys = this.defaultKeys.stream().collect(Collectors.toCollection(ArrayList::new));
		updateBinds();
	}

	public void setBinds(ModifiedKey... keys) {
		this.boundKeys = Stream.of(keys).collect(Collectors.toCollection(ArrayList::new));
		updateBinds();
	}

	public void setBind(int offset, ModifiedKey key) {
		if (offset < boundKeys.size() && offset >= 0) {
			boundKeys.set(offset, key);
		}
		updateBinds();
	}

	public boolean isHeld() {
		for (ModifiedKey boundKey : boundKeys) {
			if (EmiInput.getCurrentModifiers() == boundKey.modifiersToMatch()) {
				if (boundKey.key.getType() == InputConstants.Type.KEYBOARD && !boundKey.isUnbound()) {
					if (InputConstants.isKeyDown(boundKey.key.getValue())) {
						return true;
					}
				}
			}
		}
		return false;
	}

	/** Scalar callers use the native physical scancode as the first key field. */
	public boolean matchesKey(int scancode, int keycode) {
		return matchesKeyboard(scancode, EmiInput.getCurrentModifiers());
	}

	public boolean matchesKey(KeyEvent event) {
		return matchesKeyboard(event.key(), EmiInput.modifiers(event.modifiers(), InputQuirks.ON_OSX));
	}

	private boolean matchesKeyboard(int scancode, int modifiers) {
		for (ModifiedKey boundKey : boundKeys) {
			if (!boundKey.isUnbound() && boundKey.key.getType() == InputConstants.Type.KEYBOARD
					&& boundKey.key.getValue() == scancode && modifiers == boundKey.modifiersToMatch()) return true;
		}
		return false;
	}

	/** Retain the old widget mouse numbering at this API boundary. */
	public boolean matchesMouse(int widgetButton) {
		int nativeButton = switch (widgetButton) {
			case 0 -> InputConstants.MOUSE_BUTTON_LEFT;
			case 1 -> InputConstants.MOUSE_BUTTON_RIGHT;
			case 2 -> InputConstants.MOUSE_BUTTON_MIDDLE;
			default -> widgetButton + 1;
		};
		return matchesNativeMouse(nativeButton, EmiInput.getCurrentModifiers());
	}

	public boolean matchesMouse(MouseButtonEvent event) {
		return matchesNativeMouse(event.button(), EmiInput.modifiers(event.modifiers(), InputQuirks.ON_OSX));
	}

	private boolean matchesNativeMouse(int button, int modifiers) {
		for (ModifiedKey boundKey : boundKeys) {
			if (boundKey.key.getType() == InputConstants.Type.MOUSE && boundKey.key.getValue() == button
					&& modifiers == boundKey.modifiersToMatch()) return true;
		}
		return false;
	}

	public void setKey(List<String> keys) {
		List<ModifiedKey> modifiedKeys = Lists.newArrayList();
		for (String string : keys) {
			String[] parts = string.split(" ");
			InputConstants.Key key = InputConstants.UNKNOWN;
			int modifiers = 0;
			if (parts.length > 0) {
				key = InputConstants.getKey(parts[parts.length - 1]);
				for (int i = 0; i < parts.length - 1; i++) {
					if (parts[i].equals("ctrl") || parts[i].equals("control")) {
						modifiers |= EmiInput.CONTROL_MASK;
					} else if (parts[i].equals("alt")) {
						modifiers |= EmiInput.ALT_MASK;
					} else if (parts[i].equals("shift")) {
						modifiers |= EmiInput.SHIFT_MASK;
					}
				}
			}
			modifiedKeys.add(new ModifiedKey(key, modifiers));
		}
		this.boundKeys = modifiedKeys;
		updateBinds();
	}

	public static record ModifiedKey(InputConstants.Key key, int modifiers) {

		public static ModifiedKey of(int code, int modifiers) {
			return new ModifiedKey(InputConstants.Type.KEYBOARD.getOrCreate(code), modifiers);
		}

		public String toName() {
			String name = "";
			if ((modifiers & EmiInput.CONTROL_MASK) > 0) {
				name += "ctrl ";
			}
			if ((modifiers & EmiInput.ALT_MASK) > 0) {
				name += "alt ";
			}
			if ((modifiers & EmiInput.SHIFT_MASK) > 0) {
				name += "shift ";
			}
			name += key.getName();
			return name;
		}

		public int modifiersToMatch() {
			int modifiers = this.modifiers;
			if (key.getType() == InputConstants.Type.KEYBOARD) {
				modifiers ^= EmiInput.maskFromCode(key.getValue());
			}
			return modifiers;
		}

		public boolean isUnbound() {
			return key == InputConstants.UNKNOWN;
		}

		public MutableComponent getKeyText(ChatFormatting formatting) {
			MutableComponent text = Component.literal("").withStyle(formatting);
			appendModifiers(text, modifiers());
			text.append(key().getDisplayName());
			return text;
		}
	
		private void appendModifiers(MutableComponent text, int modifiers) {
			if ((modifiers & EmiInput.CONTROL_MASK) > 0) {
				text.append(Component.translatable("key.keyboard.control"));
				text.append(Component.literal(" + "));
			}
			if ((modifiers & EmiInput.ALT_MASK) > 0) {
				text.append(Component.translatable("key.keyboard.alt"));
				text.append(Component.literal(" + "));
			}
			if ((modifiers & EmiInput.SHIFT_MASK) > 0) {
				text.append(Component.translatable("key.keyboard.shift"));
				text.append(Component.literal(" + "));
			}
		}
	}
}
