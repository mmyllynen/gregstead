package dev.latvian.mods.kubejs.plugin.builtin.wrapper;

import dev.latvian.mods.kubejs.KubeJS;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;

public interface TextIcons {
	Identifier FONT = KubeJS.id("icons");
	Style STYLE = Style.EMPTY.withFont(new FontDescription.Resource(FONT)).applyFormat(ChatFormatting.WHITE);

	Component CRAFTING = Component.literal("A").setStyle(STYLE);
	Component LOGO = Component.literal("K").setStyle(STYLE);
	Component VSCODE = Component.literal("V").setStyle(STYLE);

	Component NAME = Component.empty().append(LOGO).append(smallSpace()).append(Component.literal(KubeJS.MOD_NAME));

	String ALL_ICONS = "AKV";

	static Component smallSpace() {
		return OptionalTextIcons.resolve("SMALL_SPACE", " ");
	}

	static Component error() {
		return OptionalTextIcons.resolve("ERROR", "[!]");
	}

	static Component plus() {
		return OptionalTextIcons.resolve("PLUS", "+");
	}

	static Component minus() {
		return OptionalTextIcons.resolve("MINUS", "-");
	}

	static Component tilde() {
		return OptionalTextIcons.resolve("TILDE", "~");
	}

	static Component crafting() {
		return CRAFTING;
	}

	static Component copy() {
		return OptionalTextIcons.resolve("COPY", "[copy]");
	}

	static Component id() {
		return OptionalTextIcons.resolve("ID", "[id]");
	}

	static Component info() {
		return OptionalTextIcons.resolve("INFO", "[i]");
	}

	static Component logo() {
		return LOGO;
	}

	static Component camera() {
		return OptionalTextIcons.resolve("CAMERA", "[camera]");
	}

	static Component no() {
		return OptionalTextIcons.resolve("NO", "[x]");
	}

	static Component prototypeComponent() {
		return OptionalTextIcons.resolve("PROTOTYPE_COMPONENT", "[base]");
	}

	static Component patchedComponent() {
		return OptionalTextIcons.resolve("PATCHED_COMPONENT", "[patch]");
	}

	static Component fire() {
		return OptionalTextIcons.resolve("FIRE", "[fire]");
	}

	static Component tag() {
		return OptionalTextIcons.resolve("TAG", "#");
	}

	static Component vscode() {
		return VSCODE;
	}

	static Component warn() {
		return OptionalTextIcons.resolve("WARN", "[!]");
	}

	static Component yes() {
		return OptionalTextIcons.resolve("YES", "[ok]");
	}

	static Component yes(boolean yes) {
		return yes ? OptionalTextIcons.resolve("YES", "[ok]") : OptionalTextIcons.resolve("NO", "[x]");
	}
}
