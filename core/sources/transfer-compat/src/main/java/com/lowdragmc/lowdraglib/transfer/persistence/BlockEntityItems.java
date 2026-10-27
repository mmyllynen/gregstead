package com.lowdragmc.lowdraglib.transfer.persistence;

import net.minecraft.core.HolderLookup;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.TagValueOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Original custom-data removal, typed payload, then collected-components order. */
public final class BlockEntityItems {
    private static final Logger LOGGER=LoggerFactory.getLogger(BlockEntityItems.class);
    private BlockEntityItems() {}
    public static void save(BlockEntity entity,ItemStack stack,HolderLookup.Provider registries) {
        try(var reporter=new ProblemReporter.ScopedCollector(entity.problemPath(),LOGGER)) {
            var output=TagValueOutput.createWithContext(reporter,registries);
            entity.saveCustomOnly(output);
            entity.removeComponentsFromTag(output);
            BlockItem.setBlockEntityData(stack,entity.getType(),output);
            stack.applyComponents(entity.collectComponents());
        }
    }
}
