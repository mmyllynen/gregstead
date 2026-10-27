package com.gregtechceu.gtceu.client;

import net.minecraft.core.ClientAsset;
import net.minecraft.resources.Identifier;
import net.minecraft.client.entity.ClientAvatarEntity;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.world.entity.Avatar;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.PlayerSkin;
import net.neoforged.neoforge.client.renderstate.AvatarRenderStateModifier;
import net.neoforged.neoforge.client.renderstate.RegisterRenderStateModifiersEvent;
import java.util.UUID;
import java.util.function.Function;

/** Applies custom capes to detached frame state, preserving the native player-info skin. */
public final class NativePlayerCapes {
    private NativePlayerCapes() {}
    public static PlayerSkin withCape(PlayerSkin original, Identifier texturePath) {
        if (texturePath == null) return original;
        var cape = new ClientAsset.ResourceTexture(texturePath, texturePath);
        return new PlayerSkin(original.body(), cape, original.elytra(), original.model(), original.secure());
    }
    public static void register(RegisterRenderStateModifiersEvent event, Function<UUID, Identifier> lookup) {
        event.registerAvatarEntityModifier(new AvatarRenderStateModifier() {
            @Override public <T extends Avatar & ClientAvatarEntity> void accept(T avatar, AvatarRenderState state) {
                if (avatar instanceof Player player) state.skin = withCape(state.skin, lookup.apply(player.getUUID()));
            }
        });
    }
}
