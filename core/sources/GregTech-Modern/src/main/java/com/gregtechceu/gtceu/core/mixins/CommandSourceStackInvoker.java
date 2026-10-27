package com.gregtechceu.gtceu.core.mixins;

import net.minecraft.commands.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.permissions.PermissionSet;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(CommandSourceStack.class)
public interface CommandSourceStackInvoker {
    @Invoker("<init>")
    static CommandSourceStack gtceu$create(CommandSource source, Vec3 pos, Vec2 rotation, ServerLevel level,
            PermissionSet permissions, CommandSourceStack.NamesProvider names, MinecraftServer server, Entity entity) {
        throw new AssertionError("Mixin constructor invoker was not applied");
    }
}
