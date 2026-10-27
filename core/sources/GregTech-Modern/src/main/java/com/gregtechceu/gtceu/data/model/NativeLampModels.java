package com.gregtechceu.gtceu.data.model;

import net.minecraft.resources.Identifier;
import net.minecraft.world.item.DyeColor;

/** Original lamp texture selection encoded using native model templates. */
public final class NativeLampModels {
    private NativeLampModels() {}
    public static NativeInlineModel model(DyeColor color, boolean border, boolean lit, boolean bloom) {
        String suffix = border ? "" : "_borderless";
        var model = new NativeInlineModel().parent(Identifier.parse("gtceu:block/lamp" + suffix + (lit ? "" : "_off")));
        String texture = "gtceu:block/lamps/" + color.getName();
        if (!lit) return model.texture("inactive", Identifier.parse(texture + "_off" + suffix));
        if (border) return model.texture("active", Identifier.parse(texture))
                .texture("active_overlay", Identifier.parse(texture + (bloom ? "_emissive" : "")));
        return model.texture("active", Identifier.parse(texture + "_borderless" + (bloom ? "_emissive" : "")));
    }
}
