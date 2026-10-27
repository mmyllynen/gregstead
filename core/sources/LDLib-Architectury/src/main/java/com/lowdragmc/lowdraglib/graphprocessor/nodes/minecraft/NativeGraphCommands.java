package com.lowdragmc.lowdraglib.graphprocessor.nodes.minecraft;

import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.permissions.LevelBasedPermissionSet;
import net.minecraft.server.permissions.PermissionLevel;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

/** Original graph server source expressed through native level-based permissions. */
public final class NativeGraphCommands {
    private NativeGraphCommands() {}
    public static boolean enabled(ServerLevel level) { return level.isCommandBlockEnabled(); }
    public static CommandSourceStack source(ServerLevel level, Vec3 position, CommandSource output) {
        return new CommandSourceStack(output,
                position == null ? Vec3.atLowerCornerOf(level.getRespawnData().globalPos().pos()) : position,
                Vec2.ZERO, level, LevelBasedPermissionSet.forLevel(PermissionLevel.byId(5)),
                Component.literal("Server"), level.getServer());
    }
}
