package com.lowdragmc.lowdraglib.transfer.mixin;

import java.util.List;
import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.client.renderer.item.CuboidItemModelWrapper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(CuboidItemModelWrapper.class)
public interface ItemModelTintAccess {
    @Accessor("tints") List<ItemTintSource> gttransfercompat$tints();
}
