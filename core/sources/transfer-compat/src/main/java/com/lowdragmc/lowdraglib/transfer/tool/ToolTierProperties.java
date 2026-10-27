package com.lowdragmc.lowdraglib.transfer.tool;
import net.minecraft.world.item.Item;
public final class ToolTierProperties {
    private ToolTierProperties() {}
    public static Item.Properties configure(Item.Properties properties,ToolTier tier){return properties.durability(tier.getUses());}
}
