package com.lowdragmc.lowdraglib.transfer.tool;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;
/** Original material-tier values remain owned, rather than aliased to a removed vanilla type. */
public interface ToolTier {
    int getUses();float getSpeed();float getAttackDamageBonus();
    TagKey<Block> getIncorrectBlocksForDrops();int getEnchantmentValue();Ingredient getRepairIngredient();
}
