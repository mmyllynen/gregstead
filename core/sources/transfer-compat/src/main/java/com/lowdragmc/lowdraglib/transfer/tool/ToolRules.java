package com.lowdragmc.lowdraglib.transfer.tool;

import com.lowdragmc.lowdraglib.transfer.recipe.TagReferences;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.component.Tool;
import net.minecraft.world.level.block.Block;
import java.util.List;

/** Native rule construction with live registry tags and unchanged rule priority. */
public final class ToolRules {
    private ToolRules() {}
    public static Tool.Rule deniesDrops(TagKey<Block> tag) {
        return Tool.Rule.deniesDrops(TagReferences.get(BuiltInRegistries.BLOCK,tag));
    }
    public static Tool.Rule minesAndDrops(TagKey<Block> tag,float speed) {
        return Tool.Rule.minesAndDrops(TagReferences.get(BuiltInRegistries.BLOCK,tag),speed);
    }
    public static Tool.Rule minesAndDrops(List<Block> blocks,float speed) {
        return Tool.Rule.minesAndDrops(HolderSet.direct(blocks.stream().map(BuiltInRegistries.BLOCK::wrapAsHolder).toList()),speed);
    }
    public static Tool.Rule overrideSpeed(TagKey<Block> tag,float speed) {
        return Tool.Rule.overrideSpeed(TagReferences.get(BuiltInRegistries.BLOCK,tag),speed);
    }
    public static Tool withRules(Tool tool,List<Tool.Rule> rules) {
        return new Tool(rules,tool.defaultMiningSpeed(),tool.damagePerBlock(),tool.canDestroyBlocksInCreative());
    }
}
