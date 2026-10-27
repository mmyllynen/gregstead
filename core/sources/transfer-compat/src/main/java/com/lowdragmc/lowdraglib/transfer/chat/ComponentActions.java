package com.lowdragmc.lowdraglib.transfer.chat;

import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.resources.Identifier;
import java.util.Optional;

/** Typed payloads consumed by the existing component-panel click route. */
public final class ComponentActions {
    public static final Identifier BUTTON = Identifier.fromNamespaceAndPath("gttransfercompat", "component_button");
    public static final Identifier URL = Identifier.fromNamespaceAndPath("gttransfercompat", "component_url");
    private ComponentActions() {}
    public static ClickEvent button(String data) { return action(BUTTON, data); }
    public static ClickEvent url(String url) { return action(URL, url); }
    private static ClickEvent action(Identifier id, String value) {
        return new ClickEvent.Custom(id, Optional.of(StringTag.valueOf(value)));
    }
    public static Optional<String> buttonPayload(ClickEvent event) { return payload(event, BUTTON); }
    public static Optional<String> urlPayload(ClickEvent event) { return payload(event, URL); }
    private static Optional<String> payload(ClickEvent event, Identifier id) {
        if (event instanceof ClickEvent.Custom custom && custom.id().equals(id)
                && custom.payload().orElse(null) instanceof StringTag text) {
            return Optional.of(text.value());
        }
        return Optional.empty();
    }
}
