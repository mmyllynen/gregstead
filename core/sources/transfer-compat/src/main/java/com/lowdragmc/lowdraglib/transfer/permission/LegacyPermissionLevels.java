package com.lowdragmc.lowdraglib.transfer.permission;

import net.minecraft.server.permissions.Permission;
import net.minecraft.server.permissions.PermissionLevel;
import net.minecraft.server.permissions.PermissionSet;

/** Integer OP thresholds retained for existing config and binding data. */
public final class LegacyPermissionLevels {
    private LegacyPermissionLevels() {}
    public static boolean has(PermissionSet permissions, int requiredLevel) {
        if (requiredLevel <= 0) return true;
        // byId clamps; levels above four must stay false, including the binding loop sentinel.
        if (requiredLevel > 4) return false;
        return permissions.hasPermission(new Permission.HasCommandLevel(PermissionLevel.byId(requiredLevel)));
    }
}
