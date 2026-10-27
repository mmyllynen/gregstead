package com.lowdragmc.lowdraglib.transfer.recipe;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import org.jetbrains.annotations.Nullable;
public final class CraftingRemainders {
    private CraftingRemainders() {}
    public static ItemStack stack(ItemInstance instance) {
        if(instance instanceof ItemStack stack)return stack;
        if(instance instanceof ItemStackTemplate template)return template.create();
        var stack=new ItemStack(instance.typeHolder(),instance.count());
        for(var type:BuiltInRegistries.DATA_COMPONENT_TYPE)stack.copyFrom(type,instance);
        return stack;
    }
    public static @Nullable ItemStackTemplate template(ItemStack stack) {
        return stack.isEmpty()?null:ItemStackTemplate.fromNonEmptyStack(stack);
    }
}
