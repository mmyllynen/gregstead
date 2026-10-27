package dev.emi.emi.jemi.impl;

import com.mojang.datafixers.util.Either;
import dev.emi.emi.runtime.EmiLog;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.runtime.IJeiKeyMapping;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.world.inventory.tooltip.TooltipComponent;

public class JemiTooltipBuilder implements ITooltipBuilder {
    private final List<Either<FormattedText, TooltipComponent>> lines = new ArrayList<>();
    private ITypedIngredient<?> ingredient;

    @Override public void add(FormattedText text) { lines.add(Either.left(text)); }
    @Override public void addAll(Collection<? extends FormattedText> texts) { texts.forEach(this::add); }
    @Override public void add(TooltipComponent image) { lines.add(Either.right(image)); }
    @Override public void addKeyUsageComponent(String translationKey, IJeiKeyMapping key) {
        add(Component.translatable(translationKey, key.getTranslatedKeyMessage()));
    }
    @Override public void setIngredient(ITypedIngredient<?> typed) { ingredient = typed; }
    @Override public void clearIngredient() { ingredient = null; }
    @Override public List<Either<FormattedText, TooltipComponent>> getLines() { return lines; }
    public Optional<ITypedIngredient<?>> ingredient() { return Optional.ofNullable(ingredient); }

    public List<ClientTooltipComponent> components() {
        var result = new ArrayList<ClientTooltipComponent>();
        for (var line : lines) {
            line.ifLeft(text -> {
                var component = Component.empty();
                text.visit((style, value) -> {
                    component.append(Component.literal(value).setStyle(style));
                    return Optional.empty();
                }, Style.EMPTY);
                result.add(ClientTooltipComponent.create(component.getVisualOrderText()));
            });
            line.ifRight(image -> {
                try { result.add(ClientTooltipComponent.create(image)); }
                catch (Exception e) { EmiLog.error("Error converting TooltipComponent", e); }
            });
        }
        return result;
    }
}
