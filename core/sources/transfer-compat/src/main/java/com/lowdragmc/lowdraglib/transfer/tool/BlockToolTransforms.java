package com.lowdragmc.lowdraglib.transfer.tool;

import net.minecraft.core.component.BlockTransformer;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.component.BlockTransformers;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.ItemAbility;
import net.neoforged.neoforge.common.DataMapHooks;
import org.jetbrains.annotations.Nullable;

/** Native transformation lookup while GT retains its own AoE/durability/sound logic. */
public final class BlockToolTransforms {
    private BlockToolTransforms() {}
    public static final ItemAbility AXE_STRIP = ItemAbility.get("axe_strip");
    public static final ItemAbility AXE_SCRAPE = ItemAbility.get("axe_scrape");
    public static final ItemAbility AXE_WAX_OFF = ItemAbility.get("axe_wax_off");
    public static final ItemAbility SHOVEL_FLATTEN = ItemAbility.get("shovel_flatten");
    public static final ItemAbility HOE_TILL = ItemAbility.get("till");

    public static @Nullable BlockState modified(BlockState state, UseOnContext context, ItemAbility ability, boolean simulate) {
        BlockState custom = state.getToolModifiedState(context, ability, simulate);
        if (custom != null) return custom;
        ResourceKey<BlockTransformer> key;
        String sound;
        if (ability == AXE_STRIP) { key=BlockTransformers.AXE; sound="item.axe.strip"; }
        else if (ability == AXE_SCRAPE) { key=BlockTransformers.AXE; sound="item.axe.scrape"; }
        else if (ability == AXE_WAX_OFF) { key=BlockTransformers.AXE; sound="item.axe.wax_off"; }
        else if (ability == SHOVEL_FLATTEN) { key=BlockTransformers.SHOVEL; sound="item.shovel.flatten"; }
        else if (ability == HOE_TILL) { key=BlockTransformers.HOE; sound="item.hoe.till"; }
        else return null;
        var level=context.getLevel();var pos=context.getClickedPos();
        var holder=level.registryAccess().lookupOrThrow(Registries.BLOCK_TRANSFORMER).getOrThrow(key);
        for(var transform:DataMapHooks.appendDatamapTransformers(holder,holder.value().transforms())) {
            if(!transform.sound().value().location().equals(Identifier.withDefaultNamespace(sound))
                    || transform.disallowedFaces().contains(context.getClickedFace())) continue;
            var result=transform.blockStateProvider().value().getOptionalState(level,level.getRandom(),pos);
            if(result==null)continue;
            if(!simulate && level instanceof ServerLevel serverLevel) {
                transform.loot().ifPresent(loot -> Block.dropFromBlockInteractLootTable(serverLevel,loot,pos,state,
                        level.getBlockEntity(pos),context.getItemInHand(),context.getPlayer(),
                        (world,stack)->transform.dropStrategy().pop(world,pos,context.getClickedFace(),stack)));
            }
            return transform.updateFromNeighbors() ? Block.updateFromNeighbourShapes(result,level,pos) : result;
        }
        return null;
    }
}
