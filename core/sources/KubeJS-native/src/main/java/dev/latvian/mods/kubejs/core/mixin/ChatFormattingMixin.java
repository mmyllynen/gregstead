package dev.latvian.mods.kubejs.core.mixin;

import dev.latvian.mods.kubejs.color.KubeColor;
import net.minecraft.ChatFormatting;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ChatFormatting.class)
public abstract class ChatFormattingMixin implements KubeColor {

	@Override
	public int kjs$getARGB() {
		return 0xFF000000 | kjs$getRGB();
	}

	@Override
	public int kjs$getRGB() {
		var color = net.minecraft.network.chat.TextColor.fromLegacyFormat((ChatFormatting) (Object) this);
		return color == null ? 0 : color.getValue();
	}
}
