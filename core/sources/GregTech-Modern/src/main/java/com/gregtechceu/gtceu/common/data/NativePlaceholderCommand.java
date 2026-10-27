package com.gregtechceu.gtceu.common.data;

import com.gregtechceu.gtceu.core.mixins.CommandSourceStackInvoker;
import net.minecraft.commands.*;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.permissions.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.*;

public final class NativePlaceholderCommand {
    private NativePlaceholderCommand() {}
    public static CommandSourceStack source(CommandSource output, Vec3 pos, ServerLevel level,
            int opLevel, Component name, Player player) {
        return CommandSourceStackInvoker.gtceu$create(output, pos, Vec2.ZERO, level,
                LevelBasedPermissionSet.forLevel(PermissionLevel.byId(opLevel)),
                CommandSourceStack.NamesProvider.constant(name), level.getServer(), player);
    }
}
