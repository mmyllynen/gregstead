package com.gregtechceu.gtceu.integration.emi.orevein;

import com.gregtechceu.gtceu.api.data.worldgen.bedrockore.BedrockOreDefinition;
import com.gregtechceu.gtceu.integration.xei.widgets.GTOreVeinWidget;

import com.lowdragmc.lowdraglib.integration.emi.ModularEmiRecipe;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;

import dev.emi.emi.api.recipe.EmiRecipeCategory;
import org.jetbrains.annotations.Nullable;

public class GTBedrockOre extends ModularEmiRecipe<WidgetGroup> {

    private final Holder<BedrockOreDefinition> bedrockOre;

    public GTBedrockOre(Holder<BedrockOreDefinition> bedrockOre) {
        super(() -> new GTOreVeinWidget(bedrockOre, null));
        this.bedrockOre = bedrockOre;
    }

    @Override
    public EmiRecipeCategory getCategory() {
        return GTBedrockOreEmiCategory.CATEGORY;
    }

    @Override
    public @Nullable Identifier getId() {
        return bedrockOre.getKey().identifier().withPrefix("/bedrock_ore_diagram/");
    }
}
