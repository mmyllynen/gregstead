package dev.emi.emi.integration.ae2;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import appeng.api.stacks.AEItemKey;
import appeng.integration.modules.itemlists.CraftingHelper;
import appeng.menu.SlotSemantics;
import appeng.menu.me.items.CraftingTermMenu;
import appeng.util.CraftingRecipeUtil;
import dev.emi.emi.api.recipe.EmiPlayerInventory;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiCraftingRecipe;
import dev.emi.emi.api.recipe.VanillaEmiRecipeCategories;
import dev.emi.emi.api.recipe.handler.EmiCraftContext;
import dev.emi.emi.api.recipe.handler.EmiRecipeHandler;
import dev.emi.emi.api.recipe.handler.StandardRecipeHandler;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.Widget;
import dev.emi.emi.api.widget.SlotWidget;
import dev.emi.emi.runtime.EmiDrawContext;
import dev.emi.emi.runtime.NativeRecipeContents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.ShapedRecipe;
import org.jetbrains.annotations.Nullable;

/**
 * Restores the AE2/EMI crafting-terminal bridge using AE2's native transfer service.
 * The service retains authority over extraction, power, filters and displaced items.
 * See AE2's EmiUseCraftingRecipeHandler in neoforge/v19.0.4-alpha for the original integration model.
 */
public final class Ae2CraftingHandler implements EmiRecipeHandler<CraftingTermMenu> {
    private static final String UNSUPPORTED = "emi.ae2.unsupported_recipe";
    private static final String MISSING = "emi.ae2.missing_ingredients";
    private static final String PARTIAL = "emi.ae2.partial_ingredients";

    private static @Nullable RecipeHolder<?> resolve(EmiRecipe recipe) {
        if (recipe.getCategory() != VanillaEmiRecipeCategories.CRAFTING || recipe.getId() == null) return null;
        var holder = NativeRecipeContents.get(recipe.getId());
        if (holder == null || !(holder.value() instanceof CraftingRecipe crafting)) return null;
        if (crafting.placementInfo().isImpossibleToPlace()
                || crafting.placementInfo().slotsToIngredientIndex().size() > 9) return null;
        if (crafting instanceof ShapedRecipe shaped && (shaped.getWidth() > 3 || shaped.getHeight() > 3)) return null;
        return holder;
    }

    private static Map<Integer, Ingredient> ingredients(RecipeHolder<?> holder) {
        var matrix = CraftingRecipeUtil.ensure3by3CraftingMatrix(holder.value());
        Map<Integer, Ingredient> result = new LinkedHashMap<>();
        for (int i = 0; i < matrix.size(); i++) {
            final int slot = i;
            matrix.get(i).ifPresent(ingredient -> result.put(slot, ingredient));
        }
        return result;
    }

    @Override
    public EmiPlayerInventory getInventory(AbstractContainerScreen<CraftingTermMenu> screen) {
        var menu = screen.getMenu();
        var stacks = new ArrayList<EmiStack>();
        for (var semantic : List.of(SlotSemantics.PLAYER_INVENTORY, SlotSemantics.PLAYER_HOTBAR, SlotSemantics.CRAFTING_GRID)) {
            for (var slot : menu.getSlots(semantic)) {
                if (slot.mayPickup(Minecraft.getInstance().player)
                        && (semantic == SlotSemantics.CRAFTING_GRID || !menu.isPlayerInventorySlotLocked(slot.getContainerSlot()))) {
                    stacks.add(EmiStack.of(slot.getItem()));
                }
            }
        }
        var repo = menu.getClientRepo();
        if (repo != null && menu.getLinkStatus().connected()) {
            for (var entry : repo.getAllEntries()) {
                if (entry.getStoredAmount() > 0 && entry.getWhat() instanceof AEItemKey item && menu.isKeyVisible(item)) {
                    stacks.add(EmiStack.of(item.toStack()).setAmount(entry.getStoredAmount()));
                }
            }
        }
        return new EmiPlayerInventory(stacks);
    }

    @Override
    public boolean supportsRecipe(EmiRecipe recipe) {
        return resolve(recipe) != null;
    }

    @Override
    public boolean canCraft(EmiRecipe recipe, EmiCraftContext<CraftingTermMenu> context) {
        var holder = resolve(recipe);
        if (holder == null) return false;
        var inputs = ingredients(holder);
        var missing = context.getScreenHandler().findMissingIngredients(inputs);
        return inputs.size() > missing.totalSize();
    }

    @Override
    public boolean craft(EmiRecipe recipe, EmiCraftContext<CraftingTermMenu> context) {
        var holder = resolve(recipe);
        if (holder == null || !canCraft(recipe, context)) return false;
        // Fill real ingredients only, including for modified clicks. Never schedule autocrafting.
        CraftingHelper.performTransfer(context.getScreenHandler(), holder.id(), holder.value(), false);
        Minecraft.getInstance().gui.setScreen(context.getScreen());
        return true;
    }

    @Override
    public List<ClientTooltipComponent> getTooltip(EmiRecipe recipe, EmiCraftContext<CraftingTermMenu> context) {
        var holder = resolve(recipe);
        if (holder == null) return List.of(ClientTooltipComponent.create(Component.translatable(UNSUPPORTED).getVisualOrderText()));
        var inputs = ingredients(holder);
        var missing = context.getScreenHandler().findMissingIngredients(inputs);
        if (missing.totalSize() == 0) return List.of();
        return List.of(ClientTooltipComponent.create(Component.translatable(
                inputs.size() > missing.totalSize() ? PARTIAL : MISSING).getVisualOrderText()));
    }

    @Override
    public void render(EmiRecipe recipe, EmiCraftContext<CraftingTermMenu> context, List<Widget> widgets,
                       GuiGraphicsExtractor draw) {
        var holder = resolve(recipe);
        if (holder == null) return;
        if (!(recipe instanceof EmiCraftingRecipe crafting)) {
            StandardRecipeHandler.renderMissing(recipe, getInventory(context.getScreen()), widgets, draw);
            return;
        }
        var missing = context.getScreenHandler().findMissingIngredients(ingredients(holder));
        int offset = 0;
        if (!crafting.shapeless) {
            if (crafting.canFit(1, 3)) offset--;
            if (crafting.canFit(3, 1)) offset -= 3;
        }
        var graphics = EmiDrawContext.wrap(draw);
        graphics.raw().nextStratum();
        int displaySlot = 0;
        for (var widget : widgets) {
            if (widget instanceof SlotWidget slot && slot.getRecipe() == null) {
                int index = displaySlot++ + offset;
                if (missing.missingSlots().contains(index) || missing.craftableSlots().contains(index)) {
                    var bounds = slot.getBounds();
                    graphics.fill(bounds.x(), bounds.y(), bounds.width(), bounds.height(), 0x44FF0000);
                }
            }
        }
    }
}
