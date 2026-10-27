package dev.emi.emi.jemi.impl.extras;

import java.util.List;
import net.minecraft.network.chat.FormattedText;

/** Native JEI text widget owns font, wrapping, alignment, truncation and rich tooltips. */
public final class JemiTextWidget extends mezz.jei.common.gui.elements.TextWidget {
    public JemiTextWidget(List<FormattedText> text, int width, int height) {
        super(List.copyOf(text), 0, 0, width, height);
    }
}
