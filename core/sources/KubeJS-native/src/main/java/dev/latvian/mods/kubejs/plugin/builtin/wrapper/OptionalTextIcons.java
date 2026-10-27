package dev.latvian.mods.kubejs.plugin.builtin.wrapper;

import net.minecraft.network.chat.Component;
import net.neoforged.fml.ModList;

/** The optional tooltip font stays optional for dedicated servers and standalone clients. */
final class OptionalTextIcons {
    private OptionalTextIcons() {}

    static Component resolve(String name, String fallback) {
        var mods = ModList.get();
        if (mods != null && mods.isLoaded("betteradvancedtooltips")) return InstalledIcons.get(name);
        return Component.literal(fallback);
    }

    private static final class InstalledIcons {
        private static Component get(String name) {
            return switch (name) {
                case "SMALL_SPACE" -> dev.latvian.mods.betteradvancedtooltips.BATIcons.SMALL_SPACE;
                case "ERROR" -> dev.latvian.mods.betteradvancedtooltips.BATIcons.ERROR;
                case "PLUS" -> dev.latvian.mods.betteradvancedtooltips.BATIcons.PLUS;
                case "MINUS" -> dev.latvian.mods.betteradvancedtooltips.BATIcons.MINUS;
                case "TILDE" -> dev.latvian.mods.betteradvancedtooltips.BATIcons.TILDE;
                case "COPY" -> dev.latvian.mods.betteradvancedtooltips.BATIcons.COPY;
                case "ID" -> dev.latvian.mods.betteradvancedtooltips.BATIcons.ID;
                case "INFO" -> dev.latvian.mods.betteradvancedtooltips.BATIcons.INFO;
                case "CAMERA" -> dev.latvian.mods.betteradvancedtooltips.BATIcons.CAMERA;
                case "NO" -> dev.latvian.mods.betteradvancedtooltips.BATIcons.NO;
                case "PROTOTYPE_COMPONENT" -> dev.latvian.mods.betteradvancedtooltips.BATIcons.PROTOTYPE_COMPONENT;
                case "PATCHED_COMPONENT" -> dev.latvian.mods.betteradvancedtooltips.BATIcons.PATCHED_COMPONENT;
                case "FIRE" -> dev.latvian.mods.betteradvancedtooltips.BATIcons.FIRE;
                case "TAG" -> dev.latvian.mods.betteradvancedtooltips.BATIcons.TAG;
                case "WARN" -> dev.latvian.mods.betteradvancedtooltips.BATIcons.WARN;
                case "YES" -> dev.latvian.mods.betteradvancedtooltips.BATIcons.YES;
                default -> throw new IllegalArgumentException("Unknown tooltip icon " + name);
            };
        }
    }
}
