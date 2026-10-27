package com.lowdragmc.lowdraglib.transfer.mixin;

import net.minecraft.core.component.DataComponentInitializers;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Item.Properties.class)
public interface ItemPropertiesComponentsAccessor {
    @Accessor("componentInitializer") DataComponentInitializers.Initializer<Item> gtport$initializer();
    @Accessor("componentInitializer") void gtport$initializer(DataComponentInitializers.Initializer<Item> initializer);
}
