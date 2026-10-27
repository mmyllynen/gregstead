package com.lowdragmc.lowdraglib.compass.component;

import com.lowdragmc.lowdraglib.compass.ILayoutComponent;
import com.lowdragmc.lowdraglib.compass.LayoutPageWidget;
import com.lowdragmc.lowdraglib.gui.texture.ItemStackTexture;
import com.lowdragmc.lowdraglib.gui.texture.ProgressTexture;
import com.lowdragmc.lowdraglib.gui.texture.ResourceBorderTexture;
import com.lowdragmc.lowdraglib.gui.texture.ResourceTexture;
import com.lowdragmc.lowdraglib.gui.widget.ImageWidget;
import com.lowdragmc.lowdraglib.gui.widget.ProgressWidget;
import com.lowdragmc.lowdraglib.gui.widget.SlotWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.misc.CycleItemStackHandler;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import lombok.NoArgsConstructor;
import net.minecraft.client.Minecraft;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.*;
import com.lowdragmc.lowdraglib.transfer.item.ItemStackHandler;
import org.w3c.dom.Element;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

/**
 * @author KilaBash
 * @date 2022/10/21
 * @implNote RecipeComponent
 */
@NoArgsConstructor
public class RecipeComponent extends AbstractComponent {
    public static final ResourceTexture PROGRESS_BAR_ARROW = new ResourceTexture("ldlib:textures/gui/progress_bar_arrow.png");

    private static final List<RecipeViewCreator> RECIPE_VIEW_CREATORS = new ArrayList<>();

    public interface RecipeViewCreator extends Predicate<Recipe<?>> {
        ItemStack getWorkstation(RecipeHolder<?> recipe);

        WidgetGroup getViewWidget(RecipeHolder<?> recipe);
    }

    public static void registerRecipeViewCreator(RecipeViewCreator recipeViewCreator) {
        RECIPE_VIEW_CREATORS.add(recipeViewCreator);
    }

    @Nullable
    protected RecipeHolder<?> recipe;

    @Override
    public ILayoutComponent fromXml(Element element) {
        super.fromXml(element);
        if (element.hasAttribute("id")) {
            var recipeID = Identifier.parse(element.getAttribute("id"));
            this.recipe = com.lowdragmc.lowdraglib.client.NativeRecipeCatalog.get(recipeID);
        }
        return this;
    }

    @Override
    protected LayoutPageWidget addWidgets(LayoutPageWidget currentPage) {
        if (recipe == null) return currentPage;
        WidgetGroup recipeGroup = null;
        ItemStack workstation = ItemStack.EMPTY;
        for (RecipeViewCreator creator : RECIPE_VIEW_CREATORS) {
            if (creator.test(recipe.value())) {
                recipeGroup = creator.getViewWidget(recipe);
                workstation = creator.getWorkstation(recipe);
                break;
            }
        }
        if (recipeGroup == null) {
            var view = NativeRecipeView.of(recipe.value(), Minecraft.getInstance().level);
            Int2ObjectMap<Ingredient> inputs = new Int2ObjectArrayMap<>();
            inputs.putAll(view.inputs());
            var output = view.output();
            if (recipe.value() instanceof CraftingRecipe) {
                recipeGroup = createCraftingRecipeWidget(inputs, output);
                workstation = new ItemStack(Items.CRAFTING_TABLE);
            } else if (recipe.value() instanceof AbstractCookingRecipe) {
                recipeGroup = createSmeltingRecipeWidget(inputs, output);
                workstation = new ItemStack(Items.FURNACE);
            } else {
                recipeGroup = createCraftingRecipeWidget(inputs, output);
            }
        }
        recipeGroup.addWidget(new ImageWidget(-40, recipeGroup.getSize().height / 2 - 15, 30, 30, new ItemStackTexture(workstation)));
        return currentPage.addStreamWidget(wrapper(recipeGroup));
    }

    protected WidgetGroup createSmeltingRecipeWidget(Int2ObjectMap<Ingredient> input, ItemStack output) {
        WidgetGroup widgetGroup = new WidgetGroup(0, 0, 150, 30);
        widgetGroup.setBackground(ResourceBorderTexture.BORDERED_BACKGROUND);
        CycleItemStackHandler itemStackHandler = new CycleItemStackHandler(List.of(
                NativeRecipeView.items(input.get(0))));
        widgetGroup.addWidget(new SlotWidget(itemStackHandler, 0, 20, 6, false, false));

        var handler = new ItemStackHandler();
        handler.setStackInSlot(0, output);
        widgetGroup.addWidget(new ProgressWidget(ProgressWidget.JEIProgress, 65, 5, 20, 20, new ProgressTexture()));
        widgetGroup.addWidget(new SlotWidget(handler, 0, 120, 6, false, false));
        return widgetGroup;
    }

    protected WidgetGroup createCraftingRecipeWidget(Int2ObjectMap<Ingredient> input, ItemStack output) {
        WidgetGroup widgetGroup = new WidgetGroup(0, 0, 150, 12 + 18 * 3);
        widgetGroup.setBackground(ResourceBorderTexture.BORDERED_BACKGROUND);
        for (int x = 0; x < 3; x++) {
            for (int y = 0; y < 3; y++) {
                var itemStackHandler = new CycleItemStackHandler(List.of(
                        NativeRecipeView.items(input.get(x + y * 3))));
                widgetGroup.addWidget(new SlotWidget(itemStackHandler, 0, x * 18 + 20, y * 18 + 6, false, false));
            }
        }

        var handler = new ItemStackHandler();
        handler.setStackInSlot(0, output);
        widgetGroup.addWidget(new ProgressWidget(ProgressWidget.JEIProgress, (3 * 18 + 20) / 2 + 60 - 10, (12 + 18 * 3) / 2 - 10, 20, 20, PROGRESS_BAR_ARROW));
        widgetGroup.addWidget(new SlotWidget(handler, 0, 120, (12 + 18 * 3) / 2 - 9, false, false));
        return widgetGroup;
    }
}
