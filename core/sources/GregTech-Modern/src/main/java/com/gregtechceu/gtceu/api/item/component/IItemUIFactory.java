package com.gregtechceu.gtceu.api.item.component;

import com.lowdragmc.lowdraglib.gui.factory.HeldItemUIFactory;
import com.lowdragmc.lowdraglib.gui.modular.ModularUI;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import com.lowdragmc.lowdraglib.transfer.interaction.UseResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public interface IItemUIFactory extends IInteractionItem {

    ModularUI createUI(HeldItemUIFactory.HeldItemHolder holder, Player entityPlayer);

    @Override
    default UseResult<ItemStack> use(ItemStack item, Level level, Player player,
                                                   InteractionHand usedHand) {
        if (player instanceof ServerPlayer serverPlayer) {
            HeldItemUIFactory.INSTANCE.openUI(serverPlayer, usedHand);
        }
        return UseResult.sidedSuccess(item, level.isClientSide());
    }
}
