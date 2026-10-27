package com.gregtechceu.gtceu.api.recipe;

/** Original grid bounds retained when Minecraft trims empty crafting rows/columns. */
public interface FullCraftingGrid {
    void gtceu$setGrid(int width, int height, int left, int top);
    int gtceu$width();
    int gtceu$height();
    int gtceu$left();
    int gtceu$top();
}
