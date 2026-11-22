package fi.gregstead.mushroom.mixin.client;

import fi.gregstead.mushroom.MushroomDimension;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Hud;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(Hud.class)
abstract class PortalOverlayMixin {
    @Shadow @Final private Minecraft minecraft;

    @ModifyArg(method = "extractPortalOverlay",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/block/BlockStateModelSet;getParticleMaterial(Lnet/minecraft/world/level/block/state/BlockState;)Lnet/minecraft/client/resources/model/sprite/Material$Baked;"))
    private BlockState mushroom$portalTexture(BlockState vanilla) {
        var player = minecraft.player;
        return player != null && player.portalProcess != null && player.portalProcess.isSamePortal(MushroomDimension.PORTAL.get())
                ? MushroomDimension.PORTAL.get().defaultBlockState()
                : vanilla;
    }
}
